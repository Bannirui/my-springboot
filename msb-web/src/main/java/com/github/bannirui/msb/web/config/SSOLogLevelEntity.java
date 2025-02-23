package com.github.bannirui.msb.web.config;

@ConfigEntity(
    prefix = "sso"
)
public class SSOLogLevelEntity {
    private String logLevel;

    public SSOLogLevelEntity() {
    }

    public String getLogLevel() {
        return this.logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }
}
