package com.github.bannirui.msb.log.appender;

import com.github.bannirui.msb.common.constant.EnvType;
import com.github.bannirui.msb.common.env.EnvironmentMgr;

public class ConsoleAppender<E> extends ch.qos.logback.core.ConsoleAppender<E> {

    // VM中参数
    public static final String CONSOLE_LOG = "console.log";
    private static volatile String showConsoleLog = "true";

    public ConsoleAppender() {
    }

    @Override
    public void doAppend(E eventObject) {
        if ("true".equalsIgnoreCase(showConsoleLog)) {
            super.doAppend(eventObject);
        } else if ((EnvironmentMgr.getEnv().contains(EnvType.DEV) || EnvironmentMgr.getEnv().contains(EnvType.DEFAULT)) &&
            (showConsoleLog == null || showConsoleLog.trim().isEmpty())) {
            super.doAppend(eventObject);
        }
    }

    public static void showConsoleLog(String show) {
        showConsoleLog = show;
    }
}
