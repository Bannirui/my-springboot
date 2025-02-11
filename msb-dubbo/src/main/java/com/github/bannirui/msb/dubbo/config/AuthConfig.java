package com.github.bannirui.msb.dubbo.config;

import java.util.HashMap;
import java.util.Map;

public class AuthConfig {
    private String[] dubboAuthPackage = new String[0];
    private Map<String, String> appSecret = new HashMap<>();

    public String[] getDubboAuthPackage() {
        return this.dubboAuthPackage;
    }

    public void setDubboAuthPackage(String[] dubboAuthPackage) {
        this.dubboAuthPackage = dubboAuthPackage;
    }

    public Map<String, String> getAppSecret() {
        return this.appSecret;
    }

    public void setAppSecret(Map<String, String> appSecret) {
        this.appSecret = appSecret;
    }
}
