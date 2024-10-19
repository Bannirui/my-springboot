package com.github.bannirui.msb.common.startup.monitor.param;

public class StartMonitorParam {
    private String appId;
    private String serverIp;
    private long startTime;
    private String titansVersion;
    private String titansModules;

    public StartMonitorParam() {
    }

    public String getAppId() {
        return this.appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getServerIp() {
        return this.serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getTitansVersion() {
        return this.titansVersion;
    }

    public void setTitansVersion(String titansVersion) {
        this.titansVersion = titansVersion;
    }

    public String getTitansModules() {
        return this.titansModules;
    }

    public void setTitansModules(String titansModules) {
        this.titansModules = titansModules;
    }
}
