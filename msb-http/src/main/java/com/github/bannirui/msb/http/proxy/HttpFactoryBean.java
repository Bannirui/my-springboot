package com.github.bannirui.msb.http.proxy;

import org.springframework.beans.factory.FactoryBean;

public class HttpFactoryBean<T> implements FactoryBean<T> {
    private Class<T> httpFactoryBean;

    public HttpFactoryBean(Class<T> httpFactoryBean) {
        this.httpFactoryBean = httpFactoryBean;
    }

    @Override
    public T getObject() {
        return (T) (new HttpProxyFactory()).getInstance(this.httpFactoryBean);
    }

    @Override
    public Class<?> getObjectType() {
        return this.httpFactoryBean;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
