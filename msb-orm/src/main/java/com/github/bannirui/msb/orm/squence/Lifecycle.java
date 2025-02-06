package com.github.bannirui.msb.orm.squence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Lifecycle {
    private Logger log = LoggerFactory.getLogger(Lifecycle.class);

    void init();

    void destroy();

    boolean isInited();
}
