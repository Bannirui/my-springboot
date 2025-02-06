package com.github.bannirui.msb.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ResourceReleaseThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    public ResourceReleaseThreadFactory() {
        this.group = Thread.currentThread().getThreadGroup();
        this.namePrefix = "titans-res-release-" + poolNumber.getAndIncrement() + "-thread-";
    }

    public Thread newThread(Runnable r) {
        Thread t = new Thread(this.group, r, this.namePrefix + this.threadNumber.getAndIncrement(), 0L);
        if (t.isDaemon()) {
            t.setDaemon(false);
        }
        if (t.getPriority() != 5) {
            t.setPriority(5);
        }
        return t;
    }
}
