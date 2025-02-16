package com.github.bannirui.msb.dubbo.autoconfig;

import com.github.bannirui.msb.dubbo.DubboAutoConfiguration;
import com.github.bannirui.msb.dubbo.DubboConfigDefaultCustomizer;
import com.github.bannirui.msb.dubbo.DubboRegistryBeanRegistrar;
import com.github.bannirui.msb.dubbo.annotation.EnableMsbDubbo;
import com.github.bannirui.msb.env.MsbEnvironmentMgr;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.spring.util.AnnotatedBeanDefinitionRegistryUtils;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;

public class ComponentImportRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {
    private ConfigurableEnvironment environment;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(EnableMsbDubbo.class.getName()));
        this.registerDubboConfigDefaultCustomizer(attributes, registry);
        /**
         * {@link EnableMsbDubbo#multipleConfig()}默认值true
         */
        boolean multipleConfig = attributes.getBoolean("multipleConfig");
        String isMultiple = MsbEnvironmentMgr.getProperty(this.environment, "dubbo.multiple");
        if (multipleConfig && (isMultiple == null || "true".equals(isMultiple))) {
            DubboRegistryBeanRegistrar registryBeanRegistrar = new DubboRegistryBeanRegistrar();
            registryBeanRegistrar.setEnvironment(this.environment);
            registryBeanRegistrar.registerBeanDefinitions(importingClassMetadata, registry);
        }
        AnnotatedBeanDefinitionRegistryUtils.registerBeans(registry, DubboAutoConfiguration.class);
    }

    private void registerDubboConfigDefaultCustomizer(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        String portStr = attributes.getString("port");
        Integer port = StringUtils.isNotBlank(portStr) ? Integer.valueOf(portStr) : null;
        String protocol = attributes.getString("protocol");
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(DubboConfigDefaultCustomizer.class);
        builder.addPropertyValue("dubboPort", port);
        builder.addPropertyValue("dubboProtocol", protocol);
        builder.setRole(2);
        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
        BeanDefinitionReaderUtils.registerWithGeneratedName(beanDefinition, registry);
    }

    @Override
    public void setEnvironment(Environment environment) {
        Assert.isInstanceOf(ConfigurableEnvironment.class, environment);
        this.environment = (ConfigurableEnvironment)environment;
    }
}
