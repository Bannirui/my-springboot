package com.github.bannirui.msb.log.cat.http;

import com.dianping.cat.message.Transaction;

public class CatConstants {
    static ThreadLocal<Transaction> threadLocal = new ThreadLocal<>();
    static final String HTTP_CALL = "HttpCall";
    static final String HTTP_SERVICE = "HttpService";
    static final String HTTP_URL = "HTTP.url";
    static final String HTTP_REQUEST_INFO = "HTTP.Request.Info";
    static final String HTTP_TARGET_HOST = "HTTP.Target.Host";
    static final String HTTP_REQUEST_URI = "HTTP.Request.uri";
    static final String HTTP_REQUEST_METHOD = "HTTP.Request.Method";
    static final String HTTP_REQUEST_PROTOCOL_NAME = "HTTP.Request.ProtocolName";
}
