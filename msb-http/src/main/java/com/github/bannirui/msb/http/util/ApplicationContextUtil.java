package com.github.bannirui.msb.http.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ApplicationContextUtil implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext app) {
        applicationContext = app;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
