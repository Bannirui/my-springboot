package com.github.bannirui.msb.mq.sdk.consumer;

import com.github.bannirui.msb.mq.sdk.common.BrokerType;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.rocketmq.common.message.MessageExt;

public class ConsumeMessage {
    private String topic;
    private int partition;
    private long offset;
    private byte[] payload;
    private String msgId;
    private String tag;
    private Properties properties = new Properties();

    public String getTopic() {
        return this.topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getPartition() {
        return this.partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public long getOffset() {
        return this.offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public byte[] getPayload() {
        return this.payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public String getMsgId() {
        return this.msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getTag() {
        return this.tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Properties getProperties() {
        return this.properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public static ConsumeMessage parse(MessageExt rocketmqMsg) {
        ConsumeMessage message = new ConsumeMessage();
        message.setTopic(rocketmqMsg.getTopic());
        message.setMsgId(rocketmqMsg.getMsgId());
        message.setOffset(rocketmqMsg.getQueueOffset());
        message.setPartition(rocketmqMsg.getQueueId());
        message.setPayload(rocketmqMsg.getBody());
        message.setTag(rocketmqMsg.getTags());
        message.getProperties().put("reconsumeTimes", String.valueOf(rocketmqMsg.getReconsumeTimes()));
        message.getProperties().put("bornTimestamp", rocketmqMsg.getBornTimestamp());
        message.getProperties().put("bornHost", rocketmqMsg.getBornHost().toString());
        message.getProperties().put("storeTimestamp", rocketmqMsg.getStoreTimestamp());
        message.getProperties().put("storeHost", rocketmqMsg.getBornHost().toString());
        message.getProperties().put("boydCrc", rocketmqMsg.getBodyCRC());
        message.getProperties().put("broker_type", BrokerType.ROCKETMQ);
        message.getProperties().put("properties", rocketmqMsg.getProperties());
        return message;
    }

    public static ConsumeMessage parse(ConsumerRecord<?,?> kafkaMsg) {
        ConsumeMessage message = new ConsumeMessage();
        message.setTopic(kafkaMsg.topic());
        message.setMsgId("");
        message.setOffset(kafkaMsg.offset());
        message.setPartition(kafkaMsg.partition());
        message.setPayload((byte[]) kafkaMsg.value());
        message.getProperties().put("broker_type", BrokerType.KAFKA);
        message.getProperties().put("headers", kafkaMsg.headers());
        return message;
    }
}
