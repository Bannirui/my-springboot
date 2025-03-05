package com.github.bannirui.msb.ss;

import com.github.bannirui.mss.worker.MssWorker;
import com.github.bannirui.mss.worker.common.MssWorkerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.SmartLifecycle;

public class MssFactory implements SmartLifecycle, ApplicationListener<ApplicationReadyEvent> {
    private static final Logger logger = LoggerFactory.getLogger(MssFactory.class);
    private ApplicationContext applicationContext;
    private MssWorkerConfig workerConfig;
    private boolean isRunning = false;
    private String packageName;
    private MssWorker worker;

    public MssFactory() {
    }

    public MssFactory(String packageName) {
        this.packageName = packageName;
    }

    /**
     * 初始化的时候被回调 找到mss的bean
     */
    public void init() throws Exception {
        this.worker = new MssWorker(this.workerConfig);
        // TODO: 2025/3/6
    }

    @Override
    public void start() {
        this.isRunning = true;
    }

    @Override
    public void stop() {
        this.isRunning = false;
    }

    @Override
    public void stop(Runnable callback) {
        this.stop();
        callback.run();
    }

    @Override
    public boolean isRunning() {
        return this.isRunning;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public int getPhase() {
        return 2147483647;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            this.worker.init();
        } catch (Exception e) {
            MssFactory.logger.error("mss worker启动失败", e);
            this.stop();
        }
    }

    public ApplicationContext getApplicationContext() {
        return this.applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public MssWorkerConfig getWorkerConfig() {
        return this.workerConfig;
    }

    public void setWorkerConfig(MssWorkerConfig workerConfig) {
        this.workerConfig = workerConfig;
    }
}
