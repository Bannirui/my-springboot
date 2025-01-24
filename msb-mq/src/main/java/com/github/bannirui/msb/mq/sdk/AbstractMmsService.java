package com.github.bannirui.msb.mq.sdk;

public abstract class AbstractMmsService implements MmsService {
    protected volatile boolean running;

    public boolean isRunning() {
        return this.running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
