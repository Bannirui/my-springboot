package com.github.bannirui.msb.mq.sdk.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface KafkaMessageListener extends MessageListener {
    default MsgConsumedStatus onMessage(ConsumerRecord<String, byte[]> msg) {
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
