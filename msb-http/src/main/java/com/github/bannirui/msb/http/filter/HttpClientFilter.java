package com.github.bannirui.msb.http.filter;

public interface HttpClientFilter {
    void doFilter(HttpReqAndRsp httpReqAndRsp, HttpClientFilterChain httpFilterChain);
}
