package com.github.bannirui.msb.config;

import com.ctrip.framework.apollo.spring.annotation.ApolloAnnotationProcessor;
import com.ctrip.framework.apollo.spring.util.BeanRegistrationUtil;
import com.github.bannirui.msb.common.util.ArrayUtil;
import com.github.bannirui.msb.config.annotation.EnableMsbConfig;
import com.github.bannirui.msb.config.spring.SpringValueDefinitionProcessor;
import com.github.bannirui.msb.config.spring.SpringValueProcessor;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 通过{@link org.springframework.context.annotation.Import}向容器注册Bean.
 * 获取{@link com.github.bannirui.msb.config.annotation.EnableMsbConfig}注解目标类的原信息.
 */
public class ConfigRegistrar implements ImportBeanDefinitionRegistrar {

    /**
     * 获取{@link com.github.bannirui.msb.config.annotation.EnableMsbConfig}注解的元信息.
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(EnableMsbConfig.class.getName()));
        // 应用关注的配置中心的namespace
        String[] namespaces = attributes.getStringArray("value");
        int order = attributes.getNumber("order");
        if(!ArrayUtil.isEmpty(namespaces)) {
            PropertySourcesProcessor.addNamespaces(Lists.newArrayList(namespaces), order);
        }
        // ${}
        BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry, PropertySourcesPlaceholderConfigurer.class.getName(), PropertySourcesPlaceholderConfigurer.class);
        BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry, PropertySourcesProcessor.class.getName(), PropertySourcesProcessor.class);
        BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry, ApolloAnnotationProcessor.class.getName(), ApolloAnnotationProcessor.class);
        //
        BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry, SpringValueProcessor.class.getName(), SpringValueProcessor.class);
        BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry, SpringValueDefinitionProcessor.class.getName(), SpringValueDefinitionProcessor.class);
    }
}
