package com.github.bannirui.msb.config;

import com.github.bannirui.msb.listener.ConfigChangeListenerSpringDetector;
import com.github.bannirui.msb.listener.ConfigChangeSpringMulticaster;
import com.github.bannirui.msb.register.AbstractBeanRegistrar;
import com.github.bannirui.msb.register.BeanDefinition;

public class ConfigChangeListenerConfiguration extends AbstractBeanRegistrar {

    @Override
    public void registerBeans() {
        this.registerBeanDefinitionIfNotExists(BeanDefinition.newInstance(ConfigChangeListener.class));
        this.registerBeanDefinitionIfNotExists(BeanDefinition.newInstance(ConfigChangeListenerSpringDetector.class));
        this.registerBeanDefinitionIfNotExists(BeanDefinition.newInstance(ConfigChangeSpringMulticaster.class));
    }
}
