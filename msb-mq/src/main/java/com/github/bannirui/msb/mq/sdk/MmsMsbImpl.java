package com.github.bannirui.msb.mq.sdk;

import com.github.bannirui.mms.client.Mms;
import com.github.bannirui.mms.client.common.SimpleMessage;
import com.github.bannirui.mms.client.config.MmsClientConfig;
import com.github.bannirui.mms.client.consumer.MessageListener;
import com.github.bannirui.mms.client.producer.SendCallback;
import com.github.bannirui.mms.client.producer.SendResult;
import com.github.bannirui.mms.common.MmsConst;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.collections4.MapUtils;

/**
 * 对mq功能的封装 屏蔽了mq类型等平台细节
 * <ul>
 *     <li>元数据信息</li>
 *     <li>发送功能</li>
 *     <li>订阅功能</li>
 * </ul>
 */
public class MmsMsbImpl {

    private final Map<String, Properties> producerConfigCache;
    /**
     * <ul>key consumer group</ul>
     * <ul>val 监听器的订阅配置</ul>
     */
    private final Map<String, Properties> consumerConfigCache;

    /**
     * @param zkAddress mq name server, the zookeeper
     */
    public MmsMsbImpl(String zkAddress) {
        this.producerConfigCache = new HashMap<>();
        this.consumerConfigCache = new HashMap<>();
        /**
         * 在{@link Mms}中实例化{@link com.github.bannirui.mms.zookeeper.MmsZkClient}时会去读zk的配
         */
        System.setProperty(MmsConst.ZK.MMS_STARTUP_PARAM, zkAddress);
    }

    private void cacheProducerConfig(String topic, Map<MmsClientConfig.PRODUCER, Object> properties) {
        if(MapUtils.isEmpty(properties) || this.producerConfigCache.containsKey(topic))  return;
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

    /**
     * consumer group的订阅配置缓存起来
     * @param consumerGroup consumer group
     * @param properties 监听器的订阅配置
     */
    private void cacheConsumerConfig(String consumerGroup, Map<MmsClientConfig.CONSUMER, Object> properties) {
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

    public void stop() {
        // TODO: 2025/3/2
    }

    public SendResult send(String topic, SimpleMessage simpleMessage) {
        return Mms.send(topic, simpleMessage);
    }

    /** @deprecated */
    @Deprecated
    public SendResult send(String topic, SimpleMessage simpleMessage, Properties properties) {
        return Mms.send(topic, simpleMessage, properties);
    }

    public SendResult send(String topic, SimpleMessage simpleMessage, Map<MmsClientConfig.PRODUCER, Object> properties) {
        this.cacheProducerConfig(topic, properties);
        return Mms.send(topic, simpleMessage, this.producerConfigCache.get(topic));
    }

    public void asyncSend(String topic, SimpleMessage simpleMessage, SendCallback callBack) {
        Mms.sendAsync(topic, simpleMessage, callBack);
    }

    /** @deprecated */
    @Deprecated
    public void asyncSend(String topic, SimpleMessage simpleMessage, Properties properties, SendCallback callBack) {
        Mms.sendAsync(topic, simpleMessage, properties, callBack);
    }

    public void asyncSend(String topic, SimpleMessage simpleMessage, Map<MmsClientConfig.PRODUCER, Object> properties, SendCallback callBack) {
        this.cacheProducerConfig(topic, properties);
        Mms.sendAsync(topic, simpleMessage, this.producerConfigCache.get(topic), callBack);
    }

    public void onewaySend(String topic, SimpleMessage simpleMessage) {
        Mms.sendOneway(topic, simpleMessage);
    }

    public void subscribe(String consumerGroup, MessageListener listener) {
        Mms.subscribe(consumerGroup, listener);
    }

    public void subscribe(String consumerGroup, String tags, MessageListener listener) {
        Mms.subscribe(consumerGroup, Sets.newHashSet(tags), listener);
    }

    public void subscribe(String consumerGroup, Set<String> tags, MessageListener listener) {
        Mms.subscribe(consumerGroup, tags, listener);
    }

    /** @deprecated */
    @Deprecated
    public void subscribe(String consumerGroup, Set<String> tags, MessageListener listener, Properties properties) {
        Mms.subscribe(consumerGroup, tags, listener, properties);
    }

    public void subscribe(String consumerGroup, Set<String> tags, MessageListener listener, Map<MmsClientConfig.CONSUMER, Object> properties) {
        this.cacheConsumerConfig(consumerGroup, properties);
        Mms.subscribe(consumerGroup, tags, listener, this.consumerConfigCache.get(consumerGroup));
    }

    /** @deprecated */
    @Deprecated
    public void subscribe(String consumerGroup, MessageListener listener, Properties properties) {
        Mms.subscribe(consumerGroup, Sets.newHashSet(), listener, properties);
    }

    public void subscribe(String consumerGroup, MessageListener listener, Map<MmsClientConfig.CONSUMER, Object> properties) {
        this.cacheConsumerConfig(consumerGroup, properties);
        Mms.subscribe(consumerGroup, Sets.newHashSet(), listener, this.consumerConfigCache.get(consumerGroup));
    }
}
