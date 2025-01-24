package com.github.bannirui.msb.mq.sdk.config;

import java.util.Properties;

public class DefaultMmsClientConfig {
    public static final Properties DEFAULT_KAFKA_PRODUCER_CONFIG = new Properties();
    public static final Properties DEFAULT_KAFKA_CONSUMER_CONFIG = new Properties();
    /** @deprecated */
    @Deprecated
    public static final Properties DEFAULT_ROCKETMQ_PRODUCER_CONFIG = new Properties();
    /** @deprecated */
    @Deprecated
    public static final Properties DEFAULT_ROCKETMQ_CONSUMER_CONFIG = new Properties();

    static {
        DEFAULT_KAFKA_PRODUCER_CONFIG.put("acks", "all");
        DEFAULT_KAFKA_PRODUCER_CONFIG.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        DEFAULT_KAFKA_PRODUCER_CONFIG.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        DEFAULT_KAFKA_PRODUCER_CONFIG.put("max.in.flight.requests.per.connection", "1");
        DEFAULT_KAFKA_CONSUMER_CONFIG.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        DEFAULT_KAFKA_CONSUMER_CONFIG.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        DEFAULT_KAFKA_CONSUMER_CONFIG.put("enable.auto.commit", false);
    }
}
