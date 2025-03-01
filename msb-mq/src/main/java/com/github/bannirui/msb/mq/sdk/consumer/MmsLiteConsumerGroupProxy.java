package com.github.bannirui.msb.mq.sdk.consumer;

import com.github.bannirui.mms.client.consumer.KafkaLiteConsumerProxy;
import com.github.bannirui.mms.client.consumer.MessageListener;
import com.github.bannirui.mms.client.consumer.MmsConsumerProxy;
import com.github.bannirui.mms.client.consumer.RocketmqLiteConsumerProxy;
import com.github.bannirui.mms.common.BrokerType;
import com.github.bannirui.mms.metadata.ConsumerGroupMetadata;
import com.github.bannirui.mms.metadata.MmsMetadata;
import com.github.bannirui.msb.mq.sdk.zookeeper.RouterManager;
import java.util.Objects;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.zookeeper.AddWatchMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;

public class MmsLiteConsumerGroupProxy extends MmsConsumerProxy<MessageExt> {
    private String type;
    private MmsConsumerProxy colorConsumer;
    private MmsConsumerProxy defaultConsumer;
    private final Set<String> tags;
    private Properties properties;
    private ConsumerGroupMetadata consumerGroupMetadata;
    private ConsumerGroupMetadata colorMetadata;
    Watcher zkDataListener = event -> {
        MmsMetadata newMetadata = null;
        newMetadata = MmsLiteConsumerGroupProxy.this.getZkInstance().readConsumerGroupMetadata(MmsLiteConsumerGroupProxy.this.colorMetadata.getName());
        logger.info("metadata {} change notified", newMetadata.toString());
        if (!MmsLiteConsumerGroupProxy.this.colorMetadata.getClusterMetadata().getBrokerType().equals(newMetadata.getClusterMetadata().getBrokerType())) {
            logger.error("BrokerType can't be change for topic or consumergroup when running");
        } else if (MmsLiteConsumerGroupProxy.this.colorMetadata.equals(newMetadata)) {
            logger.info("ignore the change, for it's the same with before");
        } else {
            MmsMetadata oldMetadata = MmsLiteConsumerGroupProxy.this.colorMetadata;
            MmsLiteConsumerGroupProxy.this.colorMetadata = (ConsumerGroupMetadata)newMetadata;
            if (MmsLiteConsumerGroupProxy.this.changeConfigAndRestart(oldMetadata, newMetadata)) {
                logger.info("{} metadata change notify client restart", newMetadata.getName());
                ConsumerGroupMetadata newConsumerGroupMetadata = (ConsumerGroupMetadata)newMetadata;
                if (newConsumerGroupMetadata.getReleaseStatus().equals("all")) {
                    MmsLiteConsumerGroupProxy.this.consumerGroupMetadata.setReleaseStatus("all");
                }
                MmsLiteConsumerGroupProxy.this.restart();
            }
        }
    };

    public MmsLiteConsumerGroupProxy(ConsumerGroupMetadata metadata, boolean order, String instanceName, Set<String> tags, Properties properties, MessageListener listener, String type) {
        super(metadata, order, instanceName, properties, listener);
        this.consumerGroupMetadata = metadata;
        this.instanceName = instanceName;
        this.tags = tags;
        this.properties = properties;
        this.type = type;
        this.start();
    }

    @Override
    public void register(MessageListener listener) {
    }

    @Override
    protected void consumerStart() {
        String consumerName = this.consumerGroupMetadata.getName();
        String colorConsumerName = null;
        if (StringUtils.isNotBlank(MQ_COLOR)) {
            if (MQ_COLOR.equals("blue")) {
                colorConsumerName = "_BLUE_" + consumerName;
            }
            if (MQ_COLOR.equals("green")) {
                colorConsumerName = "_GREEN_" + consumerName;
            }
        }
        if (this.type.equals(BrokerType.ROCKETMQ.getName())) {
            if (!StringUtils.isBlank(this.consumerGroupMetadata.getReleaseStatus()) && !this.consumerGroupMetadata.getReleaseStatus().equals("all")) {
                this.colorMetadata = RouterManager.getZkInstance().readConsumerGroupMetadata(colorConsumerName);
                if (null != this.colorMetadata) {
                    this.colorConsumer = new RocketmqLiteConsumerProxy(this.colorMetadata, super.isOrderly, this.colorMetadata.getName(), this.tags, this.properties, this.listener);
                }
                if (this.colorMetadata.getReleaseStatus().equals(MQ_COLOR)) {
                    this.defaultConsumer = new RocketmqLiteConsumerProxy(this.consumerGroupMetadata, super.isOrderly, this.consumerGroupMetadata.getName(), this.tags, this.properties, this.listener);
                }
            } else {
                this.defaultConsumer = new RocketmqLiteConsumerProxy(this.consumerGroupMetadata, super.isOrderly, this.consumerGroupMetadata.getName(), this.tags, this.properties, this.listener);
            }
        } else if (!StringUtils.isBlank(this.consumerGroupMetadata.getReleaseStatus()) && !this.consumerGroupMetadata.getReleaseStatus().equals("all")) {
            this.colorMetadata = RouterManager.getZkInstance().readConsumerGroupMetadata(colorConsumerName);
            if (null != this.colorMetadata) {
                this.colorConsumer = new KafkaLiteConsumerProxy(this.colorMetadata, super.isOrderly, this.colorMetadata.getName(), this.properties, this.listener);
            }
            if (this.colorMetadata.getReleaseStatus().equals(MQ_COLOR)) {
                this.defaultConsumer = new KafkaLiteConsumerProxy(this.consumerGroupMetadata, super.isOrderly, this.consumerGroupMetadata.getName(), this.properties, this.listener);
            }
        } else {
            this.defaultConsumer = new KafkaLiteConsumerProxy(this.consumerGroupMetadata, super.isOrderly, this.consumerGroupMetadata.getName(), this.properties, this.listener);
        }
        this.registWatcher();
    }

    protected void decryptMsgBodyIfNecessary(MessageExt msg) {
    }

    @Override
    protected void consumerShutdown() {
        if (null != this.colorConsumer) {
            this.colorConsumer.shutdown();
        }
        if (null != this.defaultConsumer) {
            this.defaultConsumer.shutdown();
        }

    }

    @Override
    public void addUserDefinedProperties(Properties properties) {
    }

    @Override
    public void restart() {
        if (null != this.colorConsumer) {
            this.colorConsumer.shutdown();
        }
        if (null != this.defaultConsumer) {
            this.defaultConsumer.shutdown();
        }
        try {
            Thread.sleep((new Random()).nextInt(1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.consumerStart();
    }

    @Override
    public boolean changeConfigAndRestart(MmsMetadata oldMetadata, MmsMetadata newMetadata) {
        if (oldMetadata.isGatedLaunch() ^ newMetadata.isGatedLaunch()) {
            return true;
        } else {
            ConsumerGroupMetadata oldConsumerMeta = (ConsumerGroupMetadata)oldMetadata;
            ConsumerGroupMetadata newConsumerMeta = (ConsumerGroupMetadata)newMetadata;
            return !Objects.equals(oldConsumerMeta.getReleaseStatus(), newConsumerMeta.getReleaseStatus());
        }
    }

    public void registWatcher() {
        try {
            this.getZkInstance().addWatch(this.colorMetadata.getMmsPath(), this.zkDataListener, AddWatchMode.PERSISTENT);
        } catch (KeeperException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
