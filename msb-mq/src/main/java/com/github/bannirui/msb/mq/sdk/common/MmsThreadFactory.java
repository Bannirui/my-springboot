package com.github.bannirui.msb.mq.sdk.common;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MmsThreadFactory implements ThreadFactory {
    Logger logger = LoggerFactory.getLogger(MmsThreadFactory.class);
    private final AtomicLong threadIndex = new AtomicLong(0L);
    private final String threadNamePrefix;

    public MmsThreadFactory(final String threadNamePrefix) {
        this.threadNamePrefix = threadNamePrefix;
    }

    @Override
    public Thread newThread(Runnable r) {
        String suffix = "";
        try {
            suffix = String.valueOf(System.currentTimeMillis());
            suffix = suffix.substring(suffix.length() - 3);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread t = new Thread(r, this.threadNamePrefix + "_" + this.threadIndex.incrementAndGet() + "_" + suffix);
        t.setUncaughtExceptionHandler((t1, e) -> logger.error("uncaughtException in thread: {}", t1.getName(), e));
        return t;
    }
}
