package com.github.bannirui.msb.register;

import com.github.bannirui.msb.env.MsbEnvironmentMgr;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

import java.util.Objects;

public abstract class AbstractBeanRegistrar implements ApplicationContextAware, BeanDefinitionRegistryPostProcessor, EnvironmentAware {

    private ConfigurableEnvironment env;
    protected BeanDefinitionRegistry registry;
    protected ApplicationContext applicationContext;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        this.registry = registry;
        this.registerBeans();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext=applicationContext;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.env=(ConfigurableEnvironment) environment;
    }

    public abstract void registerBeans();

    /**
     *
     * @param beanDefinition
     * @return <t>true</t>标识注册成功 <t>false</t>标识注册
     */
    public boolean registerBeanDefinitionIfNotExists(BeanDefinition<?> beanDefinition) {
        String beanName = beanDefinition.getBeanName() == null ? beanDefinition.getBeanClass().getName() : beanDefinition.getBeanName();
        if (this.registry.containsBeanDefinition(beanName)) {
            return false;
        }
        String[] candidates = this.registry.getBeanDefinitionNames();
        for (String candidate : candidates) {
            org.springframework.beans.factory.config.BeanDefinition springBeanDefinition = this.registry.getBeanDefinition(candidate);
            if (Objects.equals(springBeanDefinition.getBeanClassName(), beanDefinition.getBeanClass().getName()) && Objects.equals(candidate, beanName)) {
                return false;
            }
        }
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(beanDefinition.getBeanClass());
        if (beanDefinition.getDependsOnList() != null) {
            beanDefinition.getDependsOnList().forEach((dependsOn) -> {
                beanDefinitionBuilder.addDependsOn((String) dependsOn);
            });
        }
        if (beanDefinition.getConstructorArgumentValues() != null) {
            beanDefinition.getConstructorArgumentValues().forEach((arg) -> {
                beanDefinitionBuilder.addConstructorArgValue(arg);
            });
        }
        if (beanDefinition.getConstructorArgumentBeanNames() != null) {
            beanDefinition.getConstructorArgumentBeanNames().forEach((arg) -> {
                beanDefinitionBuilder.addConstructorArgReference((String)arg);
            });
        }
        if (beanDefinition.getDestroyMethodName() != null) {
            beanDefinitionBuilder.setDestroyMethodName(beanDefinition.getDestroyMethodName());
        }
        if (beanDefinition.getInitMethodName() != null) {
            beanDefinitionBuilder.setInitMethodName(beanDefinition.getInitMethodName());
        }
        if (beanDefinition.getProperties() != null) {
            beanDefinition.getProperties().forEach((name, value) -> {
                beanDefinitionBuilder.addPropertyValue((String)name, value);
            });
        }
        if (beanDefinition.getPropertiesBeanNames() != null) {
            beanDefinition.getPropertiesBeanNames().forEach((name, value) -> {
                beanDefinitionBuilder.addPropertyReference((String)name, (String)value);
            });
        }
        this.registry.registerBeanDefinition(beanName, beanDefinitionBuilder.getBeanDefinition());
        return true;
    }

    public boolean registerGenericBeanDefinitionIfNotExists(GenericBeanDefinition beanDefinition) {
        String beanName = beanDefinition.getBeanClassName();
        if (this.registry.containsBeanDefinition(beanName)) {
            return false;
        }
        String[] candidates = this.registry.getBeanDefinitionNames();
        for (String candidate : candidates) {
            org.springframework.beans.factory.config.BeanDefinition springBeanDefinition = this.registry.getBeanDefinition(candidate);
            if (Objects.equals(springBeanDefinition.getBeanClassName(), beanDefinition.getBeanClass().getName()) && Objects.equals(candidate, beanName)) {
                return false;
            }
        }
        this.registry.registerBeanDefinition(beanName, beanDefinition);
        return true;
    }

    /**
     * 读取msb配置优先级 JVM->远程配置中心->本地配置文件
     */
    public String getProperty(String key) {
        return MsbEnvironmentMgr.getProperty(key);
    }

    public String getPropertyNotNull(String key) {
        String value = this.getProperty(key);
        Assert.notNull(value, key + "为空");
        return value;
    }

    public String getProperty(String key, String defaultValue) {
        String property = this.getProperty(key);
        return property == null ? defaultValue : property;
    }

    public Long getPropertyAsLong(String key) {
        String property = this.getProperty(key);
        return property == null ? null : NumberUtils.toLong(property);
    }

    public Long getPropertyAsLongNotNull(String key) {
        Long value = this.getPropertyAsLong(key);
        Assert.notNull(value, key + "为空");
        return value;
    }

    public Long getPropertyAsLong(String key, Long defaultValue) {
        Long value = this.getPropertyAsLong(key);
        return value == null ? defaultValue : value;
    }

    public Integer getPropertyAsInteger(String key) {
        String property = this.getProperty(key);
        return property == null ? null : NumberUtils.toInt(property);
    }

    public Integer getPropertyAsIntegerNotNull(String key) {
        Integer value = this.getPropertyAsInteger(key);
        Assert.notNull(value, key + "为空");
        return value;
    }

    public Integer getPropertyAsInteger(String key, Integer defaultValue) {
        Integer value = this.getPropertyAsInteger(key);
        return value == null ? defaultValue : value;
    }

    public Double getPropertyAsDouble(String key) {
        String property = this.getProperty(key);
        return property == null ? null : NumberUtils.toDouble(property);
    }

    public Double getPropertyAsDoubleNotNull(String key) {
        Double value = this.getPropertyAsDouble(key);
        Assert.notNull(value, key + "为空");
        return value;
    }

    public Double getPropertyAsDouble(String key, Double defaultValue) {
        Double value = this.getPropertyAsDouble(key);
        return value == null ? defaultValue : value;
    }

    public Boolean getPropertyAsBoolean(String key) {
        String property = this.getProperty(key);
        return property == null ? false : Boolean.parseBoolean(property);
    }

    public Boolean getPropertyAsBoolean(String key, Boolean defaultValue) {
        String property = this.getProperty(key);
        return property == null ? defaultValue : Boolean.parseBoolean(property);
    }
}
