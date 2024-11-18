package com.github.bannirui.msb.common;

import com.github.bannirui.msb.common.constant.AppEventListenerSort;
import com.github.bannirui.msb.common.env.EnvironmentMgr;
import com.github.bannirui.msb.common.util.ArrayUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.boot.logging.LogFile;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingInitializationContext;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.logging.LoggingSystemProperties;
import org.springframework.boot.logging.logback.LogbackLoggingSystem;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

public class LogBackConfigListener implements GenericApplicationListener {
    public static final String CONFIG_PROPERTY = "logging.config";
    public static final String REGISTER_SHUTDOWN_HOOK_PROPERTY = "logging.register-shutdown-hook";
    public static final String PATH_PROPERTY = "logging.path";
    public static final String FILE_PROPERTY = "logging.file";
    public static final String PID_KEY = "PID";
    public static final String EXCEPTION_CONVERSION_WORD = "LOG_EXCEPTION_CONVERSION_WORD";
    public static final String LOG_FILE = "LOG_FILE";
    public static final String LOG_PATH = "LOG_PATH";
    public static final String CONSOLE_LOG_PATTERN = "CONSOLE_LOG_PATTERN";
    public static final String FILE_LOG_PATTERN = "FILE_LOG_PATTERN";
    public static final String LOG_LEVEL_PATTERN = "LOG_LEVEL_PATTERN";
    public static final String LOGGING_SYSTEM_BEAN_NAME = "springBootLoggingSystem";
    private static MultiValueMap<LogLevel, String> LOG_LEVEL_LOGGERS = new LinkedMultiValueMap<>();
    private static AtomicBoolean shutdownHookRegistered = new AtomicBoolean(false);
    private static Class<?>[] event_types;
    private static Class<?>[] source_types;
    private final Log logger = LogFactory.getLog(this.getClass());
    private LoggingSystem loggingSystem;
    private boolean parseArgs = true;
    private LogLevel springBootLogLevel = null;
    public static final String LOG_LEVEL_KEY_PREFIX = "logging.level.";
    /**
     * 整合Apollo的时机在下面两个时机之间
     * <ul>
     *     <li>{@link ApplicationEnvironmentPreparedEvent}</li>
     *     <li>{@link org.springframework.boot.context.event.ApplicationContextInitializedEvent}</li>
     * </ul>
     * 因此在{@link ApplicationPreparedEvent}时机可以用Apollo的配置了
     */
    public static final String APOLLO_PROPERTY_SOURCE_NAME = "ApolloPropertySources";
    /**
     * 环境变量 console.log
     * 控制终端日志开关的key
     * <ul>
     *     <li>启动参数 --console.log=true</li>
     *     <li>VM参数 -Dconsole.log=true</li>
     * </ul>
     */
    private static final String console_log_option_key = "console.log";

    static {
        LOG_LEVEL_LOGGERS.add(LogLevel.INFO, "org.springframework.boot");
        LOG_LEVEL_LOGGERS.add(LogLevel.INFO, "org.springframework");
        LOG_LEVEL_LOGGERS.add(LogLevel.INFO, "org.apache.tomcat");
        LOG_LEVEL_LOGGERS.add(LogLevel.INFO, "org.apache.catalina");
        LOG_LEVEL_LOGGERS.add(LogLevel.INFO, "org.eclipse.jetty");
        LOG_LEVEL_LOGGERS.add(LogLevel.INFO, "org.hibernate.tool.hbm2ddl");
        LOG_LEVEL_LOGGERS.add(LogLevel.INFO, "org.hibernate.SQL");
        event_types = new Class[] {
            ApplicationStartingEvent.class,
            ApplicationEnvironmentPreparedEvent.class,
            ApplicationPreparedEvent.class,
            ContextClosedEvent.class,
            ApplicationFailedEvent.class,
            ApplicationStartedEvent.class
        };
        source_types = new Class[] {SpringApplication.class, ApplicationContext.class};
    }

    public LogBackConfigListener() {
    }

    public int getOrder() {
        return AppEventListenerSort.MSB_BASE_LOG;
    }

    @Override
    public boolean supportsEventType(ResolvableType eventType) {
        return this.isAssignableFrom(eventType.getRawClass(), event_types);
    }

    @Override
    public boolean supportsSourceType(Class<?> sourceType) {
        return this.isAssignableFrom(sourceType, source_types);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationStartingEvent e) {
            this.onApplicationStartingEvent(e);
        } else if (event instanceof ApplicationEnvironmentPreparedEvent e) {
            this.onApplicationEnvironmentPreparedEvent(e);
        } else if (event instanceof ApplicationPreparedEvent e) {
            this.onApplicationPreparedEvent(e);
        } else if (event instanceof ApplicationStartedEvent e) {
            ConfigurableEnvironment environment = e.getApplicationContext().getEnvironment();
            LoggingSystem loggingSystem = e.getApplicationContext().getBean(LoggingSystem.class);
            if (environment.getPropertySources().contains(APOLLO_PROPERTY_SOURCE_NAME)) {
                String[] keys =
                    ((EnumerablePropertySource<?>) environment.getPropertySources().get(APOLLO_PROPERTY_SOURCE_NAME)).getPropertyNames();
                // like, INFO=false, ERROR=true
                Map<String, String> levels = new HashMap<>();
                if (!ArrayUtil.isEmpty(keys)) {
                    for (String key : keys) {
                        if (key.startsWith(LOG_LEVEL_KEY_PREFIX)) {
                            levels.put(key.substring(LOG_LEVEL_KEY_PREFIX.length()), environment.getProperty(key));
                        }
                    }
                }
                levels.forEach((k, v) -> this.setLogLevel(loggingSystem, environment, k, v));
            }
        } else if (event instanceof ApplicationFailedEvent) {
            this.onApplicationFailedEvent();
        } else if (event instanceof ContextClosedEvent) {
            this.onContextClosedEvent();
        }
    }

    /**
     * type类型是不是给定的基类.
     */
    private boolean isAssignableFrom(Class<?> type, Class<?>... supportedTypes) {
        if (Objects.isNull(type)) {
            return false;
        }
        for (Class<?> supportedType : supportedTypes) {
            if (supportedType.isAssignableFrom(type)) {
                return true;
            }
        }
        return false;
    }

    private void onApplicationStartingEvent(ApplicationStartingEvent event) {
        this.loggingSystem = LoggingSystem.get(event.getSpringApplication().getClassLoader());
        this.loggingSystem.beforeInitialize();
    }

    private void onApplicationEnvironmentPreparedEvent(ApplicationEnvironmentPreparedEvent event) {
        String[] args = event.getArgs();
        // 命令行启动参数 --console.log=true
        if (!ArrayUtil.isEmpty(args)) {
            for (String arg : args) {
                if (arg.contains(console_log_option_key)) {
                    String[] kvs = arg.split("=");
                    if (!ArrayUtil.isEmpty(kvs) && kvs.length == 2) {
                        String k = kvs[0];
                        String v = kvs[1];
                        if (k.equalsIgnoreCase(console_log_option_key)) {
                            System.setProperty(k, v);
                        }
                    }
                }
            }
        }
        // true or false
        String consoleLogOption = System.getProperty(console_log_option_key);
        if (Objects.equals("true", consoleLogOption)) {
            this.loggingSystem = new LogbackLoggingSystem(this.getClass().getClassLoader());
            this.initialize(new MsbLoggingEnvironment(), event.getSpringApplication().getClassLoader());
        }
    }

    private void onApplicationPreparedEvent(ApplicationPreparedEvent event) {
        ConfigurableListableBeanFactory beanFactory = event.getApplicationContext().getBeanFactory();
        if (!beanFactory.containsBean(LOGGING_SYSTEM_BEAN_NAME)) {
            beanFactory.registerSingleton(LOGGING_SYSTEM_BEAN_NAME, this.loggingSystem);
        }
    }

    private void onContextClosedEvent() {
        if (this.loggingSystem != null) {
            this.loggingSystem.cleanUp();
        }
    }

    private void onApplicationFailedEvent() {
        if (this.loggingSystem != null) {
            this.loggingSystem.cleanUp();
        }
    }

    protected void initialize(ConfigurableEnvironment environment, ClassLoader classLoader) {
        (new LoggingSystemProperties(environment)).apply();
        LogFile logFile = LogFile.get(environment);
        if (Objects.nonNull(logFile)) {
            logFile.applyToSystemProperties();
        }
        this.initializeEarlyLoggingLevel(environment);
        this.initializeSystem(environment, this.loggingSystem, logFile);
        this.initializeFinalLoggingLevels(environment, this.loggingSystem);
        this.registerShutdownHookIfNecessary(environment, this.loggingSystem);
    }

    private void initializeEarlyLoggingLevel(ConfigurableEnvironment environment) {
        if (this.parseArgs && Objects.isNull(this.springBootLogLevel)) {
            if (this.isSet(environment, LOG_LEVEL_KEY_PREFIX + "debug")) {
                this.springBootLogLevel = LogLevel.DEBUG;
            }
            if (this.isSet(environment, LOG_LEVEL_KEY_PREFIX + "trace")) {
                this.springBootLogLevel = LogLevel.TRACE;
            }
        }
    }

    private boolean isSet(ConfigurableEnvironment environment, String property) {
        String value = environment.getProperty(property);
        return Objects.nonNull(value) && !value.equals("false");
    }

    private void initializeSystem(ConfigurableEnvironment environment, LoggingSystem system, LogFile logFile) {
        LoggingInitializationContext initializationContext = new LoggingInitializationContext(environment);
        String logConfig = EnvironmentMgr.getProperty(CONFIG_PROPERTY);
        if (this.ignoreLogConfig(logConfig)) {
            system.initialize(initializationContext, null, logFile);
        } else {
            try {
                system.cleanUp();
                ResourceUtils.getURL(logConfig).openStream().close();
                system.initialize(initializationContext, logConfig, logFile);
            } catch (Exception e) {
                System.err.println("Logging system failed to initialize using configuration from [" + logConfig + "]");
                e.printStackTrace(System.err);
                throw new IllegalStateException(e);
            }
        }

    }

    private boolean ignoreLogConfig(String logConfig) {
        return !StringUtils.hasLength(logConfig) || logConfig.startsWith("-D");
    }

    private void initializeFinalLoggingLevels(ConfigurableEnvironment environment, LoggingSystem system) {
        if (Objects.nonNull(this.springBootLogLevel)) {
            this.initializeLogLevel(system, this.springBootLogLevel);
        }
        this.setLogLevels(system, environment);
    }

    protected void initializeLogLevel(LoggingSystem system, LogLevel level) {
        List<String> loggers = LOG_LEVEL_LOGGERS.get(level);
        for (String logger : loggers) {
            system.setLogLevel(logger, level);
        }
    }

    protected void setLogLevels(LoggingSystem system, Environment environment) {
        Set<String> allProperties = EnvironmentMgr.getAllKeys();
        // like, DEBUG=true, INFO=false
        Map<String, String> levels = new HashMap<>();
        allProperties.forEach((key) -> {
            if (key.startsWith(LOG_LEVEL_KEY_PREFIX)) {
                levels.put(key.substring(LOG_LEVEL_KEY_PREFIX.length()), EnvironmentMgr.getProperty(key));
            }
        });
        levels.forEach((k, v) -> {
            this.setLogLevel(system, environment, k, v);
        });
    }

    private void setLogLevel(LoggingSystem system, Environment environment, String name, String level) {
        try {
            level = environment.resolvePlaceholders(level);
            system.setLogLevel(name, this.coerceLogLevel(level));
        } catch (RuntimeException e) {
            this.logger.error("Cannot set level: " + level + " for [" + name + "]");
        }
    }

    private LogLevel coerceLogLevel(String level) {
        return "false".equalsIgnoreCase(level) ? LogLevel.OFF : LogLevel.valueOf(level.toUpperCase(Locale.ENGLISH));
    }

    private void registerShutdownHookIfNecessary(Environment environment, LoggingSystem loggingSystem) {
        Boolean registerShutdownHook = (Boolean) environment.getProperty(REGISTER_SHUTDOWN_HOOK_PROPERTY, Boolean.class, false);
        if (registerShutdownHook) {
            Runnable shutdownHandler = loggingSystem.getShutdownHandler();
            if (shutdownHandler != null && shutdownHookRegistered.compareAndSet(false, true)) {
                this.registerShutdownHook(new Thread(shutdownHandler));
            }
        }
    }

    void registerShutdownHook(Thread shutdownHook) {
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }
}
