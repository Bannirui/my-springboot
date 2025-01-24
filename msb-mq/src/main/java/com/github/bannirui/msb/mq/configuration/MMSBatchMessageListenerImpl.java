package com.github.bannirui.msb.mq.configuration;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.github.bannirui.msb.common.ex.FrameworkException;
import com.github.bannirui.msb.mq.enums.MMSResult;
import com.github.bannirui.msb.mq.sdk.consumer.KafkaBatchMsgListener;
import com.github.bannirui.msb.mq.sdk.consumer.KafkaMessageListener;
import com.github.bannirui.msb.mq.sdk.consumer.MsgConsumedStatus;
import com.github.bannirui.msb.mq.sdk.consumer.RocketmqBatchMsgListener;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MMSBatchMessageListenerImpl implements RocketmqBatchMsgListener, KafkaMessageListener, KafkaBatchMsgListener {
    private static final Logger log = LoggerFactory.getLogger(MMSBatchMessageListenerImpl.class);
    private String consumerGroup;

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public String getConsumerGroup() {
        return this.consumerGroup;
    }

    public MMSBatchMessageListenerImpl(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public MsgConsumedStatus onMessage(List<MessageExt> msgs) {
        Map<MMSConf, List<MessageWrapper<Object>>> msgMap = new HashMap<>();
        try {
            for (MessageExt msg : msgs) {
                MMSConf MMSConf = MMSContext.getMmsConfMap().get(this.consumerGroup + "~" + msg.getTags());
                if (MMSConf == null) {
                    MMSConf = MMSContext.getMmsConfMap().get(this.consumerGroup + "~*");
                }
                if (MMSConf == null) {
                    throw FrameworkException.getInstance("订阅消息未定义的consumerGroup:[{0}] tags:[{1}]", new Object[]{this.consumerGroup, msg.getTags()});
                }
                Map<String, Object> params = MMSConf.getParams().get(0);
                Class<?> bodyClazz = (Class<?>)params.get("bodyType");
                MessageWrapper<Object> messageWrapper = new MessageWrapper<>();
                if (bodyClazz.equals(String.class)) {
                    messageWrapper.setBody(new String(msg.getBody()));
                } else {
                    messageWrapper.setBody(JSON.parseObject(msg.getBody(), bodyClazz, new Feature[0]));
                }
                messageWrapper.setQueueId(String.valueOf(msg.getQueueId()));
                messageWrapper.setBodyCRC(msg.getBodyCRC());
                messageWrapper.setQueueOffset(msg.getQueueOffset());
                messageWrapper.setMsgId(msg.getMsgId());
                messageWrapper.setReconsumeTimes(msg.getReconsumeTimes());
                messageWrapper.setTags(msg.getTags());
                messageWrapper.setBornTimestamp(msg.getBornTimestamp());
                messageWrapper.setBornHost(msg.getBornHost().toString());
                messageWrapper.setStoreTimestamp(msg.getStoreTimestamp());
                messageWrapper.setStoreHost(msg.getStoreHost().toString());
                messageWrapper.setProperties(msg.getProperties());
                messageWrapper.setConsumerGroup(this.consumerGroup);
                messageWrapper.setTopic(msg.getTopic());
                List<MessageWrapper<Object>> messageWrappers = msgMap.computeIfAbsent(MMSConf, (c) -> new ArrayList<>());
                messageWrappers.add(messageWrapper);
            }
            for (Map.Entry<MMSConf, List<MessageWrapper<Object>>> entry : msgMap.entrySet()) {
                MMSConf k = entry.getKey();
                List<MessageWrapper<Object>> v = entry.getValue();
                Object result = k.getMethod().invoke(k.getObj(), v);
                if (result instanceof MMSResult ret) {
                    return ret.getConsumedStatus();
                }
            }
        } catch (InvocationTargetException e) {
            if (e.getTargetException() != null) {
                log.error("MQ消费消息失败,msg{}，等待重试...", msgs, e.getTargetException());
            }
            return MsgConsumedStatus.RETRY;
        } catch (Exception e) {
            log.error("MQ消费消息失败,msg{}，等待重试...", msgs, e);
            return MsgConsumedStatus.RETRY;
        }
        return MsgConsumedStatus.SUCCEED;
    }

    public MsgConsumedStatus onMessage(Collection<ConsumerRecord<String, byte[]>> msgs) {
        return this.kafkaBatchConsume(msgs);
    }

    public MsgConsumedStatus onMessage(ConsumerRecord<String, byte[]> msg) {
        List<ConsumerRecord<String, byte[]>> msgs = new ArrayList<>();
        msgs.add(msg);
        return this.kafkaBatchConsume(msgs);
    }

    private MsgConsumedStatus kafkaBatchConsume(Collection<ConsumerRecord<String, byte[]>> msgs) {
        MMSConf MMSConf = MMSContext.getMmsConfMap().get(this.consumerGroup + "~*");
        if (MMSConf == null) {
            throw FrameworkException.getInstance("订阅消息未定义的consumerGroup:[{0}]", this.consumerGroup);
        } else {
            List<MessageWrapper<Object>> messageWrappers = new ArrayList<>();
            Map<String, Object> params = MMSConf.getParams().get(0);
            Class<?> bodyClazz = (Class<?>)params.get("bodyType");
            Object record = null;
            try {
                for (ConsumerRecord<String, byte[]> msg : msgs) {
                    MessageWrapper<Object> messageWrapper = new MessageWrapper<>();
                    if (bodyClazz.equals(String.class)) {
                        messageWrapper.setBody(new String(msg.value()));
                    } else {
                        messageWrapper.setBody(JSON.parseObject(msg.value(), bodyClazz));
                    }
                    messageWrapper.setQueueId(String.valueOf(msg.partition()));
                    messageWrapper.setQueueOffset(msg.offset());
                    HashMap<String, String> kafkaProperties = new HashMap<>();
                    Headers headers = msg.headers();
                    for (Header header : headers) {
                        kafkaProperties.put(header.key(), new String(header.value(), StandardCharsets.UTF_8));
                    }
                    messageWrapper.setProperties(kafkaProperties);
                    messageWrapper.setConsumerGroup(this.consumerGroup);
                    messageWrapper.setTopic(msg.topic());
                    messageWrappers.add(messageWrapper);
                }
                Object result = MMSConf.getMethod().invoke(MMSConf.getObj(), messageWrappers);
                if (result instanceof MMSResult) {
                    return ((MMSResult)result).getConsumedStatus();
                }
            } catch (InvocationTargetException e) {
                if (e.getTargetException() != null) {
                    log.error("Kafka 消费消息失败, record{}...", record, e.getTargetException());
                }
                return MsgConsumedStatus.RETRY;
            } catch (Exception e) {
                log.error("Kafka 消费消息失败, record{}", record, e);
                return MsgConsumedStatus.RETRY;
            }
            return MsgConsumedStatus.SUCCEED;
        }
    }

    public boolean isEasy() {
        return false;
    }
}
