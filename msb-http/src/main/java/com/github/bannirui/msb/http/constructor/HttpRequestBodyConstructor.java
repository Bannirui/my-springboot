package com.github.bannirui.msb.http.constructor;

import com.github.bannirui.msb.http.annotation.HttpJsonExecute;
import java.util.Map;

public interface HttpRequestBodyConstructor {
    RequestEntity constructor(HttpJsonExecute httpJsonExecute, Object param, Map<String, String> headersParam);
}
