package com.github.bannirui.msb.http.config;

import java.util.Objects;

public class HttpConfigPropertiesProvider {
    private static HttpConfigProperties httpConfigProperties;

    public static void setHttpConfigProperties(HttpConfigProperties httpConfigProperties) {
        HttpConfigPropertiesProvider.httpConfigProperties = httpConfigProperties;
    }

    public static HttpConfigProperties getHttpConfigProperties() {
        if(Objects.isNull(httpConfigProperties)) {
            return new HttpConfigProperties();
        }
        return httpConfigProperties;
    }
}
