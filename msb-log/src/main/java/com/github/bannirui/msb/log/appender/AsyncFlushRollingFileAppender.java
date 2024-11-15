package com.github.bannirui.msb.log.appender;

import ch.qos.logback.core.rolling.RollingFileAppender;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncFlushRollingFileAppender extends RollingFileAppender {

    private static Logger logger = LoggerFactory.getLogger(AsyncFlushRollingFileAppender.class);
    private volatile ScheduledExecutorService schedule;

    public AsyncFlushRollingFileAppender() {
    }

    @Override
    public void setImmediateFlush(boolean immediateFlush) {
        super.setImmediateFlush(immediateFlush);
    }

    @Override
    public void start() {
        super.start();
        this.setImmediateFlush(true);
        if (this.schedule == null) {
            synchronized (this) {
                this.schedule =
                    new ScheduledThreadPoolExecutor(1, (new ThreadFactoryBuilder()).setNameFormat("asyncFlush-pool-%d").setDaemon(true).build());
                this.schedule.scheduleWithFixedDelay(() -> {
                    try {
                        if (super.getOutputStream() != null) {
                            super.getOutputStream().flush();
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    } finally {
                        // TODO: 2024/11/14
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
