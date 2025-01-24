package com.github.bannirui.msb.mq.enums;

public enum MQMsgEnum {
    QUEUE_ID("queueId"),
    BODY_CRC("bodyCRC"),
    QUEUE_OFFSET("queueOffset"),
    MSG_ID("msgId"),
    RECONSUME_TIMES("reconsumeTimes"),
    BODY("BODY"),
    TAG("TAG"),
    BORN_TIMESTAMP("bornTimestamp"),
    BORN_HOST("bornHost"),
    STORE_TIMESTAMP("storeTimestamp"),
    STORE_HOST("storeHost"),
    PROPERTIES("properties"),
    CONSUMER_GROUP("CONSUMER_GROUP"),
    TOPIC("TOPIC");

    private final String value;

    private MQMsgEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
