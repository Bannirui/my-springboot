package com.github.bannirui.msb.mq.sdk.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.bannirui.msb.mq.sdk.Mms;
import com.github.bannirui.msb.mq.sdk.MmsProxy;
import com.github.bannirui.msb.mq.sdk.common.SLA;
import com.github.bannirui.msb.mq.sdk.common.SimpleMessage;
import com.github.bannirui.msb.mq.sdk.common.StatisticLoggerType;
import com.github.bannirui.msb.mq.sdk.common.MmsException;
import com.github.bannirui.msb.mq.sdk.message.statistic.StatisticsInfo;
import com.github.bannirui.msb.mq.sdk.metadata.ConsumerGroupMetadata;
import com.github.bannirui.msb.mq.sdk.metadata.MmsMetadata;
import com.github.bannirui.msb.mq.sdk.metrics.MmsConsumerMetrics;
import java.util.Objects;
import java.util.Properties;
import java.util.Random;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 消费者代理 屏蔽中间件差异和细节
 * @param <T> 消息体类型
 */
public abstract class MmsConsumerProxy<T> extends MmsProxy<MmsConsumerMetrics> implements Consumer {
    protected static final Logger logger = LoggerFactory.getLogger(MmsConsumerProxy.class);
    protected MessageListener listener;
    protected Properties customizedProperties;
    protected static String MQ_TAG;
    protected static String MQ_COLOR;

    static {
        String mmsRewrite = System.getProperty("mmsRewrite");
        String mqTag = System.getProperty("mqTag");
        String mqColor = System.getProperty("mqColor");
        if (StringUtils.isNotBlank(mqTag) && StringUtils.isNotBlank(mqColor)) {
            throw MmsException.DEPLOY_EXCEPTION;
        } else if (StringUtils.isNotBlank(mqTag) && StringUtils.isBlank(mmsRewrite)) {
            throw MmsException.MQ_TAG_EXCEPTION;
        } else {
            MQ_TAG = mqTag == null ? "" : mqTag;
            MQ_COLOR = mqColor == null ? "" : mqColor;
        }
    }
    protected boolean msgFilter(String mqTagValue) {
        return StringUtils.isBlank(MQ_TAG) && StringUtils.isBlank(mqTagValue) || MQ_TAG.equals(mqTagValue);
    }

    protected boolean msgFilterByColor(String mqColorValue) {
        String releaseColor = ((ConsumerGroupMetadata)this.metadata).getReleaseStatus();
        if (null != releaseColor && !releaseColor.equals("all")) {
            if (releaseColor.equals("default") && StringUtils.isBlank(mqColorValue)) {
                return true;
            } else {
                return StringUtils.isNotBlank(mqColorValue) && (releaseColor.equals("blue") || releaseColor.equals("green")) && MQ_COLOR.equals(mqColorValue);
            }
        } else {
            return true;
        }
    }

    public MmsConsumerProxy(MmsMetadata metadata, SLA sla, String name, Properties properties, MessageListener listener) {
        super(metadata, sla, new MmsConsumerMetrics(metadata.getName(), name));
        this.listener = listener;
        this.customizedProperties = properties;
    }

    public void start() {
        if (this.running) {
            logger.warn("Consumer {} has been started, can not be started again", this.instanceName);
        } else {
            this.consumerStart();
            super.start();
            this.register(this.listener);
        }
    }

    protected abstract void consumerStart();

    protected abstract void decryptMsgBodyIfNecessary(T msg);

    public void restart() {
        logger.info("consumer {} begin to restart", this.instanceName);
        this.shutdown();
        if (StringUtils.isNotBlank(((ConsumerGroupMetadata)this.metadata).getReleaseStatus()) && !((ConsumerGroupMetadata)this.metadata).getReleaseStatus().equals("all") && !this.metadata.getName().startsWith("_BLUE_") && !this.metadata.getName().startsWith("_GREEN_")) {
            ConsumerFactory.rewriteConsumerGroup((ConsumerGroupMetadata)this.metadata);
            ConsumerFactory.add(this.metadata.getName(), this);
        }
        try {
            Thread.sleep((new Random()).nextInt(1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.start();
    }

    public boolean changeConfigAndRestart(MmsMetadata oldMetadata, MmsMetadata newMetadata) {
        if (oldMetadata.isGatedLaunch() ^ newMetadata.isGatedLaunch()) {
            return true;
        }
        ConsumerGroupMetadata oldConsumerMeta = (ConsumerGroupMetadata)oldMetadata;
        ConsumerGroupMetadata newConsumerMeta = (ConsumerGroupMetadata)newMetadata;
        return !Objects.equals(oldConsumerMeta.getClusterMetadata(), newConsumerMeta.getClusterMetadata()) || !Objects.equals(oldConsumerMeta.getBindingTopic(), newConsumerMeta.getBindingTopic()) || !Objects.equals(oldConsumerMeta.getBroadcast(), newConsumerMeta.getBroadcast()) || !Objects.equals(oldConsumerMeta.getConsumeFrom(), newConsumerMeta.getConsumeFrom()) || !Objects.equals(oldConsumerMeta.getIsEncrypt(), newConsumerMeta.getIsEncrypt());
    }

    public void shutdown() {
        if (!this.running) {
            logger.warn("Consumer {} has been shutdown, can not be shutdown again", this.instanceName);
            return;
        }
        this.running = false;
        super.shutdown();
        this.consumerShutdown();
        ConsumerFactory.recycle(this.metadata.getName(), this.instanceName);
        logger.info("Consumer {} shutdown", this.instanceName);
    }

    public void statistics() {
        if (this.running && !this.isStatistic(this.mmsMetrics.getClientName())) {
            if (!StringUtils.isEmpty(this.metadata.getStatisticsLogger()) && !StatisticLoggerType.MESSAGE.getName().equalsIgnoreCase(this.metadata.getStatisticsLogger())) {
                logger.info(this.mmsMetrics.reportLogStatistics());
            } else {
                StatisticsInfo info = this.mmsMetrics.reportMessageStatistics();
                Mms.sendOneway("statistic_topic_consumerinfo", new SimpleMessage(JSON.toJSONBytes(info, new SerializerFeature[0])));
            }
        }
    }

    protected abstract void consumerShutdown();

    public abstract void addUserDefinedProperties(Properties properties);
}
