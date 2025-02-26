package com.github.bannirui.msb.endpoint;

import com.github.bannirui.msb.endpoint.jmx.JmxClientLogger;
import com.github.bannirui.msb.endpoint.jmx.MonitorForLogger;
import com.github.bannirui.msb.env.MsbEnvironmentMgr;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.Order;

@Order(-2147483648)
public class LoggerScheduleRunner implements ApplicationRunner, ApplicationContextAware, DisposableBean {
    private ApplicationContext applicationContext;
    private ScheduledThreadPoolExecutor service;
    private final Runnable runnable = () -> {
        Logger log = JmxClientLogger.getLog();
        Map<String, MonitorForLogger> beansOfType = this.applicationContext.getBeansOfType(MonitorForLogger.class);
        for (Map.Entry<String, MonitorForLogger> entry : beansOfType.entrySet()) {
            Map<String, String> data = entry.getValue().monitor();
            if(MapUtils.isEmpty(data)) continue;
            StringBuilder datalog = new StringBuilder();
            datalog.append(MsbEnvironmentMgr.getAppName()).append("|");
            for (Map.Entry<String, String> dataEntry : data.entrySet()) {
                datalog.append((String)dataEntry.getKey());
                datalog.append("=");
                datalog.append((String)dataEntry.getValue());
                datalog.append(",");
            }
            datalog.append("timestamp=").append(System.currentTimeMillis());
            log.info(datalog.toString());
        }
    };

    public LoggerScheduleRunner() {
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.applicationContext = context;
    }

    @Override
    public void run(ApplicationArguments applicationArguments) {
        this.service = new ScheduledThreadPoolExecutor(1, r -> new Thread(r, "jmxMonitorScheduleThread"));
        this.service.scheduleAtFixedRate(this.runnable, 5L, 1L, TimeUnit.SECONDS);
    }

    @Override
    public void destroy() throws Exception {
        if (this.service != null) {
            this.service.shutdown();
        }
    }
}
