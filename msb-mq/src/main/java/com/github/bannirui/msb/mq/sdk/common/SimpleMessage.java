package com.github.bannirui.msb.mq.sdk.common;

import java.util.Map;

public class SimpleMessage {
    private String key;
    private String tags;
    private int delayLevel = 0;
    private Map<String, String> properties;
    private byte[] payload;

    public SimpleMessage() {
    }

    public SimpleMessage(byte[] payload) {
        this.payload = payload;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTags() {
        return this.tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public byte[] getPayload() {
        return this.payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public int getDelayLevel() {
        return this.delayLevel;
    }

    public void setDelayLevel(int delayLevel) {
        this.delayLevel = delayLevel;
    }

    public Map<String, String> getProperties() {
        return this.properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
}
