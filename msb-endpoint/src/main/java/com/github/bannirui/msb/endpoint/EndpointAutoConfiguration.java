package com.github.bannirui.msb.endpoint;

import com.github.bannirui.msb.endpoint.health.DataSourceHealthIndicator;
import com.github.bannirui.msb.endpoint.jmx.DataSourceMonitor;
import javax.sql.DataSource;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

@Configuration
@AutoConfigureAfter({HealthIndicatorAutoConfig.class, MonitorComponentAutoConfig.class})
@Import({EndpointRunner.class, LoggerScheduleRunner.class})
public class EndpointAutoConfiguration implements ApplicationContextAware, EnvironmentAware {
    private Environment env;

    @Override
    public void setEnvironment(Environment environment) {
        this.env = environment;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ConfigurableApplicationContext context = (ConfigurableApplicationContext)applicationContext;
        Boolean enabledDsHealth = this.env.getProperty("msb.endpoint.health.datasource.enabled", Boolean.class, true);
        if (enabledDsHealth) {
            String[] dataSourceBeans = applicationContext.getBeanNamesForType(DataSource.class);
            if (dataSourceBeans.length > 0) {
                DataSourceHealthIndicator dataSourceHealthIndicator = new DataSourceHealthIndicator();
                dataSourceHealthIndicator.setApplicationContext(applicationContext);
                context.getBeanFactory().registerSingleton("dataSourceHealthIndicator", dataSourceHealthIndicator);
            }
        }
        Boolean enabledDsMonitor = this.env.getProperty("msb.endpoint.monitor.datasource.enabled", Boolean.class, true);
        if (enabledDsMonitor) {
            String[] dataSourceBeans = applicationContext.getBeanNamesForType(DataSource.class);
            if (dataSourceBeans.length > 0) {
                DataSourceMonitor dataSourceMonitor = new DataSourceMonitor();
                dataSourceMonitor.setApplicationContext(applicationContext);
                context.getBeanFactory().registerSingleton("dataSourceMonitor", dataSourceMonitor);
            }
        }
    }
}
