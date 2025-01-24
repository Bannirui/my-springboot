package com.github.bannirui.msb.mq.sdk.zookeeper;

import com.github.bannirui.msb.mq.sdk.common.MmsException;
import com.github.bannirui.msb.mq.sdk.metadata.ClusterMetadata;
import com.github.bannirui.msb.mq.sdk.metadata.ConsumerGroupMetadata;
import com.github.bannirui.msb.mq.sdk.metadata.TopicMetadata;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouterManager {
    private static final Logger logger = LoggerFactory.getLogger(RouterManager.class);
    /**
     * zk客户端
     */
    private MmsZookeeper zkClient;

    private RouterManager() {
        // msb配置mms.nameServerAddres zk注册中心
        String mmsServer = System.getProperty("mms_zk");
        String effectiveParam = "MMS_STARTUP_PARAM";
        if (StringUtils.isEmpty(mmsServer)) {
            mmsServer = System.getenv("MMS_ZK");
            effectiveParam = "MMS_ZK_ENV";
        }
        if (StringUtils.isEmpty(mmsServer)) {
            String env = System.getProperty("env");
            if (StringUtils.isEmpty(env)) {
                throw MmsException.NO_ZK_EXCEPTION;
            }
            mmsServer = ZkConfig.getZkAddress(env);
            effectiveParam = "ENV";
        }
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            this.zkClient=new MmsZookeeper(mmsServer, 20_000, watchedEvent -> {
                Watcher.Event.KeeperState state = watchedEvent.getState();
                Watcher.Event.EventType type = watchedEvent.getType();
                if (Watcher.Event.KeeperState.SyncConnected == state) {
                    if (Watcher.Event.EventType.None == type) {
                        logger.info("zookeeper connect success");
                        countDownLatch.countDown();
                    }
                }
            });
            countDownLatch.await();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        logger.info("zk connected to {} by parameter {}", mmsServer, effectiveParam);
    }

    public static RouterManager getInstance() {
        return RouterManager.InstanceHolder.routerManager;
    }

    public static MmsZookeeper getZkInstance() {
        return RouterManager.InstanceHolder.routerManager.zkClient;
    }

    public void shutown() {
        try {
            this.zkClient.close();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        logger.info("routerManager shutdown");
    }

    /**
     * cluster数据写到zk
     */
    public void writeClusterMetadata(ClusterMetadata clusterMetadata) {
        this.zkClient.writeClusterMetadata(clusterMetadata);
    }

    /**
     * topic数据写到zk
     */
    public void writeTopicMetadata(TopicMetadata topicMetadata) {
        this.zkClient.writeTopicMetadata(topicMetadata);
    }

    /**
     * consumer group数据写到zk
     */
    public void writeConsumerGroupMetadata(ConsumerGroupMetadata cgMetadata) {
        this.zkClient.writeConsumerGroupMetadata(cgMetadata);
    }

    /**
     * 删除topic数据
     */
    public void deleteTopic(String topicName) {
        this.zkClient.deleteTopic(topicName);
    }

    /**
     * 删除consumer group数据
     */
    public void deleteConsumerGroup(String consumerGroupName) {
        this.zkClient.deleteConsumerGroup(consumerGroupName);
    }

    /**
     * 删除cluster数据
     */
    public void deleteCluster(String clusterName) {
        this.zkClient.deleteCluster(clusterName);
    }

    private static class InstanceHolder {
        private static final RouterManager routerManager = new RouterManager();

        private InstanceHolder() {
        }
    }
}
