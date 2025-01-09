package com.github.bannirui.msb.config;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.github.bannirui.msb.config.annotation.EnableMsbConfig;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

/**
 * 为业务应用Apollo namespace的配置注册观察者 监听配置的变更事件.
 */
public class ConfigChangeListenerAdapter implements ApplicationContextAware, BeanPostProcessor {
    private static final Logger log = LoggerFactory.getLogger(ConfigChangeListenerAdapter.class);
    private ApplicationContext applicationContext;
    private static final String APOLLO_LISTENER_CLASSES = "com.github.bannirui.msb.config.ConfigChangeListener";
    private static final List<String> apollo_listener_methods = Arrays.asList("apolloConfigOnChange", "publishEvent", "publishApolloConfigChangeEvent");
    private static final String apollo_namespace_application = "application";
    public static final String LISTENER_ALL_NAMESPACE_SWITCH_KEY = "msb.config.listen.allnamespace";
    /**
     * {@link EnableMsbConfig}注解打在的类.
     */
    private Class<?> bootstrapClass;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> clazz = bean.getClass();
        // 场景启动器一般打在启动类
        if (AnnotationUtils.findAnnotation(clazz, EnableMsbConfig.class) != null) {
            this.bootstrapClass = clazz;
            return bean;
        } else {
            if (Objects.equals(clazz.getName(), ConfigChangeListener.class.getName())) {
                List<Method> executeMethods = new ArrayList<>();
                Method[] methods = clazz.getDeclaredMethods();
                for (Method method : methods) {
                    if (apollo_listener_methods.contains(method.getName())) {
                        executeMethods.add(method);
                    }
                }
                if (!executeMethods.isEmpty()) {
                    /**
                     * {@link EnableMsbConfig}注解指定的apollo的namespace
                     */
                    List<String> validListeningNamespaces = new ArrayList<>();
                    EnableMsbConfig enableConfigAnnotation = AnnotationUtils.findAnnotation(this.bootstrapClass, EnableMsbConfig.class);
                    if (Objects.nonNull(enableConfigAnnotation)) {
                        // 业务应用的Apollo配置namespace
                        validListeningNamespaces.addAll(Arrays.asList(enableConfigAnnotation.value()));
                    }
                    for (String namespace : validListeningNamespaces) {
                        // apollo的config注册监听器 当配置发生了变更进行回调实现热更新
                        Config config = ConfigService.getConfig(namespace);
                        for (Method method : executeMethods) {
                            config.addChangeListener(changeEvent -> ReflectionUtils.invokeMethod(method, bean, new Object[] {changeEvent}));
                        }
                    }
                }
            }
            return bean;
        }
    }
}
