package com.github.bannirui.msb.common.plugin;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Objects;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;

public class InterceptorUtil {

    private static final String INTERCEPTOR_FILE_NAME = "com.github.bannirui.msb.common.plugin.Interceptor";

    public InterceptorUtil() {
    }

    public static <T> T getProxyObj(Class<T> clz, Class[] argumentTypes, Object[] arguments, String typePrefix) throws Exception {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clz);
        enhancer.setCallback(getInterceptor(typePrefix));
        enhancer.setClassLoader(clz.getClassLoader());
        return (T) enhancer.create(argumentTypes, arguments);
    }

    public static <T> T getProxyObj(Class<T> clz, String typePrefix) throws Exception {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clz);
        enhancer.setCallback(getInterceptor(typePrefix));
        enhancer.setClassLoader(clz.getClassLoader());
        return (T) enhancer.create();
    }

    private static Interceptor getInterceptor(String typePrefix) throws Exception {
        List<PluginDecorator<Class>> interceptorDecorators =
            PluginConfigManger.getOrderedPluginClasses("com.github.bannirui.msb.common.plugin.Interceptor", typePrefix, true);
        Interceptor interceptor = new Interceptor(null);
        if (Objects.isNull(interceptorDecorators) || interceptorDecorators.isEmpty()) {
            return interceptor;
        }
        for (PluginDecorator<Class> pd : interceptorDecorators) {
            Class<Interceptor> interceptorClass = pd.getPlugin();
            Constructor<Interceptor> constructor = interceptorClass.getConstructor(MethodInterceptor.class);
            interceptor = constructor.newInstance(interceptor);
        }
        return interceptor;
    }
}
