package com.github.bannirui.msb.orm.squence;

public class AbstractLifecycle implements Lifecycle {
    protected final Object lock = new Object();
    protected volatile boolean isInited = false;

    @Override
    public void init() {
        synchronized(this.lock) {
            if (!this.isInited()) {
                try {
                    this.doInit();
                    this.isInited = true;
                } catch (Exception e) {
                    try {
                        this.doDestroy();
                    } catch (Exception ex) {
                    }
                    throw new RuntimeException(e);
                }
            }

        }
    }

    @Override
    public void destroy() {
        synchronized(this.lock) {
            if (this.isInited()) {
                this.doDestroy();
                this.isInited = false;
            }
        }
    }

    @Override
    public boolean isInited() {
        return this.isInited;
    }

    protected void doInit() {
    }

    protected void doDestroy() {
    }
}
