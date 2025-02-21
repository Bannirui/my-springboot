package com.github.bannirui.msb.http.ex;

import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

public class SoaHttpException extends RuntimeException {
    public static final Charset UTF_8 = Charset.forName("UTF-8");
    private static final String EXCEPTION_MESSAGE_TEMPLATE_NULL_HttpRequest = "HttpRequest should not be null";
    private static final long serialVersionUID = 0L;
    private int status;
    private HttpResponse httpResponse;
    private HttpRequest HttpRequest;

    public static <T> T checkNotNull(T reference, String errorMessageTemplate, Object... errorMessageArgs) {
        if (reference == null) {
            throw new NullPointerException(String.format(errorMessageTemplate, errorMessageArgs));
        } else {
            return reference;
        }
    }

    protected SoaHttpException(int status, String message, HttpRequest HttpRequest, Throwable cause, HttpResponse httpResponse) {
        super(message, cause);
        this.status = status;
        this.httpResponse = httpResponse;
        this.HttpRequest = this.checkHttpRequestNotNull(HttpRequest);
    }

    protected SoaHttpException(int status, String message, HttpRequest HttpRequest, HttpResponse httpResponse) {
        super(message);
        this.status = status;
        this.httpResponse = httpResponse;
        this.HttpRequest = this.checkHttpRequestNotNull(HttpRequest);
    }

    private HttpRequest checkHttpRequestNotNull(HttpRequest HttpRequest) {
        return checkNotNull(HttpRequest, "HttpRequest should not be null");
    }

    public int status() {
        return this.status;
    }

    public HttpResponse httpResponse() {
        return this.httpResponse;
    }

    public HttpRequest HttpRequest() {
        return this.HttpRequest;
    }

    public boolean hasHttpRequest() {
        return this.HttpRequest != null;
    }

    static SoaHttpException errorReading(HttpRequest HttpRequest, HttpResponse response, IOException cause) {
        return new SoaHttpException(response.getStatusLine().getStatusCode(), String.format("%s reading %s %s", cause.getMessage(), HttpRequest.getRequestLine().getMethod(), HttpRequest.getRequestLine().getUri()), HttpRequest, cause, response);
    }

    public static SoaHttpException errorStatus(String methodKey, HttpRequest request, HttpResponse response) {
        String message = String.format("status %s reading %s", response.getStatusLine().getStatusCode(), methodKey);
        return errorStatus(response.getStatusLine().getStatusCode(), message, request, response);
    }

    private static SoaHttpException errorStatus(int status, String message, HttpRequest HttpRequest, HttpResponse response) {
        return new SoaHttpException(status, message, HttpRequest, response);
    }
}
