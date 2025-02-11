package com.github.bannirui.msb.dubbo.exception;

public class RpcAuthenticationException extends RuntimeException {
    public RpcAuthenticationException(String message) {
        super(message);
    }

    public RpcAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
