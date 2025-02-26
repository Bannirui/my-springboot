package com.github.bannirui.msb.endpoint.health;

import com.github.bannirui.msb.orm.configuration.DynamicDataSource;
import com.zaxxer.hikari.HikariConfigMXBean;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import javax.sql.DataSource;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class DataSourceHealthIndicator implements HealthIndicator, ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(DataSourceHealthIndicator.class);
    public static final String ENABLE_KEY = "msb.endpoint.health.datasource.enabled";
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Health health() {
        Health health = new Health();
        Map<String, DataSource> dataSources = this.applicationContext.getBeansOfType(DataSource.class, false, false);
        if (MapUtils.isEmpty(dataSources)) {
            return health.down();
        }
        boolean isUp = true;
        for (Map.Entry<String, DataSource> entry : dataSources.entrySet()) {
            String beanName = entry.getKey();
            if (!beanName.equals("DataSource") && beanName.endsWith("DataSource")) {
                beanName = beanName.substring(0, beanName.lastIndexOf("DataSource"));
            }
            DataSource dataSource = entry.getValue();
            try {
                this.monitorData(health, beanName, dataSource);
                boolean up = this.healthWithDetail(health, beanName, dataSource);
                if (!up) {
                    isUp = false;
                }
            } catch (Exception e) {
                health.withDetail(entry.getKey(), "DOWN");
                isUp = false;
            }
        }
        if (isUp) {
            health.up();
        } else {
            health.down();
        }
        return health;
    }

    public void monitorData(Health health, String beanName, DataSource dataSource) {
        HikariDataSource hikariDataSource = null;
        if (dataSource instanceof HikariDataSource) {
            hikariDataSource = (HikariDataSource) dataSource;
        } else if (dataSource instanceof DynamicDataSource) {
            hikariDataSource = ((DynamicDataSource) dataSource).getDataSource();
        }
        if (hikariDataSource != null) {
            this.monitorConfigMXBean(health, beanName, hikariDataSource.getHikariConfigMXBean());
            this.monitorPoolMXBean(health, beanName, hikariDataSource.getHikariPoolMXBean());
        }
    }

    public boolean healthWithDetail(Health health, String beanName, DataSource dataSource) {
        ResultSet resultSet = null;
        Connection connection = null;
        boolean isUp = true;
        boolean ret = false;
        try {
            if(Objects.isNull(connection=this.getConnection(dataSource))) {
                health.withDetail(beanName, "DOWN");
                return false;
            }
            DatabaseMetaData metaData = connection.getMetaData();
            if(Objects.nonNull(metaData)) {
                String databaseProductName = metaData.getDatabaseProductName();
                health.withDetail(beanName + ".databaseProductName", databaseProductName);
                resultSet = metaData.getTypeInfo();
                if (!resultSet.next()) {
                    health.withDetail(beanName, "DOWN");
                    isUp = false;
                } else {
                    health.withDetail(beanName, "UP");
                }
                return isUp;
            }
            health.withDetail(beanName, "DOWN");
            ret = false;
        } catch (Exception e) {
            health.withDetail(beanName, "DOWN");
            return false;
        } finally {
            this.closeResultSet(resultSet);
            this.closeConnection(connection);
        }
        return ret;
    }

    public void monitorConfigMXBean(Health health, String beanName, HikariConfigMXBean configMXBean) {
        if(Objects.isNull(configMXBean)) return;
        health.put(beanName + ".ds.poolName", configMXBean.getPoolName());
        health.put(beanName + ".ds.connectionTimeout", configMXBean.getConnectionTimeout() + " milliseconds");
        health.put(beanName + ".ds.idleTimeout", configMXBean.getIdleTimeout() + " milliseconds");
        health.put(beanName + ".ds.maximumPoolSize", String.valueOf(configMXBean.getMaximumPoolSize()));
        health.put(beanName + ".ds.maxLifetime", configMXBean.getMaxLifetime() + " milliseconds");
        health.put(beanName + ".ds.minimumIdle", String.valueOf(configMXBean.getMinimumIdle()));
        health.put(beanName + ".ds.validationTimeout", configMXBean.getValidationTimeout() + " milliseconds");
        health.put(beanName + ".ds.leakDetectionThreshold", configMXBean.getLeakDetectionThreshold() + " milliseconds");
    }

    public void monitorPoolMXBean(Health health, String beanName, HikariPoolMXBean poolMXBean) {
        if(Objects.isNull(poolMXBean)) return;
        health.put(beanName + ".ds.activeConnections", String.valueOf(poolMXBean.getActiveConnections()));
        health.put(beanName + ".ds.idleConnections", String.valueOf(poolMXBean.getIdleConnections()));
        health.put(beanName + ".ds.threadsAwaitingConnection", String.valueOf(poolMXBean.getThreadsAwaitingConnection()));
        health.put(beanName + ".ds.totalConnections", String.valueOf(poolMXBean.getTotalConnections()));
    }

    public void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                logger.error("Could not close JDBC Connection", e);
            }
        }
    }

    public void closeResultSet(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                logger.error("Could not close JDBC ResultSet", e);
            }
        }
    }

    private Connection getConnection(DataSource dataSource) {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            return null;
        }
    }
}
