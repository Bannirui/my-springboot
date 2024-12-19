package com.github.bannirui.msb.log.listener;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AsyncMpscAppender;
import com.github.bannirui.msb.common.constant.AppEventListenerSort;
import com.github.bannirui.msb.common.constant.EnvType;
import com.github.bannirui.msb.common.env.MsbEnvironmentMgr;
import com.github.bannirui.msb.common.properties.bind.PropertyBinder;
import com.github.bannirui.msb.log.appender.FileAppender;
import com.github.bannirui.msb.log.appender.ConsoleAppender;
import com.github.bannirui.msb.log.configuration.AsyncFileAppenderCfg;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.ConfigurableEnvironment;

public class MsbLogApplicationListener implements GenericApplicationListener, Ordered {

    private static AtomicBoolean environmentPreparedEventReentry = new AtomicBoolean(false);
    private static AtomicBoolean applicationPreparedEventReentry = new AtomicBoolean(false);
    private static AtomicBoolean applicationStartedEventReentry = new AtomicBoolean(false);

    private static final Logger logger = LoggerFactory.getLogger(MsbLogApplicationListener.class);
    private LoggingSystem loggingSystem;
    private ConfigurableEnvironment environment;

    private static final String SYSTEM_LOGGING_LEVEL = "system.logging.level";
    /**
     * logback-spring.xml中配置策略处理器
     * <ul>
     *     <li>控制台输出日志 console</li>
     *     <li>文件输出日志 file</li>
     *     <li>日志链路采集 cat</li>
     * </ul>
     */
    private static final String console_appender_name = "console";
    private static final String file_appender_name = "file";
    private static final String cat_appender_name = "cat";

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
        } else if (event instanceof ApplicationPreparedEvent e && applicationPreparedEventReentry.compareAndSet(false, true)) {
            this.onApplicationPreparedEvent(e);
        } else if (event instanceof ApplicationStartedEvent e && applicationStartedEventReentry.compareAndSet(false, true)) {
            this.onApplicationStartedEvent(e);
        }
    }

    @Override
    public int getOrder() {
        return AppEventListenerSort.CAT_LOG;
    }

    private void onApplicationEnvironmentPreparedEvent(ApplicationEnvironmentPreparedEvent event) {
        /**
         * 启用控制台日志
         * <ul>
         *     <li>msb框架配置文件中指定console.log=true 不推荐 业务型配置不要混合在框架中</li>
         * </ul>
         */
        PropertyBinder binder = new PropertyBinder(event.getEnvironment());
        BindResult<String> bind = binder.bind(ConsoleAppender.CONSOLE_LOG_PROPERTY_KEY, Bindable.of(String.class));
        if("true".equals(bind.orElse("false"))) {
            // 启用日志控制台策略
            ConsoleAppender.enable();
        }
    }

    private void onApplicationPreparedEvent(ApplicationPreparedEvent event) {
        /**
         * 从容器中获取到{@link com.github.bannirui.msb.log.configuration.LogConfiguration}注入的{@link LoggingSystem}实例
         */
        this.loggingSystem = event.getApplicationContext().getBeanFactory().getBean(LoggingSystem.class);
        this.environment = event.getApplicationContext().getEnvironment();
        this.setDefaultRootLevel();
        this.updateLogLevelByConfig(SYSTEM_LOGGING_LEVEL);
    }

    private void onApplicationStartedEvent(ApplicationStartedEvent event) {
        this.cancelSystemLogSet();
        /**
         * 配置文件中设置日志级别
         * <ul>
         *     <li>设置所有 logging.level.root=info</li>
         *     <li>设置到包级别 logging.level.com.github=error</li>
         * </ul>
         */
        this.updateLogLevelByConfig("logging.level");
        // 日志的文件策略
        this.addAsyncAppender();
        this.cancelFileAppenderImmediateFlush();
    }

    /**
     * 日志策略是否存在文件策略{@link FileAppender}
     * @return <t>true</t>标识配置了日志文件策略 <t>false</t>标识没有配置日志文件策略
     */
    private boolean checkAsyncFileAppenderExist() {
        Appender<ILoggingEvent> fileAppender = this.getFileAppender();
        return fileAppender instanceof FileAppender;
    }

    /**
     * 日志的文件策略.
     */
    private Appender<ILoggingEvent> getFileAppender() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        return loggerContext.getLogger("root").getAppender(MsbLogApplicationListener.file_appender_name);
    }

    /**
     * 设置日志全局级别.
     */
    private void setDefaultRootLevel() {
        if (!MsbEnvironmentMgr.getEnv().contains(EnvType.DEV) && !MsbEnvironmentMgr.getEnv().contains(EnvType.DEFAULT)) {
            this.setLogLevel("ROOT", "error");
        } else {
            this.setLogLevel("ROOT", "info");
        }
    }

    /**
     * 设置日志级别.
     * @param levelName like, root
     * @param levelStr like, info
     */
    private void setLogLevel(String levelName, String levelStr) {
        try {
            this.loggingSystem.setLogLevel(levelName, LogLevel.valueOf(levelStr.toUpperCase(Locale.ROOT)));
        } catch (Exception e) {
            this.logger.warn("log级别设置失败 name={} level={}", levelName, levelStr, e);
        }
    }

    /**
     * 设置日志级别.
     * @param propertyKey <ul>
     *                    <li>system.logging.level</li>
     *                    <li>logging.level</li>
     * </ul>
     */
    private void updateLogLevelByConfig(String propertyKey) {
        /**
         * root=info
         * com.github=error
         */
        Map<String, String> levels = this.getLoggingConfig(propertyKey);
        levels.forEach(this::setLogLevel);
    }

    /**
     * Spring Boot的Env中所有配置的key属性前缀是configKey的后缀及对应的配置值.
     * 比如有配置如
     * <ul>
     *     <li>logging.level.root=info</li>
     *     <li>logging.level.com.github=error</li>
     * </ul>
     * 关于解析结果
     * <ul>
     *     <li>当key为logging.level.root时 结果为info</li>
     *     <li>当key为logging.level时 结果为root=info</li>
     * </ul>
     * @param configKey <ul>
     *                  <li>system.logging.level</li>
     *                  <li>logging.level</li>
     * </ul>
     */
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

    /**
     * 为日志添加文件策略.
     */
    private void addAsyncAppender() {
        if (AsyncFileAppenderCfg.isEnable()) {
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
        ret.setQueueSize(AsyncFileAppenderCfg.getQueueSize());
        ret.setNeverBlock(AsyncFileAppenderCfg.bNeverBlock());
        ret.setDiscardingThreshold(AsyncFileAppenderCfg.getDiscardingThreshold());
        ret.addAppender(fileAppender);
        ret.start();
        return ret;
    }

    private void cancelFileAppenderImmediateFlush() {
        Appender<ILoggingEvent> fileAppender = this.getFileAppender();
        if (fileAppender instanceof FileAppender appender) {
            appender.setImmediateFlush(false);
        }
    }
}
