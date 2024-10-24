package com.github.bannirui.msb.common.enums;

import java.util.HashMap;
import java.util.Map;

public enum MsbEnv {

    DEV("dev"),
    FAT("fat"),
    UAT("uat"),
    PROD("pro"),
    ;

    private final String msg;

    MsbEnv(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    private static final Map<String, MsbEnv> MAP = new HashMap<>();

    static {
        for (MsbEnv e : MsbEnv.values()) {
            MAP.put(e.getMsg(), e);
        }
    }

    public static MsbEnv of(String msg) {
        return MAP.get(msg);
    }
}
