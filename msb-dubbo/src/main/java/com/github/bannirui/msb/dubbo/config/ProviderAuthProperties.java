package com.github.bannirui.msb.dubbo.config;

import java.util.List;

public class ProviderAuthProperties {
    private String[] dubboAuthPackage = new String[0];
    private List<String> dubboAuthSignAppId;
    private List<String> dubboAuthSignKey;
    private List<String> configAppIdKey;
    private List<String> configSignKey;

    public String[] getDubboAuthPackage() {
        return this.dubboAuthPackage;
    }

    public void setDubboAuthPackage(String[] dubboAuthPackage) {
        for(int i = 0; i < dubboAuthPackage.length; ++i) {
            if (!dubboAuthPackage[i].endsWith(".")) {
                dubboAuthPackage[i] = dubboAuthPackage[i].trim() + ".";
            }
        }

        this.dubboAuthPackage = dubboAuthPackage;
    }

    public List<String> getDubboAuthSignAppId() {
        return this.dubboAuthSignAppId;
    }

    public void setDubboAuthSignAppId(List<String> dubboAuthSignAppId) {
        this.dubboAuthSignAppId = dubboAuthSignAppId;
    }

    public List<String> getDubboAuthSignKey() {
        return this.dubboAuthSignKey;
    }

    public void setDubboAuthSignKey(List<String> dubboAuthSignKey) {
        this.dubboAuthSignKey = dubboAuthSignKey;
    }

    public List<String> getConfigAppIdKey() {
        return this.configAppIdKey;
    }

    public void setConfigAppIdKey(List<String> configAppIdKey) {
        this.configAppIdKey = configAppIdKey;
    }

    public List<String> getConfigSignKey() {
        return this.configSignKey;
    }

    public void setConfigSignKey(List<String> configSignKey) {
        this.configSignKey = configSignKey;
    }
}
