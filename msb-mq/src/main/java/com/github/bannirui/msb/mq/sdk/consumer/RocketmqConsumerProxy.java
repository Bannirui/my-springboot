package com.github.bannirui.msb.mq.sdk.consumer;

import com.github.bannirui.msb.mq.sdk.common.ConsumeFrom;
import com.github.bannirui.msb.mq.sdk.common.SLA;
import com.github.bannirui.msb.mq.sdk.common.MmsEnv;
import com.github.bannirui.msb.mq.sdk.common.MmsException;
import com.github.bannirui.msb.mq.sdk.config.MmsClientConfig;
import com.github.bannirui.msb.mq.sdk.crypto.MMSCryptoManager;
import com.github.bannirui.msb.mq.sdk.metadata.ConsumerGroupMetadata;
import java.time.Duration;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.protocol.heartbeat.MessageModel;

public class RocketmqConsumerProxy extends MmsConsumerProxy<MessageExt> {
    private DefaultMQPushConsumer consumer;
    private final Set<String> tags;

    public RocketmqConsumerProxy(ConsumerGroupMetadata metadata, SLA sla, String instanceName, Set<String> tags, Properties properties, MessageListener listener) {
        super(metadata, sla, instanceName, properties, listener);
        this.instanceName = instanceName;
        this.tags = tags;
        this.start();
    }

    protected void consumerStart() {
        this.consumer = new DefaultMQPushConsumer(this.metadata.getName());
        long now = System.currentTimeMillis();
        if (this.metadata.isGatedLaunch()) {
            this.consumer.setNamesrvAddr(this.metadata.getGatedCluster().getBootAddr());
            this.consumer.setClientIP("consumer-client-id-" + this.metadata.getGatedCluster().getClusterName() + "-" + MmsEnv.MMS_IP + "-" + now);
        } else {
            this.consumer.setNamesrvAddr(this.metadata.getClusterMetadata().getBootAddr());
            this.consumer.setClientIP("consumer-client-id-" + this.metadata.getClusterMetadata().getClusterName() + "-" + MmsEnv.MMS_IP + "-" + now);
        }

        this.consumer.setVipChannelEnabled(false);
        String bindingTopic = ((ConsumerGroupMetadata)this.metadata).getBindingTopic();
        String consumeFrom = ((ConsumerGroupMetadata)this.metadata).getConsumeFrom();
        String broadCast = ((ConsumerGroupMetadata)this.metadata).getBroadcast();
        if (((ConsumerGroupMetadata)this.metadata).needSuspend()) {
            logger.error("consumer {} suspend is on, please set it to off first", this.metadata.getName());
            throw new RuntimeException(String.format("consumer %s suspend is on, please set it to off first", this.metadata.getName()));
        } else {
            if (StringUtils.isEmpty(consumeFrom)) {
                this.consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
            } else if (ConsumeFrom.EARLIEST.getName().equalsIgnoreCase(consumeFrom)) {
                this.consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
            } else {
                this.consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
            }
            if (!StringUtils.isEmpty(broadCast) && Boolean.parseBoolean(broadCast)) {
                this.consumer.setMessageModel(MessageModel.BROADCASTING);
            }
            if (this.customizedProperties != null) {
                this.addUserDefinedProperties(this.customizedProperties);
            }
            logger.info("consumer {} start with param {}", this.instanceName, this.buildConsumerInfo(this.consumer));
            try {
                if (CollectionUtils.isNotEmpty(this.tags)) {
                    String combinedTags = StringUtils.join(this.tags, "||");
                    logger.info("consumer {} start with tags {}", this.instanceName, combinedTags);
                    this.consumer.subscribe(bindingTopic, combinedTags);
                } else {
                    this.consumer.subscribe(bindingTopic, "*");
                }

            } catch (MQClientException e) {
                logger.error("RocketMQConsumer register {} error", bindingTopic, e);
                throw MmsException.CONSUMER_START_EXCEPTION;
            }
        }
    }

    private String buildConsumerInfo(DefaultMQPushConsumer consumer) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" clientIP: " + consumer.getClientIP());
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append(" nameSrv: " + consumer.getNamesrvAddr());
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append(" batchSize: " + consumer.getPullBatchSize());
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append(" consumeThreadMin: " + consumer.getConsumeThreadMin());
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append(" consumeThreadMax: " + consumer.getConsumeThreadMax());
        return stringBuilder.toString();
    }

    public void register(MessageListener listener) {
        if (this.sla.isOrderly) {
            this.consumer.registerMessageListener((MessageListenerOrderly) (msgs, context) -> {
                msgs = msgs.stream().filter((msgx) -> RocketmqConsumerProxy.this.msgFilter(RocketmqConsumerProxy.this.getMqTagValue(msgx))).filter((msgx) -> RocketmqConsumerProxy.this.msgFilterByColor(RocketmqConsumerProxy.this.getMqColorValue(msgx))).collect(Collectors.toList());
                if (msgs.size() < 1) {
                    return ConsumeOrderlyStatus.SUCCESS;
                } else {
                    msgs.forEach((msgx) -> {
                        try {
                            RocketmqConsumerProxy.this.decryptMsgBodyIfNecessary(msgx);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    });
                    long begin = System.currentTimeMillis();
                    MsgConsumedStatus msgConsumedStatus = MsgConsumedStatus.SUCCEED;
                    try {
                        if (!listener.isEasy() && !(listener instanceof RocketmqMessageListener)) {
                            RocketmqBatchMsgListener batchMsgListener = (RocketmqBatchMsgListener)listener;
                            msgConsumedStatus = batchMsgListener.onMessage(msgs);
                        } else {
                            for (MessageExt msg : msgs) {
                                MsgConsumedStatus consumeStatus;
                                if (listener.isEasy()) {
                                    ConsumeMessage consumeMessage = ConsumeMessage.parse(msg);
                                    consumeStatus = listener.onMessage(consumeMessage);
                                    if (msgConsumedStatus.equals(MsgConsumedStatus.SUCCEED) && !consumeStatus.equals(MsgConsumedStatus.SUCCEED)) {
                                        msgConsumedStatus = consumeStatus;
                                    }
                                } else {
                                    RocketmqMessageListener rocketmqMessageListener = (RocketmqMessageListener)listener;
                                    consumeStatus = rocketmqMessageListener.onMessage(msg);
                                    if (msgConsumedStatus.equals(MsgConsumedStatus.SUCCEED) && !consumeStatus.equals(MsgConsumedStatus.SUCCEED)) {
                                        msgConsumedStatus = consumeStatus;
                                    }
                                }
                            }
                        }
                        RocketmqConsumerProxy.this.mmsMetrics.consumeSuccessRate().mark();
                    } catch (Throwable e) {
                        MmsConsumerProxy.logger.error("consumer msg failed for {} batch", ((MessageExt)msgs.get(0)).getMsgId(), e);
                        msgConsumedStatus = MsgConsumedStatus.RETRY;
                        RocketmqConsumerProxy.this.mmsMetrics.consumeFailureRate().mark();
                    }
                    if (!msgConsumedStatus.equals(MsgConsumedStatus.SUCCEED)) {
                        RocketmqConsumerProxy.this.mmsMetrics.userCostTimeMs().update(System.currentTimeMillis() - begin, TimeUnit.MILLISECONDS);
                        return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
                    } else {
                        RocketmqConsumerProxy.this.mmsMetrics.userCostTimeMs().update(System.currentTimeMillis() - begin, TimeUnit.MILLISECONDS);
                        return ConsumeOrderlyStatus.SUCCESS;
                    }
                }
            });
        } else {
            this.consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
                msgs = msgs.stream().filter((msgx) -> RocketmqConsumerProxy.this.msgFilter(RocketmqConsumerProxy.this.getMqTagValue(msgx))).filter((msgx) -> RocketmqConsumerProxy.this.msgFilterByColor(RocketmqConsumerProxy.this.getMqColorValue(msgx))).collect(Collectors.toList());
                if (msgs.size() < 1) {
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                } else {
                    msgs.forEach((msgx) -> {
                        try {
                            RocketmqConsumerProxy.this.decryptMsgBodyIfNecessary(msgx);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    });
                    long begin = System.currentTimeMillis();
                    MsgConsumedStatus msgConsumedStatus = MsgConsumedStatus.SUCCEED;
                    try {
                        if (!listener.isEasy() && !(listener instanceof RocketmqMessageListener)) {
                            RocketmqBatchMsgListener rocketmqMessageListener = (RocketmqBatchMsgListener)listener;
                            msgConsumedStatus = rocketmqMessageListener.onMessage(msgs);
                        } else {
                            for (MessageExt msg : msgs) {
                                MsgConsumedStatus consumeStatus;
                                if (listener.isEasy()) {
                                    ConsumeMessage consumeMessage = ConsumeMessage.parse(msg);
                                    consumeStatus = listener.onMessage(consumeMessage);
                                    if (msgConsumedStatus.equals(MsgConsumedStatus.SUCCEED) && !consumeStatus.equals(MsgConsumedStatus.SUCCEED)) {
                                        msgConsumedStatus = consumeStatus;
                                    }
                                } else {
                                    RocketmqMessageListener rocketmqMessageListenerx = (RocketmqMessageListener)listener;
                                    consumeStatus = rocketmqMessageListenerx.onMessage(msg);
                                    if (msgConsumedStatus.equals(MsgConsumedStatus.SUCCEED) && !consumeStatus.equals(MsgConsumedStatus.SUCCEED)) {
                                        msgConsumedStatus = consumeStatus;
                                    }
                                }
                            }
                        }
                        RocketmqConsumerProxy.this.mmsMetrics.consumeSuccessRate().mark();
                    } catch (Throwable e) {
                        MmsConsumerProxy.logger.error("consumer msg failed for {} batch", ((MessageExt)msgs.get(0)).getMsgId(), e);
                        msgConsumedStatus = MsgConsumedStatus.RETRY;
                        RocketmqConsumerProxy.this.mmsMetrics.consumeFailureRate().mark();
                    }
                    if (!msgConsumedStatus.equals(MsgConsumedStatus.SUCCEED)) {
                        RocketmqConsumerProxy.this.mmsMetrics.userCostTimeMs().update(System.currentTimeMillis() - begin, TimeUnit.MILLISECONDS);
                        context.setDelayLevelWhenNextConsume(msgConsumedStatus.level);
                        return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                    } else {
                        RocketmqConsumerProxy.this.mmsMetrics.userCostTimeMs().update(System.currentTimeMillis() - begin, TimeUnit.MILLISECONDS);
                        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                    }
                }
            });
        }
        try {
            this.consumer.start();
            logger.info("ConsumerProxy started at {}, consumer group name:{}", System.currentTimeMillis(), this.metadata.getName());
        } catch (Exception e) {
            logger.error("RocketMQConsumer start error", e);
        }
    }

    protected void consumerShutdown() {
        this.consumer.shutdown();
    }

    public void addUserDefinedProperties(Properties properties) {
        if (properties.containsKey(MmsClientConfig.CONSUMER.MAX_BATCH_RECORDS.getKey())) {
            this.consumer.setPullBatchSize(Integer.parseInt(String.valueOf(properties.get(MmsClientConfig.CONSUMER.MAX_BATCH_RECORDS.getKey()))));
        } else if (properties.containsKey(MmsClientConfig.CONSUMER.CONSUME_MESSAGES_SIZE.getKey())) {
            this.consumer.setPullBatchSize(Integer.parseInt(String.valueOf(properties.get(MmsClientConfig.CONSUMER.CONSUME_MESSAGES_SIZE.getKey()))));
        }
        int consumeThreadMax;
        if (properties.containsKey(MmsClientConfig.CONSUMER.CONSUME_TIMEOUT_MS.getKey())) {
            consumeThreadMax = Integer.parseInt(String.valueOf(properties.get(MmsClientConfig.CONSUMER.CONSUME_TIMEOUT_MS.getKey())));
            if (consumeThreadMax > 0) {
                long timeout = Duration.ofMillis((long)consumeThreadMax).toMinutes();
                this.consumer.setConsumeTimeout(timeout == 0L ? 1L : timeout);
            }
        }
        if (properties.containsKey(MmsClientConfig.CONSUMER.MAX_RECONSUME_TIMES.getKey())) {
            consumeThreadMax = Integer.parseInt(String.valueOf(properties.get(MmsClientConfig.CONSUMER.MAX_RECONSUME_TIMES.getKey())));
            if (consumeThreadMax >= 0) {
                this.consumer.setMaxReconsumeTimes(consumeThreadMax);
            }
        }
        if (!this.sla.isOrderly) {
            if (properties.containsKey(MmsClientConfig.CONSUMER.CONSUME_BATCH_SIZE.getKey())) {
                this.consumer.setConsumeMessageBatchMaxSize(Integer.parseInt(String.valueOf(properties.get(MmsClientConfig.CONSUMER.CONSUME_BATCH_SIZE.getKey()))));
            } else if (properties.containsKey("rocketmqConsumeBatchSize")) {
                this.consumer.setConsumeMessageBatchMaxSize(Integer.parseInt(String.valueOf(properties.get("rocketmqConsumeBatchSize"))));
            }
        }
        if (properties.containsKey(MmsClientConfig.CONSUMER.CONSUME_THREAD_MIN.getKey())) {
            consumeThreadMax = Integer.parseInt(String.valueOf(properties.get(MmsClientConfig.CONSUMER.CONSUME_THREAD_MIN.getKey())));
            this.consumer.setConsumeThreadMin(consumeThreadMax);
        }
        if (properties.containsKey(MmsClientConfig.CONSUMER.CONSUME_THREAD_MAX.getKey())) {
            consumeThreadMax = Integer.parseInt(String.valueOf(properties.get(MmsClientConfig.CONSUMER.CONSUME_THREAD_MAX.getKey())));
            this.consumer.setConsumeThreadMax(consumeThreadMax);
        }
    }

    protected void decryptMsgBodyIfNecessary(MessageExt msg) {
        Map<String, String> properties = msg.getProperties();
        String encryptMarkValue = properties.get("encrypt_mark");
        if (StringUtils.isNotBlank(encryptMarkValue)) {
            msg.setBody(MMSCryptoManager.decrypt(msg.getTopic(), msg.getBody()));
        }
    }

    private String getMqTagValue(MessageExt msg) {
        return msg.getProperties().get("mqTag");
    }

    private String getMqColorValue(MessageExt msg) {
        return msg.getProperties().get("mqColor");
    }
}
