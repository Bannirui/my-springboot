package com.github.bannirui.msb.mq.sdk;

import com.github.bannirui.msb.mq.sdk.common.SimpleMessage;
import com.github.bannirui.msb.mq.sdk.config.MmsClientConfig;
import com.github.bannirui.msb.mq.sdk.consumer.MessageListener;
import com.github.bannirui.msb.mq.sdk.producer.SendResponse;
import com.github.bannirui.msb.mq.sdk.producer.MmsCallBack;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * 对mq功能的封装 屏蔽了mq类型等平台细节
 * <ul>
 *     <li>元数据信息</li>
 *     <li>发送功能</li>
 *     <li>订阅功能</li>
 * </ul>
 */
public class MmsMsbImpl {
    private final Mms mms;

    /**
     * @param zkAddress mq name server, the zookeeper
     */
    public MmsMsbImpl(String zkAddress) {
        this.mms = new Mms(zkAddress);
    }

    public void stop() {
        this.mms.shutdown();
    }

    public SendResponse send(String topic, SimpleMessage simpleMessage) {
        return this.mms.doSendSync(topic, simpleMessage, null);
    }

    /** @deprecated */
    @Deprecated
    public SendResponse send(String topic, SimpleMessage simpleMessage, Properties properties) {
        return this.mms.doSendSync(topic, simpleMessage, properties);
    }

    public SendResponse send(String topic, SimpleMessage simpleMessage, Map<MmsClientConfig.PRODUCER, Object> properties) {
        this.mms.cacheProducerConfig(topic, properties);
        return this.mms.doSendSync(topic, simpleMessage, this.mms.producerConfigCache.get(topic));
    }

    public void asyncSend(String topic, SimpleMessage simpleMessage, MmsCallBack callBack) {
        this.mms.doSendAsync(topic, simpleMessage, null, callBack);
    }

    /** @deprecated */
    @Deprecated
    public void asyncSend(String topic, SimpleMessage simpleMessage, Properties properties, MmsCallBack callBack) {
        this.mms.doSendAsync(topic, simpleMessage, properties, callBack);
    }

    public void asyncSend(String topic, SimpleMessage simpleMessage, Map<MmsClientConfig.PRODUCER, Object> properties, MmsCallBack callBack) {
        this.mms.cacheProducerConfig(topic, properties);
        this.mms.doSendAsync(topic, simpleMessage, this.mms.producerConfigCache.get(topic), callBack);
    }

    public void onewaySend(String topic, SimpleMessage simpleMessage) {
        this.mms.doSendOneway(topic, simpleMessage);
    }

    public void subscribe(String consumerGroup, MessageListener listener) {
        this.mms.doSubscribe(consumerGroup, listener);
    }

    public void subscribe(String consumerGroup, String tags, MessageListener listener) {
        this.mms.doSubscribe(consumerGroup, Sets.newHashSet(tags), listener);
    }

    public void subscribe(String consumerGroup, Set<String> tags, MessageListener listener) {
        this.mms.doSubscribe(consumerGroup, tags, listener);
    }

    /** @deprecated */
    @Deprecated
    public void subscribe(String consumerGroup, Set<String> tags, MessageListener listener, Properties properties) {
        this.mms.doSubscribe(consumerGroup, tags, listener, properties);
    }

    public void subscribe(String consumerGroup, Set<String> tags, MessageListener listener, Map<MmsClientConfig.CONSUMER, Object> properties) {
        this.mms.cacheConsumerConfig(consumerGroup, properties);
        this.mms.doSubscribe(consumerGroup, tags, listener, this.mms.consumerConfigCache.get(consumerGroup));
    }

    /** @deprecated */
    @Deprecated
    public void subscribe(String consumerGroup, MessageListener listener, Properties properties) {
        this.mms.doSubscribe(consumerGroup, Sets.newHashSet(), listener, properties);
    }

    public void subscribe(String consumerGroup, MessageListener listener, Map<MmsClientConfig.CONSUMER, Object> properties) {
        this.mms.cacheConsumerConfig(consumerGroup, properties);
        this.mms.doSubscribe(consumerGroup, Sets.newHashSet(), listener, this.mms.consumerConfigCache.get(consumerGroup));
    }
}
