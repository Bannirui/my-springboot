package com.github.bannirui.msb.dubbo.config;

import com.alibaba.fastjson.JSON;
import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.github.bannirui.msb.event.DynamicConfigChangeSpringEvent;
import com.github.bannirui.msb.properties.ConfigChange;
import com.github.bannirui.msb.properties.ConfigChangeEntry;
import com.github.bannirui.msb.properties.ConfigChangeType;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

public class AuthConfigChangeEventListener implements ApplicationListener<DynamicConfigChangeSpringEvent>, EnvironmentAware {
    private static final Logger logger = LoggerFactory.getLogger(AuthConfigChangeEventListener.class);
    private static final String DUBBO_AUTH_PREFIX = "dubbo.auth.";
    private static ProviderAuthProperties providerAuthProperties;
    private static ConsumerAuthProperties consumerAuthProperties;
    private ConfigurableEnvironment env;
    private static Map<String, AuthConfig> authMap = new ConcurrentHashMap<>();
    private static Pair<String, AuthConfig> globalAuth;

    public AuthConfigChangeEventListener() {
        Config config = ConfigService.getConfig("_msb.System.Config");
        Set<String> allKeys = config.getPropertyNames();
        allKeys.forEach((key) -> {
            if (key.startsWith("dubbo.auth.")) {
                String rule = config.getProperty(key, "");
                try {
                    AuthConfig authConfig = JSON.parseObject(rule, AuthConfig.class);
                    if (authConfig.getDubboAuthPackage().length == 1 && "*".equals(authConfig.getDubboAuthPackage()[0])) {
                        if (globalAuth != null) {
                            logger.warn("全局匹配规则存在多个，可能会引起异常, 规则标识 {}", key);
                        }
                        globalAuth = new ImmutablePair<>(key, authConfig);
                    } else {
                        authMap.put(key, authConfig);
                    }
                } catch (Exception e) {
                    logger.error("转换鉴权规则失败 {} - {}", key, rule);
                }
            }
        });
    }

    public static Map<String, AuthConfig> getAuthMap() {
        return authMap;
    }

    public static Pair<String, AuthConfig> getGlobalAuth() {
        return globalAuth;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.env = (ConfigurableEnvironment)environment;
    }

    public static ConsumerAuthProperties getConsumerDubboAuthProperties() {
        return consumerAuthProperties;
    }

    public static ProviderAuthProperties getProviderDubboAuthProperties() {
        return providerAuthProperties;
    }

    @Override
    public void onApplicationEvent(DynamicConfigChangeSpringEvent event) {
        ConfigChange configChange = event.getConfigChange();
        Set<String> changedKeys = configChange.getChangedConfigKeys();
        Map<String, ConfigChangeEntry> changedConfigs = configChange.getChangedConfigs();
        for (String key : changedKeys) {
            if(!StringUtils.startsWith(key, "dubbo.auth.")) continue;
            ConfigChangeEntry change = changedConfigs.get(key);
            try {
                AuthConfig newAuthConfig;
                if (ConfigChangeType.ADD.equals(change.getConfigChangeType())) {
                    newAuthConfig = JSON.parseObject(change.getNewValue(), AuthConfig.class);
                    if (newAuthConfig.getDubboAuthPackage().length == 1 && "*".equals(newAuthConfig.getDubboAuthPackage()[0])) {
                        if (globalAuth != null) {
                            logger.warn("全局匹配规则存在多个，可能会引起异常, 规则标识 {}", key);
                        } else {
                            globalAuth = new ImmutablePair<>(key, newAuthConfig);
                        }
                    } else {
                        authMap.put(key, newAuthConfig);
                    }
                } else if (!ConfigChangeType.UPDATE.equals(change.getConfigChangeType())) {
                    if (ConfigChangeType.DELETE.equals(change.getConfigChangeType())) {
                        newAuthConfig = JSON.parseObject(change.getOldValue(), AuthConfig.class);
                        if (newAuthConfig.getDubboAuthPackage().length == 1 && "*".equals(newAuthConfig.getDubboAuthPackage()[0])) {
                            globalAuth = null;
                        } else {
                            authMap.remove(key);
                        }
                    }
                } else {
                    newAuthConfig = JSON.parseObject(change.getNewValue(), AuthConfig.class);
                    if (newAuthConfig.getDubboAuthPackage().length == 1 && "*".equals(newAuthConfig.getDubboAuthPackage()[0])) {
                        if (globalAuth != null && !key.equals(globalAuth.getLeft())) {
                            logger.warn("全局匹配规则存在多个，可能会引起异常, 规则标识 {}", key);
                        } else {
                            globalAuth = new ImmutablePair<>(key, newAuthConfig);
                        }
                        authMap.remove(key);
                    } else {
                        authMap.put(key, newAuthConfig);
                    }
                }
            } catch (Exception e) {
                logger.error("规则变更失败", e);
            }
        }
    }
}
