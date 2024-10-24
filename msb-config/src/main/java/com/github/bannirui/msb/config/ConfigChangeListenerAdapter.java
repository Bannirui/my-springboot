package com.github.bannirui.msb.config;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.github.bannirui.msb.common.util.StringUtil;
import com.github.bannirui.msb.config.annotation.EnableMsbConfig;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

public class ConfigChangeListenerAdapter implements ApplicationContextAware, BeanPostProcessor {
    private ApplicationContext applicationContext;
    private static final String APOLLO_LISTENER_CLASSES = "com.github.bannirui.msb.config.ConfigChangeListener";
    private static final List<String> APOLLO_LISTENER_METHODS =
        Arrays.asList("apolloConfigOnChange", "publishEvent", "publishApolloConfigChangeEvent");
    private static final String APOLLO_NAMESPACE_APPLICATION = "application";
    public static final String ZSM_CONFIG_NAMESPACE = "_msb.System.Config";
    private static final String LISTENER_ALL_NAMESPACE_SWITCH_KEY = "msb.config.listen.allnamespace";
    /**
     * {@link EnableMsbConfig}注解打在的类.
     */
    private Class<?> bootstrapClass;

    public ConfigChangeListenerAdapter() {
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class clazz = bean.getClass();
        if (AnnotationUtils.findAnnotation(clazz, EnableMsbConfig.class) != null) {
            this.bootstrapClass = clazz;
            return bean;
        } else {
            if (clazz.getName().equals("com.github.bannirui.msb.config.ConfigChangeListener")) {
                List<Method> executeMethods = new ArrayList();
                Method[] methods = clazz.getDeclaredMethods();
                for (Method method : methods) {
                    if (APOLLO_LISTENER_METHODS.contains(method.getName())) {
                        executeMethods.add(method);
                    }
                }

                if (!executeMethods.isEmpty()) {
                    /**
                     * {@link EnableMsbConfig}注解指定的apollo的namespace
                     */
                    List<String> validListeningNamespaces = new ArrayList();
                    if (StringUtil.isNotEmpty(this.applicationContext.getEnvironment().getProperty("msb.config.listen.allnamespace"))) {
                        EnableMsbConfig enableConfigAnnotation =
                            AnnotationUtils.findAnnotation(this.bootstrapClass, EnableMsbConfig.class);
                        if (enableConfigAnnotation != null) {
                            validListeningNamespaces = Arrays.asList(enableConfigAnnotation.value());
                        }
                    } else {
                        validListeningNamespaces.add("application");
                        validListeningNamespaces.add("_msb.System.Config");
                    }
                    for (String namespace : validListeningNamespaces) {
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
