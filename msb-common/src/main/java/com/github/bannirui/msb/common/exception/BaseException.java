package com.github.bannirui.msb.common.exception;

/**
 * 自定义异常.
 */
public class BaseException extends RuntimeException {

    private String code;
    private String msg;

    protected BaseException() {
    }

    protected BaseException(String msg) {
        super(msg);
        this.msg = msg;
    }

    protected BaseException(String code, String msg) {
        super(code + "=" + msg);
        this.code = code;
        this.msg = msg;
    }

    protected BaseException(String code, String msg, Throwable cause) {
        super(code + "=" + msg, cause);
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
