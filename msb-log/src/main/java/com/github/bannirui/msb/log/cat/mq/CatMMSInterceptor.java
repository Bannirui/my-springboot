package com.github.bannirui.msb.log.cat.mq;

import com.github.bannirui.msb.annotation.MsbPlugin;
import com.github.bannirui.msb.plugin.Interceptor;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * {@link MMSTemplate}代理拦截器
 */
@MsbPlugin(order = 0)
public class CatMMSInterceptor extends Interceptor {

    public CatMMSInterceptor(MethodInterceptor methodInterceptor) {
        super(methodInterceptor);
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        return super.intercept(obj, method, args, proxy);
    }
}
