package com.github.bannirui.msb.common.startup.monitor;

import com.github.bannirui.msb.common.env.EnvironmentMgr;
import com.github.bannirui.msb.common.startup.MsbImportSelectorController;
import com.github.bannirui.msb.common.startup.monitor.param.EndMonitorParam;
import com.github.bannirui.msb.common.startup.monitor.param.StartMonitorParam;
import com.github.bannirui.msb.common.util.HttpUtil;
import com.github.bannirui.msb.common.util.JsonUtil;
import com.github.bannirui.msb.common.util.StringUtil;
import com.github.bannirui.msb.common.util.VersionUtil;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

public class AppInfoMonitor implements EnvironmentAware {
    private static final Logger logger = LoggerFactory.getLogger(AppInfoMonitor.class);
    private static String id;
    private static final String MONITOR_SERVER_URL = "monitor.server.url";
    private static final String START_MONITOR_URI = "startMonitor";
    private static final String END_MONITOR_URI = "endMonitor";
    private ConfigurableEnvironment env;

    public AppInfoMonitor() {
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.env = (ConfigurableEnvironment) environment;
    }

    @EventListener
    public void contextRefreshedEventListener(ContextRefreshedEvent contextClosedEvent) {
        (new Thread(() -> {
            this.startInfoMonitor(contextClosedEvent);
        }, "appinfo-monitor-thread")).start();
    }

    private void startInfoMonitor(ContextRefreshedEvent contextClosedEvent) {
        StartMonitorParam startMonitorParam = new StartMonitorParam();
        startMonitorParam.setAppId(EnvironmentMgr.getAppName());
        if (contextClosedEvent.getApplicationContext().getParent() == null) {
            try {
                InetAddress address = InetAddress.getLocalHost();
                startMonitorParam.setServerIp(address.getHostAddress());
            } catch (UnknownHostException e) {
                logger.error("App 信息监听 IP地址获取失败 errorMsg=", e);
            }

            try {
                String titansVersion = VersionUtil.getVersion();
                startMonitorParam.setTitansVersion(titansVersion);
                Set<String> enableModules = MsbImportSelectorController.getEnableModules();
                if (enableModules != null && enableModules.size() > 0) {
                    startMonitorParam.setTitansModules(enableModules.toString().replaceAll("\\[", "").replaceAll("\\]", ""));
                }

                startMonitorParam.setStartTime(Calendar.getInstance().getTime().getTime());
                id = HttpUtil.sendPostJson(EnvironmentMgr.getProperty(this.env, "monitor.server.url") + "startMonitor",
                    JsonUtil.toJSON(startMonitorParam));
                if (StringUtil.isNotEmpty(id)) {
                    logger.info("App Titans版本信息 启动事件上报成功 ");
                } else {
                    logger.info("App Titans版本信息 启动事件上报失败 ");
                }
            } catch (Exception var5) {
                Exception e = var5;
                logger.info("App 信息监听 Http发送失败 errorMsg={}", e);
            }
        }

    }

    @EventListener
    public void contextClosedEventListEven(ContextClosedEvent contextClosedEvent) {
        logger.info("App 容器正在关闭 ");
        if (StringUtil.isNotEmpty(id)) {
            EndMonitorParam endMonitorParam = new EndMonitorParam();
            endMonitorParam.setId(Integer.parseInt(id));
            endMonitorParam.setEndTime(Calendar.getInstance().getTime().getTime());

            try {
                String result = HttpUtil.sendPostJson(EnvironmentMgr.getProperty(this.env, "monitor.server.url") + "endMonitor",
                    JsonUtil.toJSON(endMonitorParam));
                if (StringUtil.isNotEmpty(result)) {
                    logger.info("App Titans版本信息 销毁事件发送成功 ");
                }
            } catch (IOException e) {
                logger.error("App 关闭时监听信息 Http发送错误 errorMsg=", e);
            }
        } else {
            logger.error("App 关闭时监听信息发送失败");
        }
    }
}
