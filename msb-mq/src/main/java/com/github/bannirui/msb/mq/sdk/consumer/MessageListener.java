package com.github.bannirui.msb.mq.sdk.consumer;

/**
 * 封装消息监听器 屏蔽中间件差异
 */
public interface MessageListener {
    RuntimeException RUNTIME_EXCEPTION = new RuntimeException("illegal messageListener type, should correct rocketmqMessageListener or kafkaMessageListener");

    default MsgConsumedStatus onMessage(ConsumeMessage msg) {
        if (!this.isEasy()) {
            throw RUNTIME_EXCEPTION;
        } else {
            return MsgConsumedStatus.SUCCEED;
        }
    }

    default boolean isEasy() {
        return true;
    }
}
