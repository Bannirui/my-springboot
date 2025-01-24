package com.github.bannirui.msb.mq.sdk.metrics;

import com.codahale.metrics.MetricRegistry;
import com.github.bannirui.msb.mq.sdk.common.MmsEnv;

public class MmsMetricsRegistry {
    public static final MetricRegistry REGISTRY = new MetricRegistry();

    public static final String buildName(String producderMetricGroup, String type, String clientName, String mmsName) {
        return producderMetricGroup + "--" + type + "--" + mmsName + "--" + MmsEnv.MMS_IP.replace(".", "_") + "--" + clientName;
    }
}
