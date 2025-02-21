package com.github.bannirui.msb.http.constructor;

import com.github.bannirui.msb.http.enums.JsonRequestMethod;
import java.util.Map;

public class RequestEntity {
    private String url;
    private JsonRequestMethod jsonRequestMethod;
    private Object param;
    private Map<String, String> headersParam;

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public JsonRequestMethod getJsonRequestMethod() {
        return this.jsonRequestMethod;
    }

    public void setJsonRequestMethod(JsonRequestMethod jsonRequestMethod) {
        this.jsonRequestMethod = jsonRequestMethod;
    }

    public Object getParam() {
        return this.param;
    }

    public void setParam(Object param) {
        this.param = param;
    }

    public Map<String, String> getHeadersParam() {
        return this.headersParam;
    }

    public void setHeadersParam(Map<String, String> headersParam) {
        this.headersParam = headersParam;
    }
}
