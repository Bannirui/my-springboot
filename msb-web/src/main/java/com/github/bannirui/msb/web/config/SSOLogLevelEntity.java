package com.github.bannirui.msb.web.config;

import com.github.bannirui.msb.annotation.ConfigEntity;

@ConfigEntity(
    prefix = "sso"
)
public class SSOLogLevelEntity {
    private String logLevel;

    public String getLogLevel() {
        return this.logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }
}
