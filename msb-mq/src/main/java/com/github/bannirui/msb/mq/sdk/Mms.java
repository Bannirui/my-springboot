package com.github.bannirui.msb.mq.sdk;

import com.github.bannirui.msb.mq.sdk.common.SimpleMessage;
import com.github.bannirui.msb.mq.sdk.common.MmsEnv;
import com.github.bannirui.msb.mq.sdk.common.MmsMessage;
import com.github.bannirui.msb.mq.sdk.common.MmsMessageBuilder;
import com.github.bannirui.msb.mq.sdk.config.MmsClientConfig;
import com.github.bannirui.msb.mq.sdk.consumer.ConsumerFactory;
import com.github.bannirui.msb.mq.sdk.consumer.ConsumerGroup;
import com.github.bannirui.msb.mq.sdk.consumer.MessageListener;
import com.github.bannirui.msb.mq.sdk.metrics.MmsJmxReporter;
import com.github.bannirui.msb.mq.sdk.producer.Producer;
import com.github.bannirui.msb.mq.sdk.producer.ProducerFactory;
import com.github.bannirui.msb.mq.sdk.producer.SendResponse;
import com.github.bannirui.msb.mq.sdk.producer.MmsCallBack;
import com.github.bannirui.msb.mq.sdk.zookeeper.RouterManager;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mms extends AbstractMmsService {
    private static final Logger logger = LoggerFactory.getLogger(Mms.class);
    private final MmsJmxReporter reporter;
    protected final Map<String, Properties> producerConfigCache;
    /**
     * <ul>key consumer group</ul>
     * <ul>val 监听器的订阅配置</ul>
     */
    protected final Map<String, Properties> consumerConfigCache;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(Mms::stop));
    }

    public static boolean isWorking() {
        return Mms.InstanceHolder.MMS.isRunning();
    }

    private Mms() {
        this.producerConfigCache = new HashMap<>();
        this.consumerConfigCache = new HashMap<>();
        logger.info("mms version {} initialized for {}", MmsEnv.MMS_VERSION, MmsEnv.MMS_IP);
        this.running = true;
        this.reporter = new MmsJmxReporter();
        this.reporter.start(10L, TimeUnit.SECONDS);
        logger.info("mms initilized");
    }

    /**
     * @param zkAddress mq name server, zookeeper addr
     */
    Mms(String zkAddress) {
        this();
        System.setProperty("mms_zk", zkAddress);
        logger.info("zk address has been reset to {}, the property key is {}", zkAddress, "mms_zk");
    }

    public void start() {
    }

    public void shutdown() {
        this.reporter.shutdown();
        ProducerFactory.shutdown();
        ConsumerFactory.shutdown();
        RouterManager.getInstance().shutown();
        this.running = false;
        logger.info("mms has been shutdown");
    }

    public static Producer newProducer(String topic, String name) {
        return ProducerFactory.getProducer(topic, name);
    }

    public static void stopProducer(String topic) {
        ProducerFactory.shutdown(topic);
    }

    public static void stopConsumer(String consumerGroup) {
        ConsumerFactory.shutdown(consumerGroup);
    }

    public static void stop() {
        Mms.InstanceHolder.MMS.shutdown();
    }

    public static void subscribe(String consumerGroup, MessageListener listener) {
        Mms.InstanceHolder.MMS.doSubscribe(consumerGroup, listener);
    }

    /** @deprecated */
    @Deprecated
    public static void subscribe(String consumerGroup, Set<String> tags, MessageListener listener, Properties properties) {
        Mms.InstanceHolder.MMS.doSubscribe(consumerGroup, tags, listener, properties);
    }

    public static void subscribe(String consumerGroup, Set<String> tags, MessageListener listener, Map<MmsClientConfig.CONSUMER, Object> properties) {
        Mms.InstanceHolder.MMS.cacheConsumerConfig(consumerGroup, properties);
        Mms.InstanceHolder.MMS.doSubscribe(consumerGroup, tags, listener, InstanceHolder.MMS.consumerConfigCache.get(consumerGroup));
    }

    /** @deprecated */
    @Deprecated
    public static void subscribe(String consumerGroup, MessageListener listener, Properties properties) {
        Mms.InstanceHolder.MMS.doSubscribe(consumerGroup, Sets.newHashSet(), listener, properties);
    }

    public static void subscribe(String consumerGroup, MessageListener listener, Map<MmsClientConfig.CONSUMER, Object> properties) {
        Mms.InstanceHolder.MMS.cacheConsumerConfig(consumerGroup, properties);
        Mms.InstanceHolder.MMS.doSubscribe(consumerGroup, Sets.newHashSet(), listener, InstanceHolder.MMS.consumerConfigCache.get(consumerGroup));
    }

    public static void subscribe(String consumerGroup, Set<String> tags, MessageListener listener) {
        Mms.InstanceHolder.MMS.doSubscribe(consumerGroup, tags, listener);
    }

    public static void subscribe(String consumerGroup, String tag, MessageListener listener) {
        Mms.InstanceHolder.MMS.doSubscribe(consumerGroup, Sets.newHashSet(tag), listener);
    }

    protected void doSubscribe(String consumerGroup, MessageListener listener) {
        if (!this.running) {
            logger.error("MMS is not running,will not consume message");
            return;
        }
        ConsumerFactory.getConsumer(new ConsumerGroup(consumerGroup), new Properties(), listener);
    }

    protected void doSubscribe(String consumerGroup, Set<String> tags, MessageListener listener) {
        if (!this.running) {
            logger.error("MMS is not running,will not consume message");
            return;
        }
        ConsumerFactory.getConsumer(new ConsumerGroup(consumerGroup, "CONSUMER_DEFAULT_NAME", tags), new Properties(), listener);
    }

    /**
     * @param consumerGroup consumer group
     * @param tags
     * @param listener
     * @param properties
     */
    protected void doSubscribe(String consumerGroup, Set<String> tags, MessageListener listener, Properties properties) {
        if (!this.running) {
            logger.error("MMS is not running,will not consume message");
        } else {
            ConsumerFactory.getConsumer(new ConsumerGroup(consumerGroup, "CONSUMER_DEFAULT_NAME", tags), properties, listener);
        }
    }

    public static SendResponse send(String topic, SimpleMessage simpleMessage) {
        return Mms.InstanceHolder.MMS.doSendSync(topic, simpleMessage, null);
    }

    public static SendResponse send(String topic, SimpleMessage simpleMessage, Map<MmsClientConfig.PRODUCER, Object> properties) {
        Mms.InstanceHolder.MMS.cacheProducerConfig(topic, properties);
        return Mms.InstanceHolder.MMS.doSendSync(topic, simpleMessage, InstanceHolder.MMS.producerConfigCache.get(topic));
    }

    /** @deprecated */
    @Deprecated
    public static SendResponse send(String topic, SimpleMessage simpleMessage, Properties properties) {
        return Mms.InstanceHolder.MMS.doSendSync(topic, simpleMessage, properties);
    }

    public static void sendAsync(String topic, SimpleMessage simpleMessage, MmsCallBack callBack) {
        Mms.InstanceHolder.MMS.doSendAsync(topic, simpleMessage, null, callBack);
    }

    public static void sendAsync(String topic, SimpleMessage simpleMessage, Map<MmsClientConfig.PRODUCER, Object> properties, MmsCallBack callBack) {
        Mms.InstanceHolder.MMS.cacheProducerConfig(topic, properties);
        Mms.InstanceHolder.MMS.doSendAsync(topic, simpleMessage, InstanceHolder.MMS.producerConfigCache.get(topic), callBack);
    }

    /** @deprecated */
    @Deprecated
    public static void sendAsync(String topic, SimpleMessage simpleMessage, Properties properties, MmsCallBack callBack) {
        Mms.InstanceHolder.MMS.doSendAsync(topic, simpleMessage, properties, callBack);
    }

    public static void sendOneway(String topic, SimpleMessage simpleMessage) {
        Mms.InstanceHolder.MMS.doSendOneway(topic, simpleMessage);
    }

    protected void doSendOneway(String topic, SimpleMessage simpleMessage) {
        if (!this.running) {
            logger.warn("MMS is not running,will not send message");
            return;
        }
        Producer producer = ProducerFactory.getProducer(topic);
        MmsMessage mmsMessage = MmsMessageBuilder.newInstance().buildPaylod(simpleMessage.getPayload()).buildKey(simpleMessage.getKey()).buildTags(simpleMessage.getTags()).buildDelayLevel(simpleMessage.getDelayLevel()).buildProperties(simpleMessage.getProperties()).build();
        producer.oneway(mmsMessage);
    }

    protected SendResponse doSendSync(String topic, SimpleMessage simpleMessage, Properties properties) {
        if (!this.running) {
            logger.warn("MMS is not running,will not send message");
            return SendResponse.buildErrorResult("MMS is not running");
        }
        Producer producer = ProducerFactory.getProducer(topic, properties);
        MmsMessage mmsMessage = MmsMessageBuilder.newInstance().buildPaylod(simpleMessage.getPayload()).buildKey(simpleMessage.getKey()).buildTags(simpleMessage.getTags()).buildDelayLevel(simpleMessage.getDelayLevel()).buildProperties(simpleMessage.getProperties()).build();
        return producer.syncSend(mmsMessage);
    }

    protected void doSendAsync(String topic, SimpleMessage simpleMessage, Properties properties, MmsCallBack callBack) {
        if (!this.running) {
            logger.warn("MMS is not running,will not send message");
            return;
        }
        Producer producer = ProducerFactory.getProducer(topic, properties);
        MmsMessage mmsMessage = MmsMessageBuilder.newInstance().buildPaylod(simpleMessage.getPayload()).buildKey(simpleMessage.getKey()).buildTags(simpleMessage.getTags()).buildDelayLevel(simpleMessage.getDelayLevel()).buildProperties(simpleMessage.getProperties()).build();
        producer.asyncSend(mmsMessage, callBack);
    }

    protected void cacheProducerConfig(String topic, Map<MmsClientConfig.PRODUCER, Object> properties) {
        if(MapUtils.isNotEmpty(properties) &&  !this.producerConfigCache.containsKey(topic)) {
            synchronized(Mms.class) {
                if (!this.producerConfigCache.containsKey(topic)) {
                    Properties p = new Properties();
                    properties.forEach((k, v) -> {
                        p.put(k.getKey(), v);
                    });
                    this.producerConfigCache.put(topic, p);
                }
            }
        }
    }

    /**
     * consumer group的订阅配置缓存起来
     * @param consumerGroup consumer group
     * @param properties 监听器的订阅配置
     */
    protected void cacheConsumerConfig(String consumerGroup, Map<MmsClientConfig.CONSUMER, Object> properties) {
        if(this.consumerConfigCache.containsKey(consumerGroup) || MapUtils.isEmpty(properties)) {
            return;
        }
        synchronized(Mms.class) {
            if (!this.consumerConfigCache.containsKey(consumerGroup)) {
                Properties p = new Properties();
                properties.forEach((k, v) -> p.put(k.getKey(), v));
                this.consumerConfigCache.put(consumerGroup, p);
            }
        }
    }

    private static class InstanceHolder {
        private static final Mms MMS = new Mms();

        private InstanceHolder() {
        }
    }
}
