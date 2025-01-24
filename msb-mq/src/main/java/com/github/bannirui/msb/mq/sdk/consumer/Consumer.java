package com.github.bannirui.msb.mq.sdk.consumer;

import java.util.Properties;

public interface Consumer {
    void register(MessageListener listener);

    void shutdown();

    void statistics();

    void addUserDefinedProperties(Properties properties);
}
