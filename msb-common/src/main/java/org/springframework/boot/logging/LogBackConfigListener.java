package org.springframework.boot.logging;

import com.github.bannirui.msb.common.env.EnvironmentMgr;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
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

    public static final int DEFAULT_ORDER = -2147483628;
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
    private static Class[] EVENT_TYPES;
    private static Class[] SOURCE_TYPES;
    private final Log logger = LogFactory.getLog(this.getClass());
    private LoggingSystem loggingSystem;
    private int order = -2147483628;
    private boolean parseArgs = true;
    private LogLevel springBootLogging = null;
    public static final String LOG_CONFIG_LEVEL_KEY_PREFIX = "logging.level.";

    static {
        LOG_LEVEL_LOGGERS.add(LogLevel.INFO, "org.springframework.boot");
        LOG_LEVEL_LOGGERS.add(LogLevel.INFO, "org.springframework");
        LOG_LEVEL_LOGGERS.add(LogLevel.INFO, "org.apache.tomcat");
        LOG_LEVEL_LOGGERS.add(LogLevel.INFO, "org.apache.catalina");
        LOG_LEVEL_LOGGERS.add(LogLevel.INFO, "org.eclipse.jetty");
        LOG_LEVEL_LOGGERS.add(LogLevel.INFO, "org.hibernate.tool.hbm2ddl");
        LOG_LEVEL_LOGGERS.add(LogLevel.INFO, "org.hibernate.SQL");
        EVENT_TYPES = new Class[] {ApplicationStartingEvent.class, ApplicationEnvironmentPreparedEvent.class, ApplicationPreparedEvent.class,
            ContextClosedEvent.class, ApplicationFailedEvent.class, ApplicationStartedEvent.class};
        SOURCE_TYPES = new Class[] {SpringApplication.class, ApplicationContext.class};
    }

    public LogBackConfigListener() {
    }

    @Override
    public boolean supportsEventType(ResolvableType eventType) {
        return this.isAssignableFrom(eventType.getRawClass(), EVENT_TYPES);
    }

    @Override
    public boolean supportsSourceType(Class sourceType) {
        return this.isAssignableFrom(sourceType, SOURCE_TYPES);
    }

    private boolean isAssignableFrom(Class type, Class... supportedTypes) {
        if (type != null) {
            Class[] var3 = supportedTypes;
            int var4 = supportedTypes.length;

            for (int var5 = 0; var5 < var4; ++var5) {
                Class supportedType = var3[var5];
                if (supportedType.isAssignableFrom(type)) {
                    return true;
                }
            }
        }

        return false;
    }

    private void onApplicationStartingEvent(ApplicationStartingEvent event) {
        this.loggingSystem = LoggingSystem.get(event.getSpringApplication().getClassLoader());
        this.loggingSystem.beforeInitialize();
    }

    private void onApplicationEnvironmentPreparedEvent(ApplicationEnvironmentPreparedEvent event) {
        Arrays.stream(event.getArgs()).filter((arg) -> {
            return arg.split("=")[0].equalsIgnoreCase("console.log");
        }).findFirst().map((arg) -> {
            String[] keyValue = arg.split("=");
            return keyValue.length == 2 ? keyValue[1] : null;
        }).ifPresent((value) -> {
            System.setProperty("console.log", value);
        });
        this.loggingSystem = new LogbackLoggingSystem(this.getClass().getClassLoader());
        this.initialize(new MsbLoggingEnvironment(), event.getSpringApplication().getClassLoader());
    }

    private void onApplicationPreparedEvent(ApplicationPreparedEvent event) {
        ConfigurableListableBeanFactory beanFactory = event.getApplicationContext().getBeanFactory();
        if (!beanFactory.containsBean("springBootLoggingSystem")) {
            beanFactory.registerSingleton("springBootLoggingSystem", this.loggingSystem);
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
        if (logFile != null) {
            logFile.applyToSystemProperties();
        }

        this.initializeEarlyLoggingLevel(environment);
        this.initializeSystem(environment, this.loggingSystem, logFile);
        this.initializeFinalLoggingLevels(environment, this.loggingSystem);
        this.registerShutdownHookIfNecessary(environment, this.loggingSystem);
    }

    private void initializeEarlyLoggingLevel(ConfigurableEnvironment environment) {
        if (this.parseArgs && this.springBootLogging == null) {
            if (this.isSet(environment, "debug")) {
                this.springBootLogging = LogLevel.DEBUG;
            }

            if (this.isSet(environment, "trace")) {
                this.springBootLogging = LogLevel.TRACE;
            }
        }

    }

    private boolean isSet(ConfigurableEnvironment environment, String property) {
        String value = environment.getProperty(property);
        return value != null && !value.equals("false");
    }

    private void initializeSystem(ConfigurableEnvironment environment, LoggingSystem system, LogFile logFile) {
        LoggingInitializationContext initializationContext = new LoggingInitializationContext(environment);
        String logConfig = EnvironmentMgr.getProperty("logging.config");
        if (this.ignoreLogConfig(logConfig)) {
            system.initialize(initializationContext, (String) null, logFile);
        } else {
            try {
                system.cleanUp();
                ResourceUtils.getURL(logConfig).openStream().close();
                system.initialize(initializationContext, logConfig, logFile);
            } catch (Exception e) {
                System.err.println("Logging system failed to initialize using configuration from '" + logConfig + "'");
                e.printStackTrace(System.err);
                throw new IllegalStateException(e);
            }
        }

    }

    private boolean ignoreLogConfig(String logConfig) {
        return !StringUtils.hasLength(logConfig) || logConfig.startsWith("-D");
    }

    private void initializeFinalLoggingLevels(ConfigurableEnvironment environment, LoggingSystem system) {
        if (this.springBootLogging != null) {
            this.initializeLogLevel(system, this.springBootLogging);
        }

        this.setLogLevels(system, environment);
    }

    protected void initializeLogLevel(LoggingSystem system, LogLevel level) {
        List<String> loggers = (List) LOG_LEVEL_LOGGERS.get(level);
        if (loggers != null) {
            Iterator var4 = loggers.iterator();

            while (var4.hasNext()) {
                String logger = (String) var4.next();
                system.setLogLevel(logger, level);
            }
        }

    }

    protected void setLogLevels(LoggingSystem system, Environment environment) {
        Set<String> allProperties = EnvironmentMgr.getAllKeys();
        Map<String, String> levels = new HashMap();
        allProperties.forEach((keyx) -> {
            if (keyx.startsWith("logging.level.")) {
                levels.put(keyx.substring("logging.level.".length()), EnvironmentMgr.getProperty(keyx));
            }

        });
        Iterator var5 = levels.keySet().iterator();

        while (var5.hasNext()) {
            String key = (String) var5.next();
            this.setLogLevel(system, environment, key, (String) levels.get(key));
        }

    }

    private void setLogLevel(LoggingSystem system, Environment environment, String name, String level) {
        try {
            level = environment.resolvePlaceholders(level);
            system.setLogLevel(name, this.coerceLogLevel(level));
        } catch (RuntimeException var6) {
            this.logger.error("Cannot set level: " + level + " for '" + name + "'");
        }

    }

    private LogLevel coerceLogLevel(String level) {
        return "false".equalsIgnoreCase(level) ? LogLevel.OFF : LogLevel.valueOf(level.toUpperCase(Locale.ENGLISH));
    }

    private void registerShutdownHookIfNecessary(Environment environment, LoggingSystem loggingSystem) {
        Boolean registerShutdownHook = (Boolean) environment.getProperty("logging.register-shutdown-hook", Boolean.class, false);
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

    public void setOrder(int order) {
        this.order = order;
    }

    public int getOrder() {
        return this.order;
    }

    public void setSpringBootLogging(LogLevel springBootLogging) {
        this.springBootLogging = springBootLogging;
    }

    public void setParseArgs(boolean parseArgs) {
        this.parseArgs = parseArgs;
    }

    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof ApplicationEnvironmentPreparedEvent) {
            this.onApplicationEnvironmentPreparedEvent((ApplicationEnvironmentPreparedEvent) applicationEvent);
        }

        if (applicationEvent instanceof ApplicationStartedEvent) {
            ConfigurableEnvironment environment = ((ApplicationStartedEvent) applicationEvent).getApplicationContext().getEnvironment();
            LoggingSystem loggingSystem =
                (LoggingSystem) ((ApplicationStartedEvent) applicationEvent).getApplicationContext().getBean(LoggingSystem.class);
            if (environment.getPropertySources().contains("ApolloPropertySources")) {
                String[] apolloPropertySources =
                    ((EnumerablePropertySource) environment.getPropertySources().get("ApolloPropertySources")).getPropertyNames();
                Map<String, String> levels = new HashMap<>();
                Optional.ofNullable(apolloPropertySources).ifPresent((o) -> {
                    Arrays.asList(o).forEach((keyx) -> {
                        if (keyx.startsWith("logging.level.")) {
                            levels.put(keyx.substring("logging.level.".length()), environment.getProperty(keyx));
                        }

                    });
                    Iterator var5 = levels.keySet().iterator();

                    while (var5.hasNext()) {
                        String key = (String) var5.next();
                        this.setLogLevel(loggingSystem, environment, key, (String) levels.get(key));
                    }

                });
            }
        }

    }
}
