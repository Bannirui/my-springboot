package com.github.bannirui.msb.config.processor;

public interface FlowControlProcessor {
    boolean switchOff(int weight, Object... args);
}
