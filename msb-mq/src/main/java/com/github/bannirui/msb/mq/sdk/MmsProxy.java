package com.github.bannirui.msb.mq.sdk;

import com.github.bannirui.msb.mq.sdk.common.SLA;
import com.github.bannirui.msb.mq.sdk.common.MmsType;
import com.github.bannirui.msb.mq.sdk.metadata.MmsMetadata;
import com.github.bannirui.msb.mq.sdk.metrics.MmsMetrics;
import com.github.bannirui.msb.mq.sdk.utils.Utils;
import com.github.bannirui.msb.mq.sdk.zookeeper.RouterManager;
import com.github.bannirui.msb.mq.sdk.zookeeper.MmsZookeeper;
import org.apache.zookeeper.AddWatchMode;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MmsProxy<K extends MmsMetrics> extends AbstractMmsService {
    private static final Logger logger = LoggerFactory.getLogger(MmsProxy.class);
    protected MmsMetadata metadata;
    public String instanceName;
    protected K mmsMetrics;
    public String proxyName;
    protected SLA sla;

    /**
     * zk监听器
     */
    Watcher zkDataListener = event -> {
        MmsMetadata newMetadata = null;
        if (MmsProxy.this.metadata.getType().equals(MmsType.TOPIC.getName())) {
            newMetadata = MmsProxy.this.getZkInstance().readTopicMetadata(MmsProxy.this.metadata.getName());
        } else {
            newMetadata = MmsProxy.this.getZkInstance().readConsumerGroupMetadata(MmsProxy.this.metadata.getName());
        }
        logger.info("metadata {} change notified", newMetadata.toString());
        if (!MmsProxy.this.metadata.getClusterMetadata().getBrokerType().equals(((MmsMetadata)newMetadata).getClusterMetadata().getBrokerType())) {
            logger.error("BrokerType can't be change for topic or consumergroup when running");
        } else if (MmsProxy.this.metadata.equals(newMetadata)) {
            logger.info("ignore the change, for it's the same with before");
        } else {
            MmsMetadata oldMetadata = MmsProxy.this.metadata;
            MmsProxy.this.metadata = newMetadata;
            if (MmsProxy.this.changeConfigAndRestart(oldMetadata, newMetadata)) {
                logger.info("{} metadata change notify client restart", newMetadata.getName());
                MmsProxy.this.restart();
            }
        }
    };

    public MmsProxy(MmsMetadata metadata, SLA sla, K metrics) {
        this.metadata = metadata;
        this.sla = sla;
        this.mmsMetrics = metrics;
    }

    public void registWatcher() {
        try {
            this.getZkInstance().addWatch(this.metadata.getMmsPath(), this.zkDataListener, AddWatchMode.PERSISTENT);
        } catch (KeeperException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void registerName() {
        // 临时节点
        if (!this.isStatistic(this.metadata.getName())) {
            this.proxyName = Utils.buildPath(this.metadata.getMmsPath(), Utils.buildName(this.instanceName));
            try {
                this.getZkInstance().create(this.proxyName, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            } catch (KeeperException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public void unregisterWatcher() {
        try {
            this.getZkInstance().removeWatches(this.metadata.getMmsClusterPath(), this.zkDataListener, Watcher.WatcherType.Any, true);
            this.getZkInstance().removeWatches(this.metadata.getMmsPath(), this.zkDataListener, Watcher.WatcherType.Any, true);
        } catch (InterruptedException | KeeperException e) {
            throw new RuntimeException(e);
        }
    }

    public MmsZookeeper getZkInstance() {
        return RouterManager.getZkInstance();
    }

    public boolean isStatistic(String name) {
        return "statistic_ping_consumer".equalsIgnoreCase(name) || "statistic_ping_topic".equalsIgnoreCase(name) || "statistic_consumer_consumerInfo".equalsIgnoreCase(name) || "statistic_consumer_kafka_consumerinfo".equalsIgnoreCase(name) || "statistic_consumer_kafka_producerinfo".equalsIgnoreCase(name) || "statistic_consumer_kafka_producerinfo".equalsIgnoreCase(name) || "statistic_consumer_producerInfo".equalsIgnoreCase(name) || "statistic_topic_consumerinfo".equalsIgnoreCase(name) || "statistic_topic_kafka_consumerinfo".equalsIgnoreCase(name) || "statistic_topic_kafka_producerinfo".equalsIgnoreCase(name) || "statistic_topic_producerinfo".equalsIgnoreCase(name);
    }

    public void unregisterName() {
        if (!this.isStatistic(this.metadata.getName())) {
            try {
                this.getZkInstance().delete(this.proxyName, -1);
            } catch (InterruptedException | KeeperException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void restart() {
    }

    public abstract boolean changeConfigAndRestart(MmsMetadata oldMetadata, MmsMetadata newMetadata);

    public void start() {
        this.registWatcher();
        this.registerName();
        this.running = true;
    }

    public void shutdown() {
        this.running = false;
        this.unregisterName();
        this.unregisterWatcher();
    }

    public MmsMetadata getMetadata() {
        return this.metadata;
    }
}
