package com.github.bannirui.msb.mq.sdk.consumer;

import java.util.Collection;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface KafkaBatchMsgListener extends MessageListener {

    default MsgConsumedStatus onMessage(Collection<ConsumerRecord<String, byte[]>> msgs) {
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
