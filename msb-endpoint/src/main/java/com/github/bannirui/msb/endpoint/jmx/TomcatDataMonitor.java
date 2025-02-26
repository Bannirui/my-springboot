package com.github.bannirui.msb.endpoint.jmx;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import javax.management.ObjectName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;

public class TomcatDataMonitor implements MonitorForLogger, MonitorForCat {
    private static final Logger logger = LoggerFactory.getLogger(TomcatDataMonitor.class);
    private int serverPort;

    @EventListener
    public void embeddedServletContainerInitializedEventListener(WebServerInitializedEvent event) {
        this.serverPort = event.getWebServer().getPort();
    }

    @Override
    public Map<String, String> monitor() {
        Map<String, String> jmxDataMap = new HashMap<>();
        if (this.serverPort != 0) {
            String objectNameStr = "Tomcat:type=Connector,port=" + this.serverPort;
            int currentThreadsBusy = 0;
            int currentThread = 0;
            int currentLeisureThreadCount = 0;
            try {
                int maxThreads = Integer.parseInt(ManagementFactory.getPlatformMBeanServer().getAttribute(new ObjectName(objectNameStr), "maxThreads").toString());
                Object currentThreadsBusyObj = ManagementFactory.getPlatformMBeanServer().getAttribute(new ObjectName(objectNameStr), "currentThreadsBusy");
                if (currentThreadsBusyObj != null) {
                    currentThreadsBusy = Integer.parseInt(currentThreadsBusyObj.toString());
                }
                Object currentThreadCountObj = ManagementFactory.getPlatformMBeanServer().getAttribute(new ObjectName(objectNameStr), "currentThreadCount");
                if (currentThreadCountObj != null) {
                    currentThread = Integer.parseInt(currentThreadCountObj.toString());
                }
                if (currentThread != 0) {
                    currentLeisureThreadCount = currentThread - currentThreadsBusy;
                }
                jmxDataMap.put("tomcat.maxThread.count", String.valueOf(maxThreads));
                jmxDataMap.put("tomcat.currentThreadsBusy.count", String.valueOf(currentThreadsBusy));
                jmxDataMap.put("tomcat.currentThreads.count", String.valueOf(currentThread));
                jmxDataMap.put("tomcat.currentLeisureThreads.count", String.valueOf(currentLeisureThreadCount));
            } catch (Exception e) {
                logger.info("JMX 获取Tomcat线程数据错误 errorMsg={}", e.getMessage());
            }
        }
        return jmxDataMap;
    }

    @Override
    public String getId() {
        return "Embedded Tomcat";
    }

    @Override
    public String getDescription() {
        return "Tomcat信息";
    }

    @Override
    public Map<String, String> getProperties() {
        return this.monitor();
    }
}
