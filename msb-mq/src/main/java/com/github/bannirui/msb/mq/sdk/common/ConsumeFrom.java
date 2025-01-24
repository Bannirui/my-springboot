package com.github.bannirui.msb.mq.sdk.common;

public enum ConsumeFrom {
    EARLIEST("earliest"),
    LATEST("latest"),
    NONE("none");

    private String name;

    private ConsumeFrom(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
