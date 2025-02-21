package com.github.bannirui.msb.http.filter;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

public class HttpReqAndRsp {
    private HttpUriRequest httpRequest;
    private HttpResponse httpResponse;

    public HttpReqAndRsp(HttpUriRequest httpRequest, HttpResponse httpResponse) {
        this.setHttpRequest(httpRequest);
        this.setHttpResponse(httpResponse);
    }

    public HttpUriRequest getHttpRequest() {
        return this.httpRequest;
    }

    public void setHttpRequest(HttpUriRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

    public HttpResponse getHttpResponse() {
        return this.httpResponse;
    }

    public void setHttpResponse(HttpResponse httpResponse) {
        this.httpResponse = httpResponse;
    }
}
