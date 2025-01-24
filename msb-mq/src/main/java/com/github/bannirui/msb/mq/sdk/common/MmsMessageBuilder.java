package com.github.bannirui.msb.mq.sdk.common;

import com.github.bannirui.msb.mq.sdk.producer.SendType;
import java.util.Map;

public class MmsMessageBuilder {
    private MmsMessage message = new MmsMessage();

    public static MmsMessageBuilder newInstance() {
        return new MmsMessageBuilder();
    }

    public MmsMessageBuilder buildSendType(SendType sendType) {
        this.message.setSendType(sendType);
        return this;
    }

    public MmsMessageBuilder buildKey(String key) {
        this.message.setKey(key);
        return this;
    }

    public MmsMessageBuilder buildTags(String tags) {
        this.message.setTags(tags);
        return this;
    }

    public MmsMessageBuilder buildPaylod(byte[] payload) {
        this.message.setPayload(payload);
        return this;
    }

    public MmsMessageBuilder buildDelayLevel(int delayLevel) {
        this.message.setDelayLevel(delayLevel);
        return this;
    }

    public MmsMessageBuilder buildProperties(Map<String, String> props) {
        this.message.setProperties(props);
        return this;
    }

    public MmsMessage build() {
        return this.message;
    }
}
