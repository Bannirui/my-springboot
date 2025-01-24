package com.github.bannirui.msb.mq.sdk.common;

public class SimpleMessageBuilder {
    private final SimpleMessage message = new SimpleMessage();

    public static SimpleMessageBuilder newInstance() {
        return new SimpleMessageBuilder();
    }

    public SimpleMessageBuilder buildKey(String key) {
        this.message.setKey(key);
        return this;
    }

    public SimpleMessageBuilder buildTags(String tags) {
        this.message.setTags(tags);
        return this;
    }

    /** @deprecated */
    @Deprecated
    public SimpleMessageBuilder buildPaylod(byte[] payload) {
        this.message.setPayload(payload);
        return this;
    }

    public SimpleMessageBuilder buildPayload(byte[] payload) {
        this.message.setPayload(payload);
        return this;
    }

    public SimpleMessageBuilder buildDelayLevel(int delayLevel) {
        this.message.setDelayLevel(delayLevel);
        return this;
    }

    public SimpleMessage build() {
        return this.message;
    }
}
