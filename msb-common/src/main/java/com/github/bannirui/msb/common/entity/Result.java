package com.github.bannirui.msb.common.entity;

import java.io.Serializable;

public class Result<T> implements Serializable {

    private boolean status = false;
    private String message;
    private T result;
    private String statusCode;

    public static <T> Result<T> error(String statusCode, String message) {
        return new Result(message, (Object) null, statusCode);
    }

    public static <T> Result<T> success(T data) {
        return new Result(true, "操作成功", data, "SYS000");
    }

    public static <T> Result<T> success() {
        return new Result(true, "操作成功", (Object) null, "SYS000");
    }

    public Result() {
    }

    public Result(String message, T result, String statusCode) {
        this.message = message;
        this.result = result;
        this.statusCode = statusCode;
    }

    public Result(boolean status, String message, T result, String statusCode) {
        this.status = status;
        this.message = message;
        this.result = result;
        this.statusCode = statusCode;
    }

    public boolean isStatus() {
        return this.status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getResult() {
        return this.result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public String getStatusCode() {
        return this.statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }
}