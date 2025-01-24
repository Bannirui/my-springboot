package com.github.bannirui.msb.mq.sdk.common;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public enum BrokerType {
    KAFKA("kafka"),
    ROCKETMQ("rocketmq");

    public String name;

    BrokerType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public static List<String> getValues() {
        List<String> list = new ArrayList<>();
        for (BrokerType value : BrokerType.values()) {
            list.add(value.getName());
        }
        return list;
    }

    public static BrokerType parseFrom(String property) {
        if (StringUtils.isEmpty(property)) {
            return null;
        } else if (KAFKA.getName().equalsIgnoreCase(property)) {
            return KAFKA;
        } else if (ROCKETMQ.getName().equalsIgnoreCase(property)) {
            return ROCKETMQ;
        }
        return null;
    }
}
