package com.github.bannirui.msb.common.event;

import com.github.bannirui.msb.common.properties.ConfigChange;
import org.springframework.context.ApplicationEvent;

public class DynamicConfigChangeSpringEvent extends ApplicationEvent {

    private ConfigChange configChange;

    public DynamicConfigChangeSpringEvent(Object source) {
        super(source);
    }

    public DynamicConfigChangeSpringEvent(Object source, ConfigChange configChange) {
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
