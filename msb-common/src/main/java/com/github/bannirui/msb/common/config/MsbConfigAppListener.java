package com.github.bannirui.msb.common.config;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;

public class MsbConfigAppListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered {

    public MsbConfigAppListener() {
    }


    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
