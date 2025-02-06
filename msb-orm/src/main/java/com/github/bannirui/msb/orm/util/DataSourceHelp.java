package com.github.bannirui.msb.orm.util;

import com.github.bannirui.msb.util.ResourceReleaseThreadFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataSourceHelp {
    private static final Logger log = LoggerFactory.getLogger(DataSourceHelp.class);
    private static final char[] ID_CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    public static final String DATASOURCE_BEAN_SUFFIX = "DataSource";
    private static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1, new ResourceReleaseThreadFactory());

    public static void asyncRelease(ReleaseDataSourceRunnable releaseDSRunnable) {
        scheduledExecutorService.schedule(releaseDSRunnable, 2L, TimeUnit.SECONDS);
    }

    public static void shutdown() {
        if (!scheduledExecutorService.isShutdown()) {
            scheduledExecutorService.shutdown();
            log.info("close task of < release useless database connection pool > ");
        }
    }

    public static String generatePoolName(String prefix) {
        prefix = prefix != null && !prefix.isEmpty() ? prefix + "-HikariPool-" : "HikariPool-";
        synchronized(System.getProperties()) {
            String next = String.valueOf(Integer.getInteger("com.zaxxer.hikari.pool_number", 0) + 1);
            System.setProperty("com.zaxxer.hikari.pool_number", next);
            return prefix + next;
        }
    }

    public static String generateDataSourceBeanName(String dataSourceName) {
        return dataSourceName == null ? null : dataSourceName + "DataSource";
    }
}
