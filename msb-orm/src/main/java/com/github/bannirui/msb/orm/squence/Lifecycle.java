package com.github.bannirui.msb.orm.squence;

public interface Lifecycle {
    void init();

    void destroy();

    boolean isInited();
}
