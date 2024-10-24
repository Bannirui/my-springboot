package com.github.bannirui.msb.common.properties;

import java.util.Map;
import java.util.Set;

public class ConfigChange {
    private Set<String> changedConfigKeys;
    private Map<String, ConfigChangeEntry> changedConfigs;
    private Map<String, Object> extraInfos;

    public ConfigChange() {
    }

    public Set<String> getChangedConfigKeys() {
        return this.changedConfigKeys;
    }

    public void setChangedConfigKeys(Set<String> changedConfigKeys) {
        this.changedConfigKeys = changedConfigKeys;
    }

    public Map<String, ConfigChangeEntry> getChangedConfigs() {
        return this.changedConfigs;
    }

    public void setChangedConfigs(Map<String, ConfigChangeEntry> changedConfigs) {
        this.changedConfigs = changedConfigs;
    }

    public Map<String, Object> getExtraInfos() {
        return this.extraInfos;
    }

    public void setExtraInfos(Map<String, Object> extraInfos) {
        this.extraInfos = extraInfos;
    }
}
