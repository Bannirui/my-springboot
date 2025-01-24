package com.github.bannirui.msb.mq.sdk.consumer;

import org.apache.rocketmq.common.message.MessageExt;

public interface RocketmqMessageListener extends MessageListener {
    default MsgConsumedStatus onMessage(MessageExt msg) {
        if (this.isEasy()) {
            throw RUNTIME_EXCEPTION;
        } else {
            return MsgConsumedStatus.SUCCEED;
        }
    }

    default boolean isEasy() {
        return false;
    }
}
