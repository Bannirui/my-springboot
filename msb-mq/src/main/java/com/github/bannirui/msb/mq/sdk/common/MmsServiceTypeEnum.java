package com.github.bannirui.msb.mq.sdk.common;

public enum MmsServiceTypeEnum {
    MMS_COLLECTOR,
    MMS_ALERT,
    ZOOKEEPER,
    INFLUXDB,
    KAFKA,
    ROCKETMQ,
    MMS_BACKUP_CLUSTER;

    public static String getServiceType(String serviceType) {
        for (MmsServiceTypeEnum value : MmsServiceTypeEnum.values()) {
            if (value.name().equalsIgnoreCase(serviceType)) {
                return value.name();
            }
        }
        return null;
    }
}
