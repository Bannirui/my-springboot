package com.github.bannirui.msb.event;

import com.github.bannirui.msb.properties.ConfigChange;
import org.springframework.context.ApplicationEvent;

public class ConfigChangeSpringEvent extends ApplicationEvent {
    private ConfigChange configChange;

    public ConfigChangeSpringEvent(Object source) {
        super(source);
    }

    public ConfigChangeSpringEvent(String source, ConfigChange configChange) {
        super(source);
        this.configChange = configChange;
    }

    public ConfigChange getConfigChange() {
        return configChange;
    }

    public void setConfigChange(ConfigChange configChange) {
        this.configChange = configChange;
    }
}
