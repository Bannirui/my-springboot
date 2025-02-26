package com.github.bannirui.msb.endpoint.jmx;

import com.github.bannirui.msb.endpoint.condition.OnEndpointElementCondition;

public class OnActivatedMonitorCondition extends OnEndpointElementCondition {
    public static final String PREFIX = "titans.endpoint.monitor.";

    public OnActivatedMonitorCondition() {
        super("titans.endpoint.monitor.", ConditionalOnActivatedMonitor.class);
    }
}
