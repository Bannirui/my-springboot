package com.github.bannirui.msb.web.config;

@TitanConfigChangeListener(
    methods = {"changeSSOLogLevel"}
)
public class SSOLogLevelListener {
    public SSOLogLevelListener() {
    }

    public void changeSSOLogLevel(SSOLogLevelEntity logLevelEntity, ApplicationContext context) {
        SSOConfig ssoConfig = (SSOConfig)context.getBean(SSOConfig.class);
        if (ssoConfig != null) {
            ssoConfig.setSsoLogLevelString(logLevelEntity.getLogLevel());
        }

    }
}
