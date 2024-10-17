package com.github.bannirui.msb.common.ex;

public class BaseException extends RuntimeException{

    private String code;
    private String msg;

    protected BaseException() {
    }

    protected BaseException(String code, String msg) {
        super(code + "=" + msg);
        this.code = code;
        this.msg = msg;
    }

    protected BaseException(String code, String msg, Throwable e) {
        super(msg, e);
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
