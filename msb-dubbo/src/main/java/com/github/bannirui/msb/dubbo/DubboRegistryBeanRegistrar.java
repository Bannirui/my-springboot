package com.github.bannirui.msb.dubbo;

import com.github.bannirui.msb.dubbo.postprocessor.DubboConfigBindingBeanPostProcessor;
import com.github.bannirui.msb.ex.FrameworkException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.MapUtils;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.spring.util.PropertySourcesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;

/**
 * 注册{@link DubboConfigBindingBeanPostProcessor}负责dubbo注册中心
 */
public class DubboRegistryBeanRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {
    private static final Logger log = LoggerFactory.getLogger(DubboRegistryBeanRegistrar.class);
    private ConfigurableEnvironment environment;

    @Override
    public void setEnvironment(Environment environment) {
        Assert.isInstanceOf(ConfigurableEnvironment.class, environment);
        this.environment = (ConfigurableEnvironment)environment;
    }

    /**
     * 注册{@link DubboConfigBindingBeanPostProcessor}负责dubbo注册中心
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        // 配置的注册中心zk
        Map<String, Object> properties = PropertySourcesUtils.getSubProperties(this.environment.getPropertySources(), "dubbo.registries");
        if (MapUtils.isEmpty(properties)) {
            DubboRegistryBeanRegistrar.log.info("There is no property for binding to dubbo config class [{}] within prefix [dubbo.registries]", RegistryConfig.class.getName());
            throw FrameworkException.getInstance("没有配置dubbo服务注册中心");
        }
        Set<String> beanNames = this.resolveMultipleBeanNames(properties);
        for (String beanName : beanNames) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(RegistryConfig.class);
            AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
            registry.registerBeanDefinition(beanName, beanDefinition);
            this.registerDubboConfigBindingBeanPostProcessor("dubbo.registries", beanName, registry);
        }
    }

    /**
     * 注册{@link DubboConfigBindingBeanPostProcessor}负责dubbo注册中心
     * 比如dubbo.registries.zookeeper.address
     * @param prefix 配置前缀 比如dubbo.registries
     * @param beanName 配置前缀后面第一级名称 比如zookeeper
     */
    private void registerDubboConfigBindingBeanPostProcessor(String prefix, String beanName, BeanDefinitionRegistry registry) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(DubboConfigBindingBeanPostProcessor.class);
        String actualPrefix = PropertySourcesUtils.normalizePrefix(prefix) + beanName;
        builder.addConstructorArgValue(actualPrefix).addConstructorArgValue(beanName);
        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
        beanDefinition.setRole(2);
        BeanDefinitionReaderUtils.registerWithGeneratedName(beanDefinition, registry);
        DubboRegistryBeanRegistrar.log.info("The BeanPostProcessor bean definition [{}] for dubbo config bean [name : {}] has been registered.", DubboConfigBindingBeanPostProcessor.class.getName(), beanName);
    }

    /**
     * 可能有多个配置 比如注册中心
     * <ul>
     *     <li>dubbo.registries.zookeeper.address</li>
     *     <li>dubbo.registries.redis.address</li>
     * </ul>
     * @return [zookeeper, redis]
     */
    private Set<String> resolveMultipleBeanNames(Map<String, Object> properties) {
        Set<String> beanNames = new LinkedHashSet<>();
        properties.forEach((propertyName, v) -> {
            int index = propertyName.indexOf(".");
            if (index > 0) {
                String beanName = propertyName.substring(0, index);
                beanNames.add(beanName);
            }
        });
        return beanNames;
    }
}
