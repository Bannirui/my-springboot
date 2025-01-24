package com.github.bannirui.msb.mq.sdk.common;

import com.github.bannirui.msb.mq.sdk.producer.SendType;

public class MmsMessage extends SimpleMessage {
    private SendType sendType;

    public MmsMessage() {
        this.sendType = SendType.SYNC;
    }

    public void setSendType(SendType sendType) {
        this.sendType = sendType;
    }

    public SendType getSendType() {
        return this.sendType;
    }
}
