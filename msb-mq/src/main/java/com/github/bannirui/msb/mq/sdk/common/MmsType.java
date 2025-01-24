package com.github.bannirui.msb.mq.sdk.common;

public enum MmsType {

    TOPIC("topic"),
    CONSUMER_GROUP("consumergroup");

    private String name;

    MmsType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
