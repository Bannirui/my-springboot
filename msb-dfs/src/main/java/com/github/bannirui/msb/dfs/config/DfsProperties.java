package com.github.bannirui.msb.dfs.config;

public class DfsProperties {
    public static final String PREFIX = "titans.dfs";
    private String url;
    private String extranetUrl;
    private String secret;
    private String appId;

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getExtranetUrl() {
        return this.extranetUrl;
    }

    public void setExtranetUrl(String extranetUrl) {
        this.extranetUrl = extranetUrl;
    }

    public String getSecret() {
        return this.secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getAppId() {
        return this.appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    @Override
    public String toString() {
        return "DfsProperties{url='" + this.url + '\'' + ", secret='" + this.secret + '\'' + ", appId='" + this.appId + '\'' + '}';
    }
}
