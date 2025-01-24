package com.github.bannirui.msb.mq.configuration;

import com.github.bannirui.msb.mq.sdk.common.SimpleMessage;

@FunctionalInterface
public interface CompleteMessageListener {
    void complete(SimpleMessage message);
}
