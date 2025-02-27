package com.github.bannirui.msb.endpoint.jmx;

import com.github.bannirui.msb.endpoint.condition.OnEndpointElementCondition;

public class OnActivatedMonitorCondition extends OnEndpointElementCondition {
    public static final String PREFIX = "msb.endpoint.monitor.";

    public OnActivatedMonitorCondition() {
        super(OnActivatedMonitorCondition.PREFIX, ConditionalOnActivatedMonitor.class);
    }
}
