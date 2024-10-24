package com.github.bannirui.msb.common.properties.adapter;

import java.util.Map;

public class ConfigurationAdapterInfo {

    private String appId;
    private Map<String, String> adapterMapping;

    public ConfigurationAdapterInfo() {
    }

    public ConfigurationAdapterInfo(String appId, Map<String, String> adapterMapping) {
        this.appId = appId;
        this.adapterMapping = adapterMapping;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public Map<String, String> getAdapterMapping() {
        return adapterMapping;
    }

    public void setAdapterMapping(Map<String, String> adapterMapping) {
        this.adapterMapping = adapterMapping;
    }
}
