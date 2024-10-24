package com.github.bannirui.msb.common.plugin;

import java.lang.reflect.Method;
import java.util.Objects;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

public class Interceptor implements MethodInterceptor {

    private MethodInterceptor methodInterceptor;

    public Interceptor(MethodInterceptor methodInterceptor) {
        this.methodInterceptor = methodInterceptor;
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        Object result = null;
        this.doBefore(proxy, method, args, proxy);
        if (Objects.isNull(this.methodInterceptor)) {
            result = proxy.invokeSuper(obj, args);
        } else {
            result = this.methodInterceptor.intercept(obj, method, args, proxy);
        }
        this.doAfter(result, obj, method, args, proxy);
        return result;
    }

    protected void doBefore(Object proxy, Method method, Object[] params, MethodProxy methodProxy) {
    }

    protected void doAfter(Object result, Object proxy, Method method, Object[] param, MethodProxy methodProxy) {
    }
}
