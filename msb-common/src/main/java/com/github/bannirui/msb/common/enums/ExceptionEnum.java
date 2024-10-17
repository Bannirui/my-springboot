package com.github.bannirui.msb.common.enums;

public enum ExceptionEnum implements ErrorCode{
    INITIALIZATION_EXCEPTION("SYS_000", "{0}初始化失败，参数列表:{1}"),
    PARAM_EXCEPTION("SYS_001", "{0}参数异常，参数列表:{1}"),
    FILE_EXCEPTION("SYS_002", "{0}操作文件发生异常"),
    ;

    private final String code;
    private final String msg;

    ExceptionEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getMsg() {
        return this.msg;
    }
}
