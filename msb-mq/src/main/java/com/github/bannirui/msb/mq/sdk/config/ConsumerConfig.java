package com.github.bannirui.msb.mq.sdk.config;

import java.util.Properties;

public class ConsumerConfig {
    public static final class RocketMq {
        public static final Properties ROCKETMQ_CONFIG = new Properties();
    }

    public static final class KAFKA {
        public static final Properties KAFKA_CONFIG = new Properties();
        static {
            KAFKA_CONFIG.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            KAFKA_CONFIG.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
            KAFKA_CONFIG.put("enable.auto.commit", false);
        }
    }
}
