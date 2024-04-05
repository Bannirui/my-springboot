package com.github.bannirui.msb.remotecfg;

import com.github.bannirui.msb.common.annotation.EnableMyFramework;
import com.github.bannirui.msb.common.exception.InvalidException;
import com.github.bannirui.msb.remotecfg.annotation.EnableMyRemoteCfg;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;

/**
 * 确保{@link EnableMyRemoteCfg}打在启动类上.
 * 换言之 如果打在其他类上要结束启动流程.
 */
public class EnableRemoteCfgAnnotationCheck implements InstantiationAwareBeanPostProcessor {

    public EnableRemoteCfgAnnotationCheck() {
    }

    /**
     * Bean的实例化依赖的是反射 比较消耗性能.
     * 因此尽早拿到目标BeanClass进行判断 不符合条件的就不要进行实例化了.
     */
    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        if (beanClass.isAnnotationPresent(EnableMyRemoteCfg.class) && !beanClass.isAnnotationPresent(EnableMyFramework.class)) {
            // 这个注解必须在启动类上用
            throw new InvalidException("启用远程配置中心的注解@EnableRemoteCfg必须打在启动类上");
        }
        return InstantiationAwareBeanPostProcessor.super.postProcessBeforeInstantiation(beanClass, beanName);
    }
}
