package com.github.bannirui.msb.log.cat.mq;

import com.github.bannirui.msb.annotation.MsbPlugin;
import com.github.bannirui.msb.plugin.Interceptor;
import java.lang.reflect.Method;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

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
        // TODO: 2025/1/23 mmsTemplate发送的mq消息
        return super.intercept(obj, method, args, proxy);
    }
}
