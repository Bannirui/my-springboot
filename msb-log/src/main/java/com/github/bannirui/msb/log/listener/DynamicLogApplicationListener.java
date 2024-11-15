package com.github.bannirui.msb.log.listener;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AsyncMpscAppender;
import com.github.bannirui.msb.common.constant.EnvType;
import com.github.bannirui.msb.common.env.EnvironmentMgr;
import com.github.bannirui.msb.common.properties.bind.PropertyBinder;
import com.github.bannirui.msb.log.appender.AsyncFlushRollingFileAppender;
import com.github.bannirui.msb.log.appender.ConsoleAppender;
import com.github.bannirui.msb.log.configuration.AsyncAppenderProperty;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.ConfigurableEnvironment;

public class DynamicLogApplicationListener implements GenericApplicationListener, PriorityOrdered {

    private static AtomicBoolean environmentPreparedEventReentry = new AtomicBoolean(false);
    private static AtomicBoolean applicationPreparedEventReentry = new AtomicBoolean(false);
    private static AtomicBoolean applicationStartedEventReentry = new AtomicBoolean(false);

    private Logger logger = LoggerFactory.getLogger(DynamicLogApplicationListener.class);
    private LoggingSystem loggingSystem;
    private ConfigurableEnvironment environment;

    private static final String SYSTEM_LOGGING_LEVEL = "system.logging.level";

    public DynamicLogApplicationListener() {
    }

    @Override
    public boolean supportsEventType(ResolvableType eventType) {
        return true;
    }

    @Override
    public boolean supportsSourceType(Class<?> sourceType) {
        return true;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationEnvironmentPreparedEvent e && environmentPreparedEventReentry.compareAndSet(false, true)) {
            this.onApplicationEnvironmentPreparedEvent(e);
        }
        if (event instanceof ApplicationStartedEvent e && applicationStartedEventReentry.compareAndSet(false, true)) {
            this.onApplicationStartedEvent(e);
        }
    }

    @Override
    public int getOrder() {
        return -2147483624;
    }

    private void onApplicationEnvironmentPreparedEvent(ApplicationEnvironmentPreparedEvent event) {
        PropertyBinder binder = new PropertyBinder(event.getEnvironment());
        BindResult<String> bind = binder.bind(ConsoleAppender.CONSOLE_LOG, Bindable.of(String.class));
        ConsoleAppender.showConsoleLog(bind.orElse("false"));
    }

    /**
     * 应用启动成功 这个时候容器执行过了{@link AbstractApplicationContext#refresh()}
     * 已经可以获取到{@link org.springframework.context.annotation.Import}和{@link org.springframework.context.annotation.Bean}注入的实例
     */
    private void onApplicationStartedEvent(ApplicationStartedEvent event) {
        this.checkAsyncFileAppenderExist();
        /**
         * 从容器中获取到{@link com.github.bannirui.msb.log.configuration.LogConfiguration}注入的{@link LoggingSystem}实例
         */
        this.loggingSystem = event.getApplicationContext().getBeanFactory().getBean(LoggingSystem.class);
        this.environment = event.getApplicationContext().getEnvironment();
        this.setDefaultRootLevel();
        this.updateLogLevelByConfig(SYSTEM_LOGGING_LEVEL);

        this.cancelSystemLogSet();
        this.updateLogLevelByConfig("logging.level");
        this.addAsyncAppender();
        this.cancelFileAppenderImmediateFlush();
    }

    private void checkAsyncFileAppenderExist() {
        Appender<ILoggingEvent> fileAppender = this.getFileAppender();
        if (!(fileAppender instanceof AsyncFlushRollingFileAppender)) {
            this.logger.error("没有检测到AsyncFlushRollingFileAppender");
        }
    }

    private Appender<ILoggingEvent> getFileAppender() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        return loggerContext.getLogger("root").getAppender("file");
    }

    private void setDefaultRootLevel() {
        if (!EnvironmentMgr.getEnv().contains(EnvType.DEV) && !EnvironmentMgr.getEnv().contains(EnvType.DEFAULT)) {
            this.setLogLevel("ROOT", "error");
        } else {
            this.setLogLevel("ROOT", "info");
        }
    }

    private void setLogLevel(String levelName, String levelStr) {
        try {
            this.loggingSystem.setLogLevel(levelName, LogLevel.valueOf(levelStr.toUpperCase(Locale.ROOT)));
        } catch (Exception e) {
            this.logger.warn("log级别设置失败 name={} level={}", levelName, levelStr);
        }
    }

    private void updateLogLevelByConfig(String configKey) {
        Map<String, String> levels = this.getLoggingConfig(configKey);
        levels.forEach(this::setLogLevel);
    }

    private Map<String, String> getLoggingConfig(String configKey) {
        PropertyBinder binder = new PropertyBinder(this.environment);
        return binder.bind(configKey, Bindable.mapOf(String.class, String.class)).orElseGet(Collections::emptyMap);
    }

    private void cancelSystemLogSet() {
        Map<String, String> levels = this.getLoggingConfig(SYSTEM_LOGGING_LEVEL);
        levels.forEach((name, level) -> {
            this.setLogLevel(name, "OFF");
        });
    }

    private void addAsyncAppender() {
        if (AsyncAppenderProperty.isEnable()) {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger("root");
            Appender<ILoggingEvent> fileAppender = rootLogger.getAppender("file");
            rootLogger.detachAppender("file");
            AsyncMpscAppender asyncAppender = this.createAsyncAppender(fileAppender);
            rootLogger.addAppender(asyncAppender);
        }
    }

    public AsyncMpscAppender createAsyncAppender(Appender<ILoggingEvent> fileAppender) {
        AsyncMpscAppender ret = new AsyncMpscAppender();
        ret.setName("asyncMpscAppender");
        ret.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        ret.setQueueSize(AsyncAppenderProperty.getQueueSize());
        ret.setNeverBlock(AsyncAppenderProperty.bNeverBlock());
        ret.setDiscardingThreshold(AsyncAppenderProperty.getDiscardingThreshold());
        ret.addAppender(fileAppender);
        ret.start();
        return ret;
    }

    private void cancelFileAppenderImmediateFlush() {
        Appender<ILoggingEvent> fileAppender = this.getFileAppender();
        if (fileAppender instanceof AsyncFlushRollingFileAppender appender) {
            appender.setImmediateFlush(false);
        }
    }
}
