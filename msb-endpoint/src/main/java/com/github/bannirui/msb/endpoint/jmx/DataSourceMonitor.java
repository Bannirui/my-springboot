package com.github.bannirui.msb.endpoint.jmx;

import com.github.bannirui.msb.orm.configuration.DynamicDataSource;
import com.zaxxer.hikari.HikariConfigMXBean;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.sql.DataSource;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class DataSourceMonitor implements MonitorForLogger, MonitorForCat, ApplicationContextAware {
    public static final String ENABLE_KEY = "msb.endpoint.monitor.datasource.enabled";
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public String getId() {
        return "Database Pool";
    }

    @Override
    public String getDescription() {
        return "数据库连接池";
    }

    @Override
    public Map<String, String> getProperties() {
        HashMap<String, String> properties = new HashMap<>();
        Map<String, DataSource> dataSources = this.applicationContext.getBeansOfType(DataSource.class, false, false);
        if(MapUtils.isEmpty(dataSources)) return properties;
        for (Map.Entry<String, DataSource> entry : dataSources.entrySet()) {
            String beanName = entry.getKey();
            if (!beanName.equals("DataSource") && beanName.endsWith("DataSource")) {
                beanName = beanName.substring(0, beanName.lastIndexOf("DataSource"));
            }
            DataSource dataSource = entry.getValue();
            try {
                this.monitorData(properties, beanName, dataSource);
            } catch (Exception e) {
            }
        }
        return properties;
    }

    @Override
    public Map<String, String> monitor() {
        return this.getProperties();
    }

    public void monitorData(Map<String, String> properties, String beanName, DataSource dataSource) {
        HikariDataSource hikariDataSource = null;
        if (dataSource instanceof HikariDataSource) {
            hikariDataSource = (HikariDataSource)dataSource;
        } else if (dataSource instanceof DynamicDataSource) {
            hikariDataSource = ((DynamicDataSource)dataSource).getDataSource();
        }
        if (hikariDataSource != null) {
            this.monitorConfigMXBean(properties, beanName, hikariDataSource.getHikariConfigMXBean());
            this.monitorPoolMXBean(properties, beanName, hikariDataSource.getHikariPoolMXBean());
        }
    }

    private void monitorConfigMXBean(Map<String, String> properties, String beanName, HikariConfigMXBean configMXBean) {
        if(Objects.isNull(configMXBean)) return;
        properties.put(beanName + ".ds.connectionTimeout", String.valueOf(configMXBean.getConnectionTimeout()));
        properties.put(beanName + ".ds.idleTimeout", String.valueOf(configMXBean.getIdleTimeout()));
        properties.put(beanName + ".ds.maximumPoolSize", String.valueOf(configMXBean.getMaximumPoolSize()));
        properties.put(beanName + ".ds.maxLifetime", String.valueOf(configMXBean.getMaxLifetime()));
        properties.put(beanName + ".ds.minimumIdle", String.valueOf(configMXBean.getMinimumIdle()));
        properties.put(beanName + ".ds.validationTimeout", String.valueOf(configMXBean.getValidationTimeout()));
        properties.put(beanName + ".ds.leakDetectionThreshold", String.valueOf(configMXBean.getLeakDetectionThreshold()));
    }

    private void monitorPoolMXBean(Map<String, String> properties, String beanName, HikariPoolMXBean poolMXBean) {
        if(Objects.isNull(poolMXBean)) return;
        properties.put(beanName + ".ds.activeConnections", String.valueOf(poolMXBean.getActiveConnections()));
        properties.put(beanName + ".ds.idleConnections", String.valueOf(poolMXBean.getIdleConnections()));
        properties.put(beanName + ".ds.threadsAwaitingConnection", String.valueOf(poolMXBean.getThreadsAwaitingConnection()));
        properties.put(beanName + ".ds.totalConnections", String.valueOf(poolMXBean.getTotalConnections()));
    }
}
