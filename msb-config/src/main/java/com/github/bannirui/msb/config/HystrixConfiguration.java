package com.github.bannirui.msb.config;

import com.github.bannirui.msb.register.AbstractBeanRegistrar;
import com.github.bannirui.msb.register.BeanDefinition;
import com.netflix.hystrix.contrib.javanica.aop.aspectj.HystrixCommandAspect;

public class HystrixConfiguration extends AbstractBeanRegistrar {

    public HystrixConfiguration() {
    }

    @Override
    public void registerBeans() {
        super.registerBeanDefinitionIfNotExists(BeanDefinition.newInstance(HystrixCommandAspect.class));
        super.registerBeanDefinitionIfNotExists(BeanDefinition.newInstance(DynamicHystrixConfig.class));
    }
}
