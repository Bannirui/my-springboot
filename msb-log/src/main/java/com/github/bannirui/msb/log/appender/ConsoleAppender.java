package com.github.bannirui.msb.log.appender;

/**
 * 日志的控制台策略.
 * 异步设置参数{@link com.github.bannirui.msb.log.configuration.AsyncFileAppenderCfg}
 */
public class ConsoleAppender<E> extends ch.qos.logback.core.ConsoleAppender<E> {

    // VM中参数
    public static final String CONSOLE_LOG_PROPERTY_KEY = "console.log";
    // 是否启用日志控制台策略
    private static volatile boolean option = true;

    @Override
    public void doAppend(E eventObject) {
        if (ConsoleAppender.option)
            super.doAppend(eventObject);
    }

    /**
     * 启用日志控制台策略.
     */
    public static void enable() {
        ConsoleAppender.option = true;
    }

    /**
     * 禁用日志控制台策略.
     */
    public static void disable() {
        ConsoleAppender.option = false;
    }
}
