package com.github.bannirui.msb.endpoint.health;

public class MemoryHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        Health health = new Health();
        Runtime runtime = Runtime.getRuntime();
        long freeMemory = runtime.freeMemory();
        long totalMemory = runtime.totalMemory();
        long maxMemory = runtime.maxMemory();
        boolean ok = maxMemory - (totalMemory - freeMemory) > 2048L;
        if (ok) {
            health.up();
        } else {
            health.down();
        }
        health.withDetail("max", maxMemory / 1024L / 1024L + "M");
        health.withDetail("total", totalMemory / 1024L / 1024L + "M");
        health.withDetail("used", totalMemory / 1024L / 1024L - freeMemory / 1024L / 1024L + "M");
        health.withDetail("free", freeMemory / 1024L / 1024L + "M");
        return health;
    }
}
