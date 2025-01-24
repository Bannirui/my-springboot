package com.github.bannirui.msb.mq.sdk.consumer;

import java.util.List;
import org.apache.rocketmq.common.message.MessageExt;

public interface RocketmqBatchMsgListener extends MessageListener {
    default MsgConsumedStatus onMessage(List<MessageExt> msgs) {
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
