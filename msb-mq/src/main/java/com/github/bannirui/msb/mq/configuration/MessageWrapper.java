package com.github.bannirui.msb.mq.configuration;

import java.io.Serializable;
import java.util.Map;

public class MessageWrapper<T> implements Serializable {
    private static final long serialVersionUID = -1857559492347465239L;
    public static final String BODY_TYPE = "bodyType";
    private T body;
    private String queueId;
    private int bodyCRC;
    private long queueOffset;
    private String msgId;
    private int reconsumeTimes;
    private String tags;
    private long bornTimestamp;
    private String bornHost;
    private long storeTimestamp;
    private String storeHost;
    private Map<String, String> properties;
    private String consumerGroup;
    private String topic;

    public T getBody() {
        return this.body;
    }

    public void setBody(T body) {
        this.body = body;
    }

    public String getQueueId() {
        return this.queueId;
    }

    public void setQueueId(String queueId) {
        this.queueId = queueId;
    }

    public int getBodyCRC() {
        return this.bodyCRC;
    }

    public void setBodyCRC(int bodyCRC) {
        this.bodyCRC = bodyCRC;
    }

    public long getQueueOffset() {
        return this.queueOffset;
    }

    public void setQueueOffset(long queueOffset) {
        this.queueOffset = queueOffset;
    }

    public String getMsgId() {
        return this.msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public int getReconsumeTimes() {
        return this.reconsumeTimes;
    }

    public void setReconsumeTimes(int reconsumeTimes) {
        this.reconsumeTimes = reconsumeTimes;
    }

    public String getTags() {
        return this.tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public long getBornTimestamp() {
        return this.bornTimestamp;
    }

    public void setBornTimestamp(long bornTimestamp) {
        this.bornTimestamp = bornTimestamp;
    }

    public String getBornHost() {
        return this.bornHost;
    }

    public void setBornHost(String bornHost) {
        this.bornHost = bornHost;
    }

    public long getStoreTimestamp() {
        return this.storeTimestamp;
    }

    public void setStoreTimestamp(long storeTimestamp) {
        this.storeTimestamp = storeTimestamp;
    }

    public String getStoreHost() {
        return this.storeHost;
    }

    public void setStoreHost(String storeHost) {
        this.storeHost = storeHost;
    }

    public Map<String, String> getProperties() {
        return this.properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public String getConsumerGroup() {
        return this.consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public String getTopic() {
        return this.topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String toString() {
        return "MessageWrap{body=" + this.body + ", queueId='" + this.queueId + '\'' + ", bodyCRC='" + this.bodyCRC + '\'' + ", queueOffset=" + this.queueOffset + ", msgId='" + this.msgId + '\'' + ", reconsumeTimes=" + this.reconsumeTimes + ", tags='" + this.tags + '\'' + ", bornTimestamp=" + this.bornTimestamp + ", bornHost='" + this.bornHost + '\'' + ", storeTimestamp=" + this.storeTimestamp + ", storeHost='" + this.storeHost + '\'' + ", properties=" + this.properties + ", consumerGroup='" + this.consumerGroup + '\'' + ", topic='" + this.topic + '\'' + '}';
    }
}
