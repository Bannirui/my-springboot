package com.github.bannirui.msb.log.configuration;

import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import com.github.bannirui.msb.log.appender.ConsoleAppender;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;

public class DynamicLogSystem {

    private static final Logger logger = LoggerFactory.getLogger(DynamicLogSystem.class);
    private static final String LOGGING_LEVEL_PREFIX = "logging.level";
    private LoggingSystem logbackLoggingSystem;

    public DynamicLogSystem(LoggingSystem loggingSystem) {
        this.logbackLoggingSystem = loggingSystem;
    }

    @PostConstruct
    public void initLogbackLoggingSystem() {

    }

    @ApolloConfigChangeListener({"application"})
    private void logLevelOnChange(ConfigChangeEvent changeEvent) {
        for (String e : changeEvent.changedKeys()) {
            if (e != null && e.startsWith(ConsoleAppender.CONSOLE_LOG)) {
                ConfigChange configChange = changeEvent.getChange(e);
                String levelName = configChange.getNewValue();
                ConsoleAppender.showConsoleLog(levelName);
            }
            if (e != null && e.startsWith(LOGGING_LEVEL_PREFIX)) {
                ConfigChange configChange = changeEvent.getChange(e);
                String levelName = e.substring(LOGGING_LEVEL_PREFIX.length(), e.length());
                if (configChange.getNewValue() == null) {
                    this.setLogLevel(levelName, "OFF");
                } else {
                    this.setLogLevel(levelName, configChange.getNewValue());
                }
            }
        }
    }

    public void setLogLevel(String levelName, String levelStr) {
        try {
            this.logbackLoggingSystem.setLogLevel(levelName, LogLevel.valueOf(levelStr.toUpperCase()));
        } catch (Exception e) {
            logger.warn("log级别设置失败 levelName:{} level:{}", levelName, levelStr, e);
        }
    }
}
