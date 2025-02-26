package com.github.bannirui.msb.endpoint.jmx;

import ch.qos.logback.classic.LoggerContext;
import com.github.bannirui.msb.env.MsbEnvironmentMgr;
import java.lang.reflect.Method;
import java.net.URL;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmxClientLogger {
    private static Logger log = createJmxLogger("JmxLogger");
    public static final String JMX_CLIENT_LOGGER_NAME = "JmxLogger";
    public static final String LOGBACK_RESOURCE_FILE = "jmx-logback.xml";

    private static Logger createJmxLogger(final String loggerName) {
        System.setProperty("appName", MsbEnvironmentMgr.getAppName());
        ILoggerFactory iLoggerFactory = LoggerFactory.getILoggerFactory();
        try {
            if (iLoggerFactory instanceof LoggerContext) {
                Class<?> joranConfigurator = null;
                Class<?> context = Class.forName("ch.qos.logback.core.Context");
                Object joranConfiguratoroObj = null;
                joranConfigurator = Class.forName("ch.qos.logback.classic.joran.JoranConfigurator");
                joranConfiguratoroObj = joranConfigurator.newInstance();
                Method setContext = joranConfiguratoroObj.getClass().getMethod("setContext", context);
                setContext.invoke(joranConfiguratoroObj, iLoggerFactory);
                URL url = JmxClientLogger.class.getClassLoader().getResource("jmx-logback.xml");
                Method doConfigure = joranConfiguratoroObj.getClass().getMethod("doConfigure", URL.class);
                doConfigure.invoke(joranConfiguratoroObj, url);
            }
        } catch (Exception e) {
            System.err.println(e);
        }
        return LoggerFactory.getLogger("JmxLogger");
    }

    public static Logger getLog() {
        return log;
    }
}
