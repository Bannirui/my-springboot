package com.github.bannirui.msb.endpoint.health;

import com.github.bannirui.msb.endpoint.condition.OnEndpointElementCondition;

public class OnActivatedHealthIndicatorCondition extends OnEndpointElementCondition {
    public static final String PREFIX = "msb.endpoint.health.";

    public OnActivatedHealthIndicatorCondition() {
        super(OnActivatedHealthIndicatorCondition.PREFIX, ConditionalOnActivatedHealthIndicator.class);
    }
}
