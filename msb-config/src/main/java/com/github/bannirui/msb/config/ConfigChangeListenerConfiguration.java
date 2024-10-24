package com.github.bannirui.msb.config;

import com.github.bannirui.msb.common.listener.ConfigChangeListenerSpringDetector;
import com.github.bannirui.msb.common.listener.ConfigChangeSpringMulticaster;
import com.github.bannirui.msb.common.register.AbstractBeanRegistrar;
import com.github.bannirui.msb.common.register.BeanDefinition;

public class ConfigChangeListenerConfiguration extends AbstractBeanRegistrar {
    public ConfigChangeListenerConfiguration() {
    }

    @Override
    public void registerBeans() {
        this.registerBeanDefinitionIfNotExists(BeanDefinition.newInstance(ConfigChangeListener.class));
        this.registerBeanDefinitionIfNotExists(BeanDefinition.newInstance(ConfigChangeListenerSpringDetector.class));
        this.registerBeanDefinitionIfNotExists(BeanDefinition.newInstance(ConfigChangeSpringMulticaster.class));
    }
}
