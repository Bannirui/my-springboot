package com.github.bannirui.msb.mq.sdk.producer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.bannirui.msb.mq.sdk.Mms;
import com.github.bannirui.msb.mq.sdk.common.BrokerType;
import com.github.bannirui.msb.mq.sdk.common.SimpleMessage;
import com.github.bannirui.msb.mq.sdk.common.MmsEnv;
import com.github.bannirui.msb.mq.sdk.message.statistic.PingInfo;
import com.github.bannirui.msb.mq.sdk.metadata.ClusterMetadata;
import com.github.bannirui.msb.mq.sdk.utils.Utils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PingTask {
    private static final Logger logger = LoggerFactory.getLogger(PingTask.class);
    private static final Pattern pattern;
    private static final Pattern patternDatalost;
    private static final Pattern patternPingInfo;
    ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(10, new ThreadFactory() {
        final AtomicInteger index = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "ping-thread-" + this.index.incrementAndGet());
        }
    });
    private volatile boolean running = false;

    static {
        pattern = Pattern.compile("(\\d+ms)(\\s+)(TTL=\\d+)", 2);
        patternDatalost = Pattern.compile(".*?(\\d+)%\\s+packet\\sloss", 2);
        patternPingInfo = Pattern.compile(".*?=\\s+([\\d\\.]+)/([\\d\\.]+)/([\\d\\.]+)/([\\d\\.]+)\\sms", 2);
    }

    public PingTask(boolean running) {
        this.running = running;
    }

    public void start() {
        this.scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (this.running) {
                this.collectUsingServers().forEach(x-> this.ping(x, 1000));
            }
        }, 0L, 10L, TimeUnit.SECONDS);
    }

    public void shutDown() {
        Utils.gracefullyShutdown(this.scheduledExecutorService);
    }

    public Set<String> collectUsingServers() {
        List<ClusterMetadata> usingCluster = Lists.newArrayList();
        ProducerFactory.getProducers().forEach(mmsProducerProxy->{
            if (!usingCluster.contains(mmsProducerProxy.getMetadata().getClusterMetadata())) {
                usingCluster.add(mmsProducerProxy.getMetadata().getClusterMetadata());
            }
        });
        Set<String> ips = Sets.newHashSet();
        usingCluster.forEach(cluster-> ips.addAll(this.extractServerIps(cluster)));
        return ips;
    }

    private List<String> extractServerIps(ClusterMetadata cluster) {
        List<String> ips = Lists.newArrayList();
        return BrokerType.ROCKETMQ.equals(cluster.getBrokerType()) ? this.extractServerIps(cluster.getServerIps(), ";") : this.extractServerIps(cluster.getBootAddr(), ",");
    }

    private List<String> extractServerIps(String ipStr, String separator) {
        List<String> ips = Lists.newArrayList();
        String[] split = ipStr.split(separator);
        String[] var5 = split;
        int var6 = split.length;
        for(int var7 = 0; var7 < var6; ++var7) {
            String str = var5[var7];
            ips.add(str.substring(0, str.indexOf(":")));
        }
        return ips;
    }

    public void ping(String ipAddress, int timeOut) {
        BufferedReader in = null;
        Runtime r = Runtime.getRuntime();
        String pingCommand = "ping " + ipAddress + " -c 10 -W " + timeOut;
        try {
            Process p = r.exec(pingCommand);
            if (p == null) {
                logger.error("create PingTask process {} error", pingCommand);
                return;
            }
            in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = in.readLine();
            PingInfo pingInfo = new PingInfo();
            pingInfo.setSource(MmsEnv.MMS_IP);
            pingInfo.setTarget(ipAddress);
            boolean running = true;

            while(true) {
                while(true) {
                    do {
                        if (!running) {
                            p.destroy();
                            return;
                        }
                        line = in.readLine();
                        Thread.sleep(1000L);
                    } while(!StringUtils.isNoneEmpty(line));
                    if (line.startsWith("64 bytes from")) {
                        pingInfo.succeedCount();
                    } else if (line.startsWith("request timeout")) {
                        pingInfo.timeoutCount();
                    } else if ("Destination Host Unreachable".equalsIgnoreCase(line)) {
                        pingInfo.failSendCount();
                    } else if (line.startsWith("10 packets transmitted,")) {
                        running = false;
                        pingInfo.setDataLost(parseDataLost(line));
                        while(StringUtils.isNoneEmpty(line = in.readLine())) {
                            parseToPingInfo(line, pingInfo);
                        }
                        Mms.sendOneway("statistic_ping_topic", new SimpleMessage(JSON.toJSONBytes(pingInfo, new SerializerFeature[0])));
                    }
                }
            }
        } catch (Exception var19) {
            logger.error("PingTask executed error", var19);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                logger.error("exit PingTask error", e);
            }
        }
    }

    private static int getCheckResult(String line) {
        Matcher matcher = pattern.matcher(line);
        return matcher.find() ? 1 : 0;
    }

    private static Double parseDataLost(String line) {
        Matcher matcher = patternDatalost.matcher(line);
        matcher.find();
        return Double.valueOf(matcher.group(1));
    }

    private static void parseToPingInfo(String line, PingInfo pingInfo) {
        Matcher matcher = patternPingInfo.matcher(line);
        while(matcher.find()) {
            pingInfo.setMin(Double.valueOf(matcher.group(1)));
            pingInfo.setAvg(Double.valueOf(matcher.group(2)));
            pingInfo.setMax(Double.valueOf(matcher.group(3)));
            pingInfo.setStdev(Double.valueOf(matcher.group(4)));
        }
    }
}
