package com.github.bannirui.msb.orm.squence;

public class AbstractLifecycle implements Lifecycle {
    protected final Object lock = new Object();
    protected volatile boolean isInited = false;

    public AbstractLifecycle() {
    }

    public void init() {
        synchronized(this.lock) {
            if (!this.isInited()) {
                try {
                    this.doInit();
                    this.isInited = true;
                } catch (Exception var6) {
                    try {
                        this.doDestroy();
                    } catch (Exception var5) {
                    }

                    throw new RuntimeException(var6);
                }
            }

        }
    }

    public void destroy() {
        synchronized(this.lock) {
            if (this.isInited()) {
                this.doDestroy();
                this.isInited = false;
            }

        }
    }

    public boolean isInited() {
        return this.isInited;
    }

    protected void doInit() {
    }

    protected void doDestroy() {
    }
}
