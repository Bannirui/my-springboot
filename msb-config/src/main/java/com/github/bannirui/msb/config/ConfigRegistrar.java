package com.github.bannirui.msb.config;

import com.ctrip.framework.apollo.spring.annotation.ApolloAnnotationProcessor;
import com.ctrip.framework.apollo.spring.annotation.SpringValueProcessor;
import com.ctrip.framework.apollo.spring.property.SpringValueDefinitionProcessor;
import com.ctrip.framework.apollo.spring.util.BeanRegistrationUtil;
import com.github.bannirui.msb.common.util.ArrayUtil;
import com.github.bannirui.msb.config.annotation.EnableMsbConfig;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 通过{@link org.springframework.context.annotation.Import}向容器注册Bean.
 * 对Apollo配置的热更新支持
 * <ul>
 *     <li>获取{@link com.github.bannirui.msb.config.annotation.EnableMsbConfig}注解目标类的nameSpace信息</li>
 *     <li>为Apollo的所有配置注册监听器</li>
 *     <li>配置变更后反射Spring Bean实现内存配置更新</li>
 * </ul>
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
        // ${}解析
        BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry, PropertySourcesPlaceholderConfigurer.class.getName(), PropertySourcesPlaceholderConfigurer.class);
        // 向Apollo注册配置变更监听器
        BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry, PropertySourcesProcessor.class.getName(), PropertySourcesProcessor.class);
        // 轮询所有Bean的成员和方法 对于Spring而言 对接的是{@link Value}注解作用的对象
        BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry, ApolloAnnotationProcessor.class.getName(), ApolloAnnotationProcessor.class);
        // Spring中{@link Value}注解的成员或者setter方法参数缓存起来 监听配置变更回调
        BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry, SpringValueProcessor.class.getName(), SpringValueProcessor.class);
        // xml配置
        BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry, SpringValueDefinitionProcessor.class.getName(), SpringValueDefinitionProcessor.class);
    }
}
