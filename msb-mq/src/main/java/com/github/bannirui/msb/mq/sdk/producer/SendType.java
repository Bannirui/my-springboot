package com.github.bannirui.msb.mq.sdk.producer;

public enum SendType {
    SYNC("sync"),
    ASYNC("async"),
    ONEWAY("oneway");

    private String sendType;

    private SendType(String sendType) {
        this.sendType = sendType;
    }

    public String getSendType() {
        return this.sendType;
    }
}
