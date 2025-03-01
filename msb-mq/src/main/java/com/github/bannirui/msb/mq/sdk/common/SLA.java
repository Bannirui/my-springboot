package com.github.bannirui.msb.mq.sdk.common;

import com.github.bannirui.mms.client.config.MmsClientConfig;
import java.util.Properties;

public class SLA {
    public boolean isOrderly = false;

    public static SLA parse(Properties properties) {
        SLA sla = new SLA();
        if (properties.containsKey(MmsClientConfig.CONSUMER.CONSUME_ORDERLY.getKey())) {
            sla.setOrderly(Boolean.parseBoolean(String.valueOf(properties.get(MmsClientConfig.CONSUMER.CONSUME_ORDERLY.getKey()))));
        }
        return sla;
    }

    public boolean isOrderly() {
        return this.isOrderly;
    }

    public void setOrderly(boolean orderly) {
        this.isOrderly = orderly;
    }
}
