package com.github.bannirui.msb.common.event;

import com.github.bannirui.msb.common.properties.ConfigChange;
import java.util.EventObject;

public class ConfigChangeEvent extends EventObject {

    private ConfigChange configChange;

    public ConfigChangeEvent(Object source) {
        super(source);
    }

    public ConfigChangeEvent(Object source, ConfigChange configChange) {
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
