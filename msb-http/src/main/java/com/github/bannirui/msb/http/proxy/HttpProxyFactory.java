package com.github.bannirui.msb.http.proxy;

import java.lang.reflect.Proxy;

public class HttpProxyFactory<T> {
    public HttpProxyFactory() {
    }

    public T getInstance(Class<T> cls) {
        return (T) Proxy.newProxyInstance(cls.getClassLoader(), new Class[]{cls}, new HttpProxy());
    }
}
