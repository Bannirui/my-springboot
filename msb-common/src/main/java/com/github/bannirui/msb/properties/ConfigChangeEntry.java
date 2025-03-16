package com.github.bannirui.msb.properties;

import java.util.Map;

public class ConfigChangeEntry {
    private String configKey;
    private String newValue;
    private String oldValue;
    private ConfigChangeType configChangeType;
    private Map<String, Object> extraInfos;

    public String getConfigKey() {
        return this.configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getNewValue() {
        return this.newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getOldValue() {
        return this.oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public ConfigChangeType getConfigChangeType() {
        return this.configChangeType;
    }

    public void setConfigChangeType(ConfigChangeType configChangeType) {
        this.configChangeType = configChangeType;
    }

    public Map<String, Object> getExtraInfos() {
        return this.extraInfos;
    }

    public void setExtraInfos(Map<String, Object> extraInfos) {
        this.extraInfos = extraInfos;
    }
}
