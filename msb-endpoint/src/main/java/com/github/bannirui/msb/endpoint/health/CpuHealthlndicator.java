package com.github.bannirui.msb.endpoint.health;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;

public class CpuHealthlndicator implements HealthIndicator {

    @Override
    public Health health() {
        Health health = new Health();
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        double load;
        try {
            Method method = OperatingSystemMXBean.class.getMethod("getSystemLoadAverage");
            load = (Double)method.invoke(operatingSystemMXBean);
            operatingSystemMXBean.getSystemLoadAverage();
        } catch (Throwable e) {
            load = -1.0D;
        }
        int cpu = operatingSystemMXBean.getAvailableProcessors();
        return load < (double)cpu ? health.up() : health.down();
    }
}
