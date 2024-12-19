package com.github.bannirui.msb.log.configuration;

/**
 * 日志文件策略{@link com.github.bannirui.msb.log.appender.FileAppender}
 * 异步支持的参数设置.
 */
public class AsyncFileAppenderCfg {

    /**
     * VM参数中指定-Dmsb.log.asyncFileAppender.enable=true进行特性开启.
     */
    public static boolean isEnable() {
        return Boolean.parseBoolean(System.getProperty("msb.log.asyncFileAppender.enable", "false"));
    }

    public static int getQueueSize() {
        return Integer.parseInt(System.getProperty("msb.log.asyncFileAppender.queueSize", "256"));
    }

    public static boolean bNeverBlock() {
        return Boolean.parseBoolean(System.getProperty("msb.log.asyncFileAppender.neverBlock", "true"));
    }

    public static int getDiscardingThreshold() {
        return Integer.parseInt(System.getProperty("msb.log.asyncFileAppender.discardingThreshold", "-1"));
    }
}
