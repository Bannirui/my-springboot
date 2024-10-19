package com.github.bannirui.msb.common.startup.monitor;

import com.github.bannirui.msb.common.env.EnvironmentMgr;
import com.github.bannirui.msb.common.register.AbstractBeanRegistrar;
import com.github.bannirui.msb.common.register.BeanDefinition;

public class AppInfoMonitorConfiguration extends AbstractBeanRegistrar {

    public AppInfoMonitorConfiguration() {
    }

    @Override
    public void registerBeans() {
        if ("true".equals(EnvironmentMgr.getProperty(this.env, "isMonitor"))) {
            super.registerBeanDefinitionIfNotExists(BeanDefinition.newInstance(AppInfoMonitor.class));
        }
    }
}
