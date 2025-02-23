package com.github.bannirui.msb.dfs.config;

import com.github.bannirui.msb.register.AbstractBeanRegistrar;
import com.github.bannirui.msb.register.BeanDefinition;

public class DfsConfiguration extends AbstractBeanRegistrar {

    @Override
    public void registerBeans() {
        super.registerBeanDefinitionIfNotExists(BeanDefinition.newInstance(FastDfsTemplate.class));
    }
}
