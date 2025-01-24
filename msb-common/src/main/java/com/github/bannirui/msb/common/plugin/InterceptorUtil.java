package com.github.bannirui.msb.common.plugin;

import java.lang.reflect.Constructor;
import java.util.List;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.util.CollectionUtils;

public class InterceptorUtil {

    public static final String INTERCEPTOR_FILE_NAME = "com.github.bannirui.msb.common.plugin.Interceptor";

    /**
     * @param clz 要为谁创建代理实例
     * @param argumentTypes constructor signature
     * @param arguments compatible wrapped arguments to pass to constructor
     * @param typePrefix classpath:/META-INF/msb/plugin下配置文件中拦截器的key
     * @return 代理对象
     */
    public static <T> T getProxyObj(Class<T> clz, Class<?>[] argumentTypes, Object[] arguments, String typePrefix) throws Exception {
        Enhancer enhancer = new Enhancer();
        // 声明给clz代理
        enhancer.setSuperclass(clz);
        // 调用代理对象的任何方法都会被callback的intercept拦截
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

    /**
     * @param typePrefix 在classpath:/META-INF/msb/plugin下配置文件中property key的前缀 用于模糊匹配所有配置的拦截器
     * @return Interceptor对象 放到Enhancer的callback中
     */
    private static Interceptor getInterceptor(String typePrefix) throws Exception {
        // 拿到的拦截器集合是order升序 优先级降序 下面要把拦截器实例化出来逐级放到MethodInterceptor中 最终放到Enhancer的callback中 放置的顺序就是优先级的逆序
        List<PluginDecorator<Class<?>>> interceptorDecorators = PluginConfigManger.getOrderedPluginClasses("com.github.bannirui.msb.common.plugin.Interceptor", typePrefix, true);
        Interceptor interceptor = new Interceptor(null);
        if(CollectionUtils.isEmpty(interceptorDecorators)) {
            return interceptor;
        }
        /**
         * {@link Enhancer#setCallback}指定代理的回调 会拦截目标对象的所有方法 在{@link Interceptor#intercept}得到回调
         * classpath:/META-INF/ms/plugin下配置文件中可以为一个扩展点配置多个拦截器 采用方法就类似类加载机制的parent逐级代理
         * for轮询最早处理的是优先级最低的 也就是{@link com.github.bannirui.msb.common.annotation.MsbPlugin#order}最大的 将是被Enhancer的回调最后处理到的
         */
        for (PluginDecorator<Class<?>> pd : interceptorDecorators) {
            // 拦截器类
            Class<Interceptor> interceptorClass = (Class<Interceptor>)pd.getPlugin();
            // 拿到拦截器的有参构造方法 构造方法的参数是MethodInterceptor
            Constructor<Interceptor> constructor = interceptorClass.getConstructor(MethodInterceptor.class);
            // 创建MethodInterceptor实例作为interceptor的持有对象
            Interceptor tmp = constructor.newInstance(interceptor);
            interceptor = new Interceptor(tmp);
        }
        return interceptor;
    }
}
