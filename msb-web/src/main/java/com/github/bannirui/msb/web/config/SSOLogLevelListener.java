package com.github.bannirui.msb.web.config;

import com.github.bannirui.msb.annotation.EnableMsbConfigChangeListener;
import org.springframework.context.ApplicationContext;

@EnableMsbConfigChangeListener(
    methods = {"changeSSOLogLevel"}
)
public class SSOLogLevelListener {

    public void changeSSOLogLevel(SSOLogLevelEntity logLevelEntity, ApplicationContext context) {
        SSOConfig ssoConfig = context.getBean(SSOConfig.class);
        if (ssoConfig != null) {
            ssoConfig.setSsoLogLevelString(logLevelEntity.getLogLevel());
        }
    }
}
