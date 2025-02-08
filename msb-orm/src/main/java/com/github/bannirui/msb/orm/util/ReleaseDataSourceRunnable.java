package com.github.bannirui.msb.orm.util;

import com.dangdang.ddframe.rdb.sharding.jdbc.MasterSlaveDataSource;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import java.lang.reflect.Field;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

public class ReleaseDataSourceRunnable implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(ReleaseDataSourceRunnable.class);
    private DataSource dataSource;
    private long startTime;
    private long forceCloseWaitSeconds = 300L;

    public ReleaseDataSourceRunnable(DataSource dataSource, long forceCloseWaitSeconds) {
        this.dataSource = dataSource;
        this.forceCloseWaitSeconds = forceCloseWaitSeconds;
        this.startTime = System.currentTimeMillis();
    }

    private boolean forceClose() {
        if (this.forceCloseWaitSeconds < 0L) {
            return false;
        } else if (this.forceCloseWaitSeconds == 0L) {
            return true;
        } else {
            return (System.currentTimeMillis() - this.startTime) / 1000L > this.forceCloseWaitSeconds;
        }
    }

    public void run() {
        try {
            if (this.dataSource instanceof HikariDataSource hikariDataSource) {
                HikariPoolMXBean hikariPoolMXBean = hikariDataSource.getHikariPoolMXBean();
                int activeConnections = hikariPoolMXBean.getActiveConnections();
                if (activeConnections > 0 && !this.forceClose()) {
                    DataSourceHelp.asyncRelease(this);
                } else {
                    hikariDataSource.close();
                }
            } else if (this.dataSource instanceof MasterSlaveDataSource masterSlaveDataSource) {
                Field masterDSField = ReflectionUtils.findField(MasterSlaveDataSource.class, "masterDataSource");
                ReflectionUtils.makeAccessible(masterDSField);
                HikariDataSource masterDS = (HikariDataSource)ReflectionUtils.getField(masterDSField, masterSlaveDataSource);
                DataSourceHelp.asyncRelease(new ReleaseDataSourceRunnable(masterDS, this.forceCloseWaitSeconds));
                Field slaveDataSourcesField = ReflectionUtils.findField(MasterSlaveDataSource.class, "slaveDataSources");
                ReflectionUtils.makeAccessible(slaveDataSourcesField);
                List<?> slaveDataSources = (List)ReflectionUtils.getField(slaveDataSourcesField, masterSlaveDataSource);
                slaveDataSources.forEach(slaveDataSource-> DataSourceHelp.asyncRelease(new ReleaseDataSourceRunnable((HikariDataSource)slaveDataSource, this.forceCloseWaitSeconds)));
            }
        } catch (Exception e) {
            log.error("An exception occurred while releasing the old datasource:", e);
        }
    }
}
