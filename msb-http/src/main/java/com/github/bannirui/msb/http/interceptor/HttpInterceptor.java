package com.github.bannirui.msb.http.interceptor;

import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.HttpRequestRetryHandler;

public interface HttpInterceptor extends HttpRequestInterceptor, HttpResponseInterceptor, HttpRequestRetryHandler {
}
