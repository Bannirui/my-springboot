package com.github.bannirui.msb.http.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpClientFilterChain {
    private final Map<String, Object> attachments = new HashMap<>();
    private int pos = 0;
    private List<HttpClientFilter> httpFilters = new ArrayList<>();

    public static HttpClientFilterChain build(List<HttpClientFilter> httpFilters) {
        return new HttpClientFilterChain(httpFilters);
    }

    public HttpClientFilterChain(List<HttpClientFilter> httpFilters) {
        if (httpFilters != null) {
            this.httpFilters.addAll(httpFilters);
        }
    }

    public Object getAttachment(String key) {
        return this.attachments.get(key);
    }

    public HttpClientFilterChain setAttachment(String key, Object value) {
        if (value == null) {
            this.removeAttachment(key);
        } else {
            this.attachments.put(key, value);
        }
        return this;
    }

    public HttpClientFilterChain removeAttachment(String key) {
        this.attachments.remove(key);
        return this;
    }

    public Map<String, Object> getAttachments() {
        return this.attachments;
    }

    public void clearAttachments() {
        this.getAttachments().clear();
    }

    private List<HttpClientFilter> getFilters() {
        return this.httpFilters;
    }

    public void doFilter(HttpReqAndRsp httpReqAndRsp) {
        HttpClientFilter httpFilter = this.nextFilter();
        if (httpFilter != null) {
            httpFilter.doFilter(httpReqAndRsp, this);
        }
    }

    private HttpClientFilter nextFilter() {
        if (this.pos < this.getFilters().size()) {
            return this.getFilters().get(this.pos++);
        } else {
            this.pos = 0;
            return null;
        }
    }

    public void reset() {
        this.clearAttachments();
        this.pos = 0;
    }
}
