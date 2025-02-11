package com.github.bannirui.msb.dubbo.config;

import java.util.List;

public class ConsumerAuthProperties {
    private List<String[]> dubboAuthPackage;
    private List<String> dubboAuthKey;
    private List<String> configPackageKey;
    private List<String> configAuthKey;

    public List<String[]> getDubboAuthPackage() {
        return this.dubboAuthPackage;
    }

    public void setDubboAuthPackage(List<String[]> dubboAuthPackage) {
        this.dubboAuthPackage = dubboAuthPackage;
    }

    public List<String> getDubboAuthKey() {
        return this.dubboAuthKey;
    }

    public void setDubboAuthKey(List<String> dubboAuthKey) {
        this.dubboAuthKey = dubboAuthKey;
    }

    public List<String> getConfigPackageKey() {
        return this.configPackageKey;
    }

    public void setConfigPackageKey(List<String> configPackageKey) {
        this.configPackageKey = configPackageKey;
    }

    public List<String> getConfigAuthKey() {
        return this.configAuthKey;
    }

    public void setConfigAuthKey(List<String> configAuthKey) {
        this.configAuthKey = configAuthKey;
    }
}
