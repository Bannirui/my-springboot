package com.github.bannirui.msb.http.autoconfig;

import com.github.bannirui.msb.http.constructor.DefaultHttpRequestBodyConstructor;
import com.github.bannirui.msb.http.proxy.HttpScannerConfigurer;
import com.github.bannirui.msb.http.util.ApplicationContextUtil;
import com.github.bannirui.msb.register.AbstractBeanRegistrar;
import com.github.bannirui.msb.register.BeanDefinition;

/**
 * 实现对http client的整合.
 */
public class HttpConfig extends AbstractBeanRegistrar {

    @Override
    public void registerBeans() {
        super.registerBeanDefinitionIfNotExists(BeanDefinition.newInstance(HttpScannerConfigurer.class));
        super.registerBeanDefinitionIfNotExists(BeanDefinition.newInstance(ApplicationContextUtil.class));
        super.registerBeanDefinitionIfNotExists(BeanDefinition.newInstance(DefaultHttpRequestBodyConstructor.class));
    }
}
