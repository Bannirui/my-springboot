package com.github.bannirui.msb.mq.sdk.common;

public enum StatisticLoggerType {
    DISK("disk"),
    MESSAGE("message");

    private String name;

    private StatisticLoggerType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
