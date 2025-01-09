package com.github.bannirui.msb.config;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfig;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import com.netflix.config.ConfigurationManager;
import jakarta.annotation.PostConstruct;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicHystrixConfig {

    private static final Logger logger = LoggerFactory.getLogger(DynamicHystrixConfig.class);
    private static final String HYSTRIX_CONFIG_PREFIX = "hystrix.";

    @ApolloConfig
    private Config config;

    @PostConstruct
    public void syncHystrixApolloConfig() {
        Set<String> propertyNames = this.config.getPropertyNames();
        for (String prefix : propertyNames) {
            if (prefix.startsWith("hystrix.")) {
                String value = this.config.getProperty(prefix, "");
                if (StringUtils.isNotEmpty(value)) {
                    logger.info("Apollo-Hystrix配置初始化设置 key={}，value={}", prefix, value);
                    ConfigurationManager.getConfigInstance().setProperty(prefix, value);
                }
            }
        }

    }

    @ApolloConfigChangeListener({"application"})
    private void hystrixConfigOnChange(ConfigChangeEvent changeEvent) {
        for (String key : changeEvent.changedKeys()) {
            if (key.startsWith("hystrix.")) {
                ConfigChange change = changeEvent.getChange(key);
                String value = change.getNewValue();
                if (StringUtils.isNotEmpty(value)) {
                    ConfigurationManager.getConfigInstance().setProperty(key, value);
                    logger.info("Apollo-Hystrix配置同步修改配置 key={}，value={}", key, value);
                }
            }
        }
    }
}
