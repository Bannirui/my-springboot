package com.github.bannirui.msb.endpoint.health;

import org.apache.dubbo.common.status.Status;
import org.apache.dubbo.rpc.protocol.dubbo.status.ServerStatusChecker;

public class DubboHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        Health health = new Health();
        Status serverStatus = (new ServerStatusChecker()).check();
        health.withDetail("serverStatusMessage", serverStatus.getMessage());
        if (serverStatus.getLevel().equals(Status.Level.OK)) {
            health.up();
            health.withDetail("threadPoolStatus", "UP");
            health.withDetail("registryStatus", "UP");
            health.withDetail("serverStatus", "UP");
        } else {
            health.down();
            if (!serverStatus.getLevel().equals(Status.Level.OK)) {
                health.withDetail("serverStatus", "DOWN");
            }
        }
        return health;
    }
}
