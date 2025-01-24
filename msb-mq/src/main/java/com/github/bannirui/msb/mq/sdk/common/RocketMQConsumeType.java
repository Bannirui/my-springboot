package com.github.bannirui.msb.mq.sdk.common;

import com.github.bannirui.msb.mq.sdk.config.MmsClientConfig;
import java.util.Properties;

public class RocketMQConsumeType {
    public boolean isNewPush = false;

    public static RocketMQConsumeType parse(Properties properties) {
        RocketMQConsumeType type = new RocketMQConsumeType();
        if (properties.containsKey(MmsClientConfig.CONSUMER.CONSUME_LITE_PUSH.getKey())) {
            type.setPoll(Boolean.parseBoolean(String.valueOf(properties.get(MmsClientConfig.CONSUMER.CONSUME_LITE_PUSH.getKey()))));
        }
        return type;
    }

    public boolean isNewPush() {
        return this.isNewPush;
    }

    public void setPoll(boolean poll) {
        this.isNewPush = poll;
    }
}
