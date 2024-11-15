package com.github.bannirui.msb.log.configuration;

public class AsyncAppenderProperty {
    public AsyncAppenderProperty() {
    }

    public static boolean isEnable() {
        return Boolean.valueOf(System.getProperty("msb.log.asyncAppender.enable", "false"));
    }

    public static int getQueueSize() {
        return Integer.valueOf(System.getProperty("msb.log.asyncAppender.queueSize", "256"));
    }

    public static boolean bNeverBlock() {
        return Boolean.valueOf(System.getProperty("msb.log.asyncAppender.neverBlock", "true"));
    }

    public static int getDiscardingThreshold() {
        return Integer.valueOf(System.getProperty("msb.log.asyncAppender.discardingThreshold", "-1"));
    }
}
