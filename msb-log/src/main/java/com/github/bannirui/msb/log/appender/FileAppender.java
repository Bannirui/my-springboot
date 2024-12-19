package com.github.bannirui.msb.log.appender;

import ch.qos.logback.core.rolling.RollingFileAppender;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志的文件策略.
 */
public class FileAppender<E> extends RollingFileAppender<E> {

    private static Logger logger = LoggerFactory.getLogger(FileAppender.class);
    private volatile ScheduledExecutorService schedule;
    // 是否启用日志文件策略
    private static volatile boolean option = true;

    @Override
    public void setImmediateFlush(boolean immediateFlush) {
        super.setImmediateFlush(immediateFlush);
    }

    @Override
    public void start() {
        if(!option) {
            return;
        }
        super.start();
        this.setImmediateFlush(true);
        if (this.schedule == null) {
            synchronized (this) {
                this.schedule = new ScheduledThreadPoolExecutor(1, (new ThreadFactoryBuilder()).setNameFormat("asyncFlush-pool-%d").setDaemon(true).build());
                this.schedule.scheduleWithFixedDelay(() -> {
                    try {
                        if (super.getOutputStream() != null) {
                            super.getOutputStream().flush();
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }, 1_000L, 1_000L, TimeUnit.MILLISECONDS);
            }
        }
    }

    @Override
    public void stop() {
        if (this.schedule != null) {
            this.schedule.shutdown();
        }
        super.stop();
    }
}
