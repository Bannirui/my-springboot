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

/**
 * 日志配置调整 监听Apollo配置
 * <ul>
 *     <li>console输出</li>
 *     <li>log level</li>
 * </ul>
 */
public class DynamicLogSystem {

    private static final Logger logger = LoggerFactory.getLogger(DynamicLogSystem.class);
    private static final String LOGGING_LEVEL_PREFIX = "logging.level";
    private LoggingSystem logbackLoggingSystem;

    public DynamicLogSystem(LoggingSystem loggingSystem) {
        this.logbackLoggingSystem = loggingSystem;
    }

    @PostConstruct
    public void initLogbackLoggingSystem() {}

    /**
     * 监听Apollo关于日志的配置变更.
     */
    @ApolloConfigChangeListener()
    public void logLevelOnChange(ConfigChangeEvent changeEvent) {
        for (String e : changeEvent.changedKeys()) {
            // console.log
            if(ConsoleAppender.CONSOLE_LOG_PROPERTY_KEY.equals(e)) {
                ConfigChange configChange = changeEvent.getChange(e);
                String consoleLogPropertyVal = configChange.getNewValue();
                if("true".equals(consoleLogPropertyVal)) {
                    ConsoleAppender.enable();
                }
            }
            // logging.level.root, like from debug to info
            if (e != null && e.startsWith(LOGGING_LEVEL_PREFIX)) {
                ConfigChange configChange = changeEvent.getChange(e);
                // root
                String levelName = e.substring(LOGGING_LEVEL_PREFIX.length()+1);
                if (configChange.getNewValue() == null) this.setLogLevel(levelName, "OFF");
                else this.setLogLevel(levelName, configChange.getNewValue());
            }
        }
    }

    /**
     * 日志告警级别
     * <ul>
     *     <li>logging.level.root=info</li>
     *     <li>logging.level.com.github.bannirui.msb=error</li>
     * </ul>
     * @param levelName root, com.github.bannirui.msb
     * @param level OFF INFO WARNING ERROR
     */
    public void setLogLevel(String levelName, String level) {
        try {
            this.logbackLoggingSystem.setLogLevel(levelName, LogLevel.valueOf(level.toUpperCase()));
        } catch (Exception e) {
            logger.warn("log级别设置失败 levelName:{} level:{}", levelName, level, e);
        }
    }
}
