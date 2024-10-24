package com.github.bannirui.msb.config.spring;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.util.ReflectionUtils;

public abstract class ApolloProcessor implements BeanPostProcessor, PriorityOrdered {
    public ApolloProcessor() {
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> clazz = bean.getClass();
        for (Field field : this.findAllField(clazz)) {
            this.processField(bean, beanName, field);
        }
        for (Method method : this.findAllMethod(clazz)) {
            this.processMethod(bean, beanName, method);
        }
        return bean;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    /**
     * Bean的类成员被{@link org.springframework.beans.factory.annotation.Value}注解标识了.
     * 将这些成员缓存起来 将来用作配置热更新
     */
    protected abstract void processField(Object bean, String beanName, Field field);

    /**
     * Bean的配置注入通过setter方式
     * 将这些setter方法缓存起来 将来用作配置热更新
     * 目前不支持
     */
    protected abstract void processMethod(Object bean, String beanName, Method method);

    private List<Method> findAllMethod(Class<?> clazz) {
        final List<Method> ret = new LinkedList<>();
        ReflectionUtils.doWithMethods(clazz, ret::add);
        return ret;
    }

    private List<Field> findAllField(Class<?> clazz) {
        final List<Field> ret = new LinkedList<>();
        ReflectionUtils.doWithFields(clazz, ret::add);
        return ret;
    }
}
