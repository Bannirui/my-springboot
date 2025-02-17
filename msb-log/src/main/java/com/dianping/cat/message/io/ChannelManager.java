package com.dianping.cat.message.io;

import com.dianping.cat.configuration.ClientConfigManager;
import com.dianping.cat.configuration.KVConfig;
import com.dianping.cat.message.internal.MessageIdFactory;
import com.dianping.cat.message.spi.MessageQueue;
import com.site.helper.JsonBuilder;
import com.site.helper.Splitters;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.plexus.logging.Logger;
import org.unidal.helper.Files;
import org.unidal.helper.Threads;
import org.unidal.helper.Urls;
import org.unidal.tuple.Pair;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ChannelManager implements Threads.Task {
    private ClientConfigManager m_configManager;
    private Bootstrap m_bootstrap;
    private Logger m_logger;
    private boolean m_active = true;
    private int m_retriedTimes = 0;
    private int m_count = -10;
    private volatile double m_sample = 1.0D;
    private MessageQueue m_queue;
    private ChannelManager.ChannelHolder m_activeChannelHolder;
    private MessageIdFactory m_idfactory;
    private JsonBuilder m_jsonBuilder = new JsonBuilder();

    public ChannelManager(Logger logger, List<InetSocketAddress> serverAddresses, MessageQueue queue, ClientConfigManager configManager,
                          MessageIdFactory idFactory) {
        this.m_logger = logger;
        this.m_queue = queue;
        this.m_configManager = configManager;
        this.m_idfactory = idFactory;
        EventLoopGroup group = new NioEventLoopGroup(1);
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.handler(new ChannelInitializer<>() {
            protected void initChannel(Channel ch) throws Exception {
            }
        });
        this.m_bootstrap = bootstrap;
        String serverConfig = this.loadServerConfig();
        if (StringUtils.isNotEmpty(serverConfig)) {
            List<InetSocketAddress> configedAddresses = this.parseSocketAddress(serverConfig);
            ChannelManager.ChannelHolder holder = this.initChannel(configedAddresses, serverConfig);
            if (holder != null) {
                this.m_activeChannelHolder = holder;
            } else {
                this.m_activeChannelHolder = new ChannelManager.ChannelHolder();
                this.m_activeChannelHolder.setServerAddresses(configedAddresses);
            }
        } else {
            ChannelManager.ChannelHolder holder = this.initChannel(serverAddresses, (String) null);
            if (holder != null) {
                this.m_activeChannelHolder = holder;
            } else {
                this.m_activeChannelHolder = new ChannelManager.ChannelHolder();
                this.m_activeChannelHolder.setServerAddresses(serverAddresses);
                this.m_logger.error("error when init cat module due to error config xml in /data/appdatas/cat/client.xml");
            }
        }
    }

    public ChannelFuture channel() {
        return this.m_activeChannelHolder != null ? this.m_activeChannelHolder.getActiveFuture() : null;
    }

    private void checkServerChanged() {
        if (this.shouldCheckServerConfig(++this.m_count)) {
            Pair<Boolean, String> pair = this.routerConfigChanged();
            if (pair.getKey()) {
                String servers = pair.getValue();
                List<InetSocketAddress> serverAddresses = this.parseSocketAddress(servers);
                ChannelManager.ChannelHolder newHolder = this.initChannel(serverAddresses, servers);
                if (newHolder != null) {
                    if (newHolder.isConnectChanged()) {
                        ChannelManager.ChannelHolder last = this.m_activeChannelHolder;
                        this.m_activeChannelHolder = newHolder;
                        this.closeChannelHolder(last);
                        this.m_logger.info("switch active channel to " + this.m_activeChannelHolder);
                    } else {
                        this.m_activeChannelHolder = newHolder;
                    }
                }
            }
        }
    }

    private void closeChannel(ChannelFuture channel) {
        try {
            if (channel != null) {
                this.m_logger.info("close channel " + channel.channel().remoteAddress());
                channel.channel().close();
            }
        } catch (Exception var3) {
        }
    }

    private void closeChannelHolder(ChannelManager.ChannelHolder channelHolder) {
        try {
            ChannelFuture channel = channelHolder.getActiveFuture();
            this.closeChannel(channel);
            channelHolder.setActiveIndex(-1);
        } catch (Exception var3) {
        }
    }

    private ChannelFuture createChannel(InetSocketAddress address) {
        ChannelFuture future = null;
        try {
            future = this.m_bootstrap.connect(address);
            future.awaitUninterruptibly(100L, TimeUnit.MILLISECONDS);
            if (future.isSuccess()) {
                this.m_logger.info("Connected to CAT server at " + address);
                return future;
            }
            this.m_logger.error("Error when try connecting to " + address);
            this.closeChannel(future);
        } catch (Throwable var4) {
            this.m_logger.error("Error when connect server " + address.getAddress(), var4);
            if (future != null) {
                this.closeChannel(future);
            }
        }
        return null;
    }

    private void doubleCheckActiveServer(ChannelFuture activeFuture) {
        try {
            if (this.isChannelStalled(activeFuture) || this.isChannelDisabled(activeFuture)) {
                this.closeChannelHolder(this.m_activeChannelHolder);
            }
        } catch (Throwable var3) {
            this.m_logger.error(var3.getMessage(), var3);
        }
    }

    @Override
    public String getName() {
        return "TcpSocketSender-ChannelManager";
    }

    public double getSample() {
        return this.m_sample;
    }

    private ChannelManager.ChannelHolder initChannel(List<InetSocketAddress> addresses, String serverConfig) {
        InetSocketAddress address;
        try {
            int len = addresses.size();
            for (int i = 0; i < len; ++i) {
                address = (InetSocketAddress) addresses.get(i);
                String hostAddress = address.getAddress().getHostAddress();
                ChannelManager.ChannelHolder holder = null;
                if (this.m_activeChannelHolder != null && hostAddress.equals(this.m_activeChannelHolder.getIp())) {
                    holder = new ChannelManager.ChannelHolder();
                    holder.setActiveFuture(this.m_activeChannelHolder.getActiveFuture()).setConnectChanged(false);
                } else {
                    ChannelFuture future = this.createChannel(address);
                    if (future != null) {
                        holder = new ChannelManager.ChannelHolder();
                        holder.setActiveFuture(future).setConnectChanged(true);
                    }
                }
                if (holder != null) {
                    holder.setActiveIndex(i).setIp(hostAddress);
                    holder.setActiveServerConfig(serverConfig).setServerAddresses(addresses);
                    this.m_logger.info("success when init CAT server, new active holder" + holder.toString());
                    return holder;
                }
            }
        } catch (Exception var10) {
            this.m_logger.error(var10.getMessage(), var10);
        }
        try {
            StringBuilder sb = new StringBuilder();
            addresses.forEach(x -> sb.append(x.toString()).append(";"));
            this.m_logger.info("Error when init CAT server " + sb.toString());
        } catch (Exception var9) {
        }
        return null;
    }

    private boolean isChannelDisabled(ChannelFuture activeFuture) {
        return activeFuture != null && !activeFuture.channel().isOpen();
    }

    private boolean isChannelStalled(ChannelFuture activeFuture) {
        ++this.m_retriedTimes;
        int size = this.m_queue.size();
        boolean stalled = activeFuture != null && size >= 4990;
        if (stalled) {
            if (this.m_retriedTimes >= 5) {
                this.m_retriedTimes = 0;
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private String loadServerConfig() {
        try {
            String url = this.m_configManager.getServerConfigUrl();
            InputStream inputstream = Urls.forIO().readTimeout(2000).connectTimeout(1000).openStream(url);
            String content = Files.forIO().readFrom(inputstream, "utf-8");
            KVConfig routerConfig = (KVConfig) this.m_jsonBuilder.parse(content.trim(), KVConfig.class);
            String current = routerConfig.getValue("routers");
            this.m_sample = Double.valueOf(routerConfig.getValue("sample").trim());
            return current.trim();
        } catch (Exception var6) {
            return null;
        }
    }

    private List<InetSocketAddress> parseSocketAddress(String content) {
        try {
            List<String> strs = Splitters.by(";").noEmptyItem().split(content);
            List<InetSocketAddress> address = new ArrayList<>();
            strs.forEach(s -> {
                List<String> items = Splitters.by(":").noEmptyItem().split(s);
                address.add(new InetSocketAddress((String) items.get(0), Integer.parseInt(items.get(1))));
            });
            return address;
        } catch (Exception var7) {
            this.m_logger.error(var7.getMessage(), var7);
            return new ArrayList();
        }
    }

    private void reconnectDefaultServer(ChannelFuture activeFuture, List<InetSocketAddress> serverAddresses) {
        try {
            int reconnectServers = this.m_activeChannelHolder.getActiveIndex();
            if (reconnectServers == -1) {
                reconnectServers = serverAddresses.size();
            }
            for (int i = 0; i < reconnectServers; ++i) {
                ChannelFuture future = this.createChannel((InetSocketAddress) serverAddresses.get(i));
                if (future != null) {
                    this.m_activeChannelHolder.setActiveFuture(future);
                    this.m_activeChannelHolder.setActiveIndex(i);
                    this.closeChannel(activeFuture);
                    break;
                }
            }
        } catch (Throwable var7) {
            this.m_logger.error(var7.getMessage(), var7);
        }
    }

    private Pair<Boolean, String> routerConfigChanged() {
        String current = this.loadServerConfig();
        return !StringUtils.isEmpty(current) && !current.equals(this.m_activeChannelHolder.getActiveServerConfig()) ? new Pair(true, current) :
            new Pair(false, current);
    }

    @Override
    public void run() {
        while (this.m_active) {
            this.m_idfactory.saveMark();
            this.checkServerChanged();
            ChannelFuture activeFuture = this.m_activeChannelHolder.getActiveFuture();
            List<InetSocketAddress> serverAddresses = this.m_activeChannelHolder.getServerAddresses();
            this.doubleCheckActiveServer(activeFuture);
            this.reconnectDefaultServer(activeFuture, serverAddresses);
            try {
                Thread.sleep(10000L);
            } catch (InterruptedException var4) {
            }
        }
    }

    private boolean shouldCheckServerConfig(int count) {
        int duration = 30;
        return count % duration == 0 || this.m_activeChannelHolder.getActiveIndex() == -1;
    }

    @Override
    public void shutdown() {
        this.m_active = false;
    }

    public static class ChannelHolder {
        private ChannelFuture m_activeFuture;
        private int m_activeIndex = -1;
        private String m_activeServerConfig;
        private List<InetSocketAddress> m_serverAddresses;
        private String m_ip;
        private boolean m_connectChanged;

        public ChannelHolder() {
        }

        public ChannelFuture getActiveFuture() {
            return this.m_activeFuture;
        }

        public int getActiveIndex() {
            return this.m_activeIndex;
        }

        public String getActiveServerConfig() {
            return this.m_activeServerConfig;
        }

        public String getIp() {
            return this.m_ip;
        }

        public List<InetSocketAddress> getServerAddresses() {
            return this.m_serverAddresses;
        }

        public boolean isConnectChanged() {
            return this.m_connectChanged;
        }

        public ChannelManager.ChannelHolder setActiveFuture(ChannelFuture activeFuture) {
            this.m_activeFuture = activeFuture;
            return this;
        }

        public ChannelManager.ChannelHolder setActiveIndex(int activeIndex) {
            this.m_activeIndex = activeIndex;
            return this;
        }

        public ChannelManager.ChannelHolder setActiveServerConfig(String activeServerConfig) {
            this.m_activeServerConfig = activeServerConfig;
            return this;
        }

        public ChannelManager.ChannelHolder setConnectChanged(boolean connectChanged) {
            this.m_connectChanged = connectChanged;
            return this;
        }

        public ChannelManager.ChannelHolder setIp(String ip) {
            this.m_ip = ip;
            return this;
        }

        public ChannelManager.ChannelHolder setServerAddresses(List<InetSocketAddress> serverAddresses) {
            this.m_serverAddresses = serverAddresses;
            return this;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("active future :").append(this.m_activeFuture.channel().remoteAddress());
            sb.append(" index:").append(this.m_activeIndex);
            sb.append(" ip:").append(this.m_ip);
            sb.append(" server config:").append(this.m_activeServerConfig);
            return sb.toString();
        }
    }

    public class ClientMessageHandler extends SimpleChannelInboundHandler<Object> {

        @Override
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
            ChannelManager.this.m_logger.info("receiver msg from server: " + o);
        }
    }
}
