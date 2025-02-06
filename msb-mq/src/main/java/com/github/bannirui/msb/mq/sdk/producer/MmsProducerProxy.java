package com.github.bannirui.msb.mq.sdk.producer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.bannirui.msb.mq.sdk.Mms;
import com.github.bannirui.msb.mq.sdk.MmsProxy;
import com.github.bannirui.msb.mq.sdk.common.MmsException;
import com.github.bannirui.msb.mq.sdk.common.SLA;
import com.github.bannirui.msb.mq.sdk.common.SimpleMessage;
import com.github.bannirui.msb.mq.sdk.common.StatisticLoggerType;
import com.github.bannirui.msb.mq.sdk.message.statistic.StatisticsInfo;
import com.github.bannirui.msb.mq.sdk.metadata.MmsMetadata;
import com.github.bannirui.msb.mq.sdk.metadata.TopicMetadata;
import com.github.bannirui.msb.mq.sdk.metrics.MmsProducerMetrics;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Properties;
import java.util.Random;

public abstract class MmsProducerProxy extends MmsProxy<MmsProducerMetrics> implements Producer {
    private static final Logger logger= LoggerFactory.getLogger(MmsProducerProxy.class);
    Properties customizedProperties;
    protected static final String MQ_TAG;
    protected static final String MQ_COLOR;
    protected static final String MMS_ENABLE_RETRY;
    protected static final String MMS_TIMEOUT_MS;

    static {
        MQ_TAG = System.getProperty("mqTag");
        MQ_COLOR = System.getProperty("mqColor");
        MMS_ENABLE_RETRY = System.getProperty("mmsEnableRetry");
        MMS_TIMEOUT_MS = System.getProperty("mmsTimeoutMillis");
        if (StringUtils.isNotBlank(MQ_TAG) && StringUtils.isNotBlank(MQ_COLOR)) {
            throw MmsException.DEPLOY_EXCEPTION;
        }
    }

    public MmsProducerProxy(MmsMetadata metadata, SLA sla, String name) {
        super(metadata, sla, new MmsProducerMetrics(metadata.getName(), name));
    }

    public MmsProducerProxy(MmsMetadata metadata, SLA sla, String name, Properties properties) {
        super(metadata, sla, new MmsProducerMetrics(metadata.getName(), name));
        this.customizedProperties = properties;
    }

    public void start() {
        if (this.running) {
            logger.warn("producer {} has been started,can't be start again", this.instanceName);
            return;
        }
        this.startProducer();
        super.start();
        logger.info("Producer {} has been started", this.instanceName);
    }

    public abstract void startProducer();

    public abstract void shutdownProducer();

    public boolean changeConfigAndRestart(MmsMetadata oldMetadata, MmsMetadata newMetadata) {
        if (oldMetadata.isGatedLaunch() ^ newMetadata.isGatedLaunch()) {
            return true;
        } else {
            TopicMetadata oldConsumerMeta = (TopicMetadata)oldMetadata;
            TopicMetadata newConsumerMeta = (TopicMetadata)newMetadata;
            return !Objects.equals(oldConsumerMeta.getClusterMetadata(), newConsumerMeta.getClusterMetadata()) || !Objects.equals(oldConsumerMeta.getIsEncrypt(), newConsumerMeta.getIsEncrypt());
        }
    }

    public void statistics() {
        if (this.running && !this.isStatistic(this.mmsMetrics.getClientName())) {
            if (!StringUtils.isEmpty(this.metadata.getStatisticsLogger()) && !StatisticLoggerType.MESSAGE.getName().equalsIgnoreCase(this.metadata.getStatisticsLogger())) {
                logger.info(this.mmsMetrics.reportLogStatistics());
            } else {
                StatisticsInfo info = this.mmsMetrics.reportMessageStatistics();
                Mms.sendOneway("statistic_topic_producerinfo", new SimpleMessage(JSON.toJSONBytes(info, new SerializerFeature[0])));
            }
        }
    }

    public void shutdown() {
        if (!this.running) {
            logger.warn("Producer {} has been shutdown,can't be shutdown again", this.instanceName);
            return;
        }
        this.running = false;
        super.shutdown();
        this.shutdownProducer();
        ProducerFactory.recycle(this.metadata.getName(), this.instanceName);
        logger.info("Producer {} hast been shutdown", this.instanceName);
    }

    public void restart() {
        logger.info("Producer {} begin to restart", this.instanceName);
        this.shutdown();
        try {
            Thread.sleep((new Random()).nextInt(1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected MmsProducerProxy.ResetTimeoutAndRetries resetTimeoutAndRetries(int customTimeout, int customRetries) {
        if (StringUtils.isNotBlank(MMS_ENABLE_RETRY) && Boolean.parseBoolean(MMS_ENABLE_RETRY)) {
            int totalTimeout = customTimeout * customRetries;
            int resetTimeout = StringUtils.isNotBlank(MMS_TIMEOUT_MS) ? Integer.parseInt(MMS_TIMEOUT_MS) : 500;
            return new MmsProducerProxy.ResetTimeoutAndRetries(Math.min(resetTimeout, customTimeout), Math.max(totalTimeout / resetTimeout, customRetries));
        } else {
            return null;
        }
    }

    protected static class ResetTimeoutAndRetries {
        int resetTimeout;
        int resetRetries;

        public ResetTimeoutAndRetries(int resetTimeout, int resetRetries) {
            this.resetTimeout = resetTimeout;
            this.resetRetries = resetRetries;
        }

        public int getResetTimeout() {
            return this.resetTimeout;
        }

        public int getResetRetries() {
            return this.resetRetries;
        }
    }
}
