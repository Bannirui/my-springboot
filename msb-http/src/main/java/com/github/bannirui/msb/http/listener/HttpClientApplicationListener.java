package com.github.bannirui.msb.http.listener;

import com.github.bannirui.msb.http.config.HttpConfigProperties;
import com.github.bannirui.msb.http.config.HttpConfigPropertiesProvider;
import com.github.bannirui.msb.properties.bind.PropertyBinder;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;

public class HttpClientApplicationListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        HttpConfigPropertiesProvider.setHttpConfigProperties(new PropertyBinder(event.getEnvironment()).bind("http", HttpConfigProperties.class).orElseGet(HttpConfigProperties::new));
    }
}
