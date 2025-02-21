package com.github.bannirui.msb.http.constructor;

import com.github.bannirui.msb.http.annotation.HttpJsonExecute;
import java.util.Map;

public class DefaultHttpRequestBodyConstructor implements HttpRequestBodyConstructor {

    @Override
    public RequestEntity constructor(HttpJsonExecute httpJsonExecute, Object param, Map<String, String> headersParam) {
        RequestEntity requestEntity = new RequestEntity();
        requestEntity.setUrl(httpJsonExecute.value());
        requestEntity.setJsonRequestMethod(httpJsonExecute.method());
        requestEntity.setHeadersParam(headersParam);
        requestEntity.setParam(param);
        return requestEntity;
    }
}
