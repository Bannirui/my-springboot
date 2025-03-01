package com.github.bannirui.msb.mq.configuration;

import com.alibaba.fastjson.JSON;
import com.github.bannirui.mms.client.consumer.ConsumeMessage;
import com.github.bannirui.mms.client.consumer.KafkaMessageListener;
import com.github.bannirui.mms.client.consumer.MsgConsumedStatus;
import com.github.bannirui.mms.client.consumer.RocketmqMessageListener;
import com.github.bannirui.mms.common.BrokerType;
import com.github.bannirui.msb.ex.FrameworkException;
import com.github.bannirui.msb.mq.enums.MMSResult;
import com.github.bannirui.msb.mq.enums.MQMsgEnum;
import com.github.bannirui.msb.mq.enums.Serialize;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * mq监听器的统一封装
 * <ul>
 *     <li>rocket</li>
 *     <li>mq</li>
 * </ul>
 */
public class MMSMessageListenerImpl implements RocketmqMessageListener, KafkaMessageListener {

    private static final Logger log = LoggerFactory.getLogger(MMSMessageListenerImpl.class);

    private boolean easy = false;
    private String consumerGroup;

    /**
     * 有参数构造 {@link org.springframework.cglib.proxy.Enhancer}生成代理对象时使用
     * @param consumerGroup
     */
    public MMSMessageListenerImpl(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public String getConsumerGroup() {
        return this.consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    @Override
    public MsgConsumedStatus onMessage(ConsumeMessage msg) {
        try {
            MMSConf MMSConf = MMSContext.getMmsConfMap().get(this.consumerGroup + "~" + msg.getTag());
            if (MMSConf == null) {
                MMSConf = MMSContext.getMmsConfMap().get(this.consumerGroup + "~" + "*");
            }
            if (MMSConf == null) {
                throw FrameworkException.getInstance("订阅消息未定义的consumerGroup:[{0}] tags:[{1}]", this.consumerGroup, msg.getTag());
            }
            Object[] params = new Object[MMSConf.getParams().size()];
            for(int i = 0; i < MMSConf.getParams().size(); ++i) {
                Map<String, Object> map = MMSConf.getParams().get(i);
                String param;
                if (map.get("name").toString().equalsIgnoreCase(MQMsgEnum.BODY.getValue())) {
                    param = new String(msg.getPayload());
                } else if (map.get("name").toString().equalsIgnoreCase(MQMsgEnum.MSG_ID.getValue())) {
                    param = msg.getMsgId();
                } else if (map.get("name").toString().equalsIgnoreCase(MQMsgEnum.BODY_CRC.getValue())) {
                    param = msg.getProperties().getProperty("boydCrc");
                } else if (map.get("name").toString().equalsIgnoreCase(MQMsgEnum.QUEUE_OFFSET.getValue())) {
                    param = String.valueOf(msg.getOffset());
                } else if (map.get("name").toString().equalsIgnoreCase(MQMsgEnum.RECONSUME_TIMES.getValue())) {
                    param = msg.getProperties().getProperty("reconsumeTimes");
                } else if (map.get("name").toString().equalsIgnoreCase(MQMsgEnum.QUEUE_ID.getValue())) {
                    param = String.valueOf(msg.getPartition());
                } else if (map.get("name").toString().equalsIgnoreCase(MQMsgEnum.TAG.getValue())) {
                    param = String.valueOf(msg.getTag());
                } else if (map.get("name").toString().equalsIgnoreCase(MQMsgEnum.BORN_TIMESTAMP.getValue())) {
                    param = String.valueOf(msg.getProperties().getProperty("bornTimestamp"));
                } else if (map.get("name").toString().equalsIgnoreCase(MQMsgEnum.BORN_HOST.getValue())) {
                    param = String.valueOf(msg.getProperties().getProperty("bornHost"));
                } else if (map.get("name").toString().equalsIgnoreCase(MQMsgEnum.STORE_TIMESTAMP.getValue())) {
                    param = String.valueOf(msg.getProperties().getProperty("storeTimestamp"));
                } else if (map.get("name").toString().equalsIgnoreCase(MQMsgEnum.STORE_HOST.getValue())) {
                    param = String.valueOf(msg.getProperties().getProperty("storeHost"));
                } else if (map.get("name").toString().equalsIgnoreCase(MQMsgEnum.PROPERTIES.getValue())) {
                    param = this.parseProperties(msg);
                } else if (map.get("name").toString().equalsIgnoreCase(MQMsgEnum.CONSUMER_GROUP.getValue())) {
                    param = this.consumerGroup;
                } else if (map.get("name").toString().equalsIgnoreCase(MQMsgEnum.TOPIC.getValue())) {
                    param = msg.getTopic();
                } else {
                    param = msg.getProperties().getProperty(map.get("name").toString());
                }
                params[i] = this.serialize(map, param);
            }
            Object result = MMSConf.getMethod().invoke(MMSConf.getObj(), params);
            if (result instanceof MMSResult) {
                return ((MMSResult)result).getConsumedStatus();
            }
        } catch (InvocationTargetException e) {
            if (e.getTargetException() != null) {
                log.error("MQ消费消息失败,msg{}，等待重试...", msg, e.getTargetException());
            }
            return MsgConsumedStatus.RETRY;
        } catch (Exception e) {
            log.error("MQ消费消息失败,msg{}，等待重试...", msg, e);
            return MsgConsumedStatus.RETRY;
        }
        return MsgConsumedStatus.SUCCEED;
    }

    @Override
    public MsgConsumedStatus onMessage(MessageExt msg) {
        try {
            MMSConf MMSConf = MMSContext.getMmsConfMap().get(this.consumerGroup + "~" + msg.getTags());
            if (MMSConf == null) {
                MMSConf = MMSContext.getMmsConfMap().get(this.consumerGroup + "~*");
            }
            if (MMSConf == null) {
                throw FrameworkException.getInstance("订阅消息未定义的consumerGroup:[{0}] tags:[{1}]", this.consumerGroup, msg.getTags());
            }
            Object[] params = new Object[MMSConf.getParams().size()];
            for(int i = 0; i < MMSConf.getParams().size(); ++i) {
                Map<String, Object> map = MMSConf.getParams().get(i);
                String param;
                if (map.get("name").toString().equalsIgnoreCase(MQMsgEnum.BODY.getValue())) {
                    param = new String(msg.getBody());
                } else if (map.get("name").toString().equalsIgnoreCase(MQMsgEnum.MSG_ID.getValue())) {
                    param = msg.getMsgId();
                } else if (map.get("name").toString().equalsIgnoreCase(MQMsgEnum.BODY_CRC.getValue())) {
                    param = String.valueOf(msg.getBodyCRC());
                } else if (map.get("name").toString().equalsIgnoreCase(MQMsgEnum.QUEUE_OFFSET.getValue())) {
                    param = String.valueOf(msg.getQueueOffset());
                } else if (map.get("name").toString().equalsIgnoreCase(MQMsgEnum.RECONSUME_TIMES.getValue())) {
                    param = String.valueOf(msg.getReconsumeTimes());
                } else if (map.get("name").toString().equalsIgnoreCase(MQMsgEnum.QUEUE_ID.getValue())) {
                    param = String.valueOf(msg.getQueueId());
                } else if (map.get("name").toString().equalsIgnoreCase(MQMsgEnum.TAG.getValue())) {
                    param = String.valueOf(msg.getTags());
                } else if (map.get("name").toString().equalsIgnoreCase(MQMsgEnum.BORN_TIMESTAMP.getValue())) {
                    param = String.valueOf(msg.getBornTimestamp());
                } else if (map.get("name").toString().equalsIgnoreCase(MQMsgEnum.BORN_HOST.getValue())) {
                    param = msg.getBornHost().toString();
                } else if (map.get("name").toString().equalsIgnoreCase(MQMsgEnum.STORE_TIMESTAMP.getValue())) {
                    param = String.valueOf(msg.getStoreTimestamp());
                } else if (map.get("name").toString().equalsIgnoreCase(MQMsgEnum.STORE_HOST.getValue())) {
                    param = msg.getStoreHost().toString();
                } else if (map.get("name").toString().equalsIgnoreCase(MQMsgEnum.PROPERTIES.getValue())) {
                    param = this.parseRocketMqProperties(msg.getProperties());
                } else if (map.get("name").toString().equalsIgnoreCase(MQMsgEnum.CONSUMER_GROUP.getValue())) {
                    param = this.consumerGroup;
                } else if (map.get("name").toString().equalsIgnoreCase(MQMsgEnum.TOPIC.getValue())) {
                    param = msg.getTopic();
                } else {
                    param = msg.getProperties().get(map.get("name").toString());
                }
                params[i] = this.serialize(map, param);
            }
            Object result = MMSConf.getMethod().invoke(MMSConf.getObj(), params);
            if (result instanceof MMSResult) {
                return ((MMSResult)result).getConsumedStatus();
            }
        } catch (InvocationTargetException e) {
            if (e.getTargetException() != null) {
                log.error("MQ消费消息失败,msg{}，等待重试...", msg, e.getTargetException());
            }
            return MsgConsumedStatus.RETRY;
        } catch (Exception e) {
            log.error("MQ消费消息失败,msg{}，等待重试...", msg, e);
            return MsgConsumedStatus.RETRY;
        }
        return MsgConsumedStatus.SUCCEED;
    }

    @Override
    public MsgConsumedStatus onMessage(ConsumerRecord msg) {
        try {
            MMSConf MMSConf = MMSContext.getMmsConfMap().get(this.consumerGroup + "~*");
            if (MMSConf == null) {
                throw FrameworkException.getInstance("订阅消息未定义的consumerGroup:[{0}]", this.consumerGroup);
            }
            Object[] params = new Object[MMSConf.getParams().size()];
            for(int i = 0; i < MMSConf.getParams().size(); ++i) {
                Map<String, Object> map = MMSConf.getParams().get(i);
                String param = null;
                if (map.get("name").toString().equalsIgnoreCase(MQMsgEnum.BODY.getValue())) {
                    param = new String((byte[]) msg.value(), StandardCharsets.UTF_8);
                } else if (map.get("name").toString().equalsIgnoreCase(MQMsgEnum.QUEUE_OFFSET.getValue())) {
                    param = String.valueOf(msg.offset());
                } else if (map.get("name").toString().equalsIgnoreCase(MQMsgEnum.QUEUE_ID.getValue())) {
                    param = String.valueOf(msg.partition());
                } else if (map.get("name").toString().equalsIgnoreCase(MQMsgEnum.PROPERTIES.getValue())) {
                    param = this.parseKafkaProperties(msg.headers());
                } else if (map.get("name").toString().equalsIgnoreCase(MQMsgEnum.CONSUMER_GROUP.getValue())) {
                    param = this.consumerGroup;
                } else if (map.get("name").toString().equalsIgnoreCase(MQMsgEnum.TOPIC.getValue())) {
                    param = msg.topic();
                }
                params[i] = this.serialize(map, param);
            }
            Object result = MMSConf.getMethod().invoke(MMSConf.getObj(), params);
            if (result instanceof MMSResult) {
                return ((MMSResult)result).getConsumedStatus();
            }
        } catch (InvocationTargetException e) {
            if (e.getTargetException() != null) {
                log.error("MQ消费消息失败,msg{}，等待重试...", msg, e.getTargetException());
            }
            return MsgConsumedStatus.RETRY;
        } catch (Exception e) {
            log.error("MQ消费消息失败,msg{}，等待重试...", msg, e);
            return MsgConsumedStatus.RETRY;
        }
        return MsgConsumedStatus.SUCCEED;
    }

    private Object serialize(Map<String, Object> map, String param) {
        Object obj;
        if (Serialize.JSON.getValue().equals(map.get("serialize"))) {
            obj = JSON.parseObject(param, (Class)map.get("serializeType"));
        } else if (Serialize.JSON_ARRAY.getValue().equals(map.get("serialize"))) {
            obj = JSON.parseArray(param, (Class)map.get("serializeType"));
        } else {
            if (!Serialize.STRING.getValue().equals(map.get("serialize"))) {
                throw FrameworkException.getInstance("订阅消息发现了未知的序列化类型{0}", new Object[]{map.get("serialize")});
            }
            obj = param;
        }
        return obj;
    }

    private String parseProperties(ConsumeMessage msg) {
        BrokerType brokerType = (BrokerType)msg.getProperties().get("broker_type");
        switch(brokerType) {
            case KAFKA:
                return this.parseKafkaProperties((Headers)msg.getProperties().get("headers"));
            case ROCKETMQ:
                return this.parseRocketMqProperties((Map)msg.getProperties().get("properties"));
            default:
                throw FrameworkException.getInstance("broker type invalid:{0}", new Object[]{brokerType.name()});
        }
    }

    private String parseKafkaProperties(Headers headers) {
        HashMap<String, String> kafkaProperties = new HashMap<>();
        for (Header header : headers) {
            kafkaProperties.put(header.key(), new String(header.value(), StandardCharsets.UTF_8));
        }
        return JSON.toJSONString(kafkaProperties);
    }

    private String parseRocketMqProperties(Map<String, String> rocketMqProperties) {
        return JSON.toJSONString(rocketMqProperties == null ? new HashMap<>() : rocketMqProperties);
    }

    @Override
    public boolean isEasy() {
        return this.easy;
    }

    public void setEasy(boolean easy) {
        this.easy = easy;
    }
}
