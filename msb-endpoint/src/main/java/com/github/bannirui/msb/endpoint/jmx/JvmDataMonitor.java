package com.github.bannirui.msb.endpoint.jmx;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.HashMap;
import java.util.Map;

public class JvmDataMonitor implements MonitorForLogger, MonitorForCat {
    private long ygcCount = 0L;
    private long fgcCount = 0L;

    public static String formatByte(double b) {
        double mb = b / 1048576.0D;
        return String.format("%.3f", mb);
    }

    @Override
    public Map<String, String> monitor() {
        Map<String, String> jmxDataMap = new HashMap<>(7);
        MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        long heapMemoryUsed = heapMemoryUsage.getUsed();
        jmxDataMap.put("jvm.heapMemory.used", formatByte((double)heapMemoryUsed));
        long nonHeapMemoryused = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getUsed();
        jmxDataMap.put("jvm.nonHeapMemory.used", formatByte((double)nonHeapMemoryused));
        long youngUsed = 0L;
        long edenUsed = 0L;
        long survivorUsed = 0L;
        if (ManagementFactory.getMemoryPoolMXBeans().get(3).getUsage() != null) {
            edenUsed = ManagementFactory.getMemoryPoolMXBeans().get(3).getUsage().getUsed();
        }
        if (ManagementFactory.getMemoryPoolMXBeans().get(4).getUsage() != null) {
            survivorUsed = ManagementFactory.getMemoryPoolMXBeans().get(4).getUsage().getUsed();
        }
        youngUsed = edenUsed + survivorUsed;
        long oldUsed = 0L;
        if (ManagementFactory.getMemoryPoolMXBeans().get(5).getUsage() != null) {
            oldUsed = ManagementFactory.getMemoryPoolMXBeans().get(5).getUsage().getUsed();
        }
        jmxDataMap.put("jvm.youngMemory.used", formatByte((double)youngUsed));
        jmxDataMap.put("jvm.oldMemory.used", formatByte((double)oldUsed));
        int currentThreadCount = ManagementFactory.getThreadMXBean().getThreadCount();
        jmxDataMap.put("jvm.currentThread.count", String.valueOf(currentThreadCount));
        long yGc = ManagementFactory.getGarbageCollectorMXBeans().get(0).getCollectionCount();
        long count = 0L;
        if (yGc > this.ygcCount) {
            count = yGc - this.ygcCount;
            this.ygcCount = yGc;
        }
        jmxDataMap.put("jvm.yGc.count", String.valueOf(count));
        long fullGc = ManagementFactory.getGarbageCollectorMXBeans().get(1).getCollectionCount();
        count = 0L;
        if (fullGc > this.fgcCount) {
            count = fullGc - this.fgcCount;
            this.fgcCount = fullGc;
        }
        jmxDataMap.put("jvm.fullGc.count", String.valueOf(count));
        return jmxDataMap;
    }

    @Override
    public String getId() {
        return "Jvm Data";
    }

    @Override
    public String getDescription() {
        return "Jvm 信息";
    }

    @Override
    public Map<String, String> getProperties() {
        return this.monitor();
    }
}
