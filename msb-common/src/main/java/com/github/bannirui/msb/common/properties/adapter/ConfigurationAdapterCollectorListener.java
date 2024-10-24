package com.github.bannirui.msb.common.properties.adapter;

import com.github.bannirui.msb.common.env.EnvironmentMgr;
import com.github.bannirui.msb.common.util.StringUtil;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

public class ConfigurationAdapterCollectorListener implements ApplicationListener<ApplicationStartedEvent>, EnvironmentAware {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationAdapterCollectorListener.class);
    private ConfigurableEnvironment env;

    public ConfigurationAdapterCollectorListener() {
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        ConfigurationAdapterInfo adapterInfo = new ConfigurationAdapterInfo();
        adapterInfo.setAppId(Objects.isNull(EnvironmentMgr.getAppName()) ? "UNKNOWN" : EnvironmentMgr.getAppName());
        adapterInfo.setAdapterMapping(new TreeMap<>());
        this.loadAdapterMapping(adapterInfo);
        Map<String, String> adapterMapping = adapterInfo.getAdapterMapping();
        if (adapterMapping.size() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("----WARN----You are using the following outdated configuration----WARN----\n");
            sb.append("-------Expired properties will no longer be supported in the future-------\n");
            sb.append("-----------Please use the latest properties as soon as possible-----------\n");
            sb.append("-----------old property---------          ---------new property-----------\n");
            adapterMapping.forEach((oldKey, newKey) -> sb.append(String.format("%-41s", oldKey)).append(" ").append(newKey).append("\n"));
            logger.error(sb.toString());
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.env = (ConfigurableEnvironment) environment;
    }

    private void loadAdapterMapping(final ConfigurationAdapterInfo adapterInfo) {
        PropertySource<?> propertySource = this.env.getPropertySources().get(AdapterConfigMgr.ARRAY_ADAPTER_SOURCE_NAME);
        if (Objects.nonNull(propertySource)) {
            Map<String, Object> adapterSource = (Map<String, Object>) propertySource.getSource();
            adapterSource.forEach((k, v) -> {
                // placeholder parse
                String oldKey = k.substring(2, k.length() - 1);
                if (this.warnProperty(oldKey)) {
                    adapterInfo.getAdapterMapping().put(oldKey, k);
                }
            });
        }
    }

    private boolean warnProperty(String key) {
        MutablePropertySources propertySources = this.env.getPropertySources();
        for (PropertySource<?> propertySource : propertySources) {
            String sourceName = propertySource.getName();
            if (!Objects.equals(sourceName, "configurationProperties")) {
                Object property = propertySource.getProperty(key);
                if (Objects.nonNull(property)) {
                    if (StringUtil.isBlank(sourceName)) {
                        return true;
                    }
                    return !sourceName.startsWith("msbApolloConfig:") && !sourceName.startsWith("msbFileConfig:");
                }
            }
        }
        return true;
    }
}
