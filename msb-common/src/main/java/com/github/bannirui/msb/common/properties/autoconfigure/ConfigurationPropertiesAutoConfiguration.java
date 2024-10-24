package com.github.bannirui.msb.common.properties.autoconfigure;

import com.github.bannirui.msb.common.properties.adapter.ConfigurationAdapterCollectorListener;
import com.github.bannirui.msb.common.properties.bind.BlackListBindHandler;
import java.util.Arrays;
import java.util.HashSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindHandlerAdvisor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConditionalOnClass({EnableConfigurationProperties.class})
@Configuration
public class ConfigurationPropertiesAutoConfiguration {

    @Value("${msb.configuration.property.blacklist:''}")
    private String blacklist;

    public ConfigurationPropertiesAutoConfiguration() {
    }

    @Bean
    public ConfigurationPropertiesBindHandlerAdvisor advisor() {
        String[] propertyExcludes = this.blacklist.split(",");
        HashSet<String> excludes = new HashSet<>(Arrays.asList(propertyExcludes));
        return (handler) -> new BlackListBindHandler(handler, excludes);
    }

    @Bean
    public ConfigurationAdapterCollectorListener configurationAdapterCollectorListener() {
        return new ConfigurationAdapterCollectorListener();
    }
}
