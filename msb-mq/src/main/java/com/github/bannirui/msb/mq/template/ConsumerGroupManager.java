package com.github.bannirui.msb.mq.template;

public interface ConsumerGroupManager {
    void enableSubscribe(String templateName, String consumerGroup, String tag);

    void disableSubscribe(String consumerGroup, String tag);
}
