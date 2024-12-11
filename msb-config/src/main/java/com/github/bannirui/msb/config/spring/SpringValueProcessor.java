package com.github.bannirui.msb.config.spring;

import com.ctrip.framework.apollo.spring.property.PlaceholderHelper;
import com.ctrip.framework.apollo.spring.property.SpringValue;
import com.ctrip.framework.apollo.spring.property.SpringValueRegistry;
import com.ctrip.framework.apollo.spring.util.SpringInjector;
import com.github.bannirui.msb.common.env.EnvironmentMgr;
import com.github.bannirui.msb.config.PropertySourcesProcessor;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.util.CollectionUtils;

public class SpringValueProcessor extends ApolloProcessor implements BeanFactoryPostProcessor, BeanFactoryAware {
    private static final Logger logger = LoggerFactory.getLogger(SpringValueProcessor.class);
    private final PlaceholderHelper placeholderHelper = SpringInjector.getInstance(PlaceholderHelper.class);
    private final com.ctrip.framework.apollo.spring.property.SpringValueRegistry springValueRegistry = SpringInjector.getInstance(SpringValueRegistry.class);
    private BeanFactory beanFactory;
    private Multimap<String, SpringValueDefinition> beanName2SpringValueDefinitions = LinkedListMultimap.create();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        super.postProcessBeforeInitialization(bean, beanName);
        this.processBeanPropertyValues(bean, beanName);
        return bean;
    }

    @Override
    protected void processField(Object bean, String beanName, Field field) {
        Value value = field.getAnnotation(Value.class);
        if (value != null) {
            this.doRegister(bean, beanName, field, value);
        }
    }

    @Override
    protected void processMethod(Object bean, String beanName, Method method) {
        Value value = method.getAnnotation(Value.class);
        if (value != null) {
            if (method.getAnnotation(Bean.class) == null) {
                if (method.getParameterTypes().length != 1) {
                    logger.error("Ignore @Value setter {}.{}, expecting 1 parameter, actual {} parameters", bean.getClass().getName(), method.getName(), method.getParameterTypes().length);
                } else {
                    this.doRegister(bean, beanName, method, value);
                }
            }
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if ("true".equals(EnvironmentMgr.getProperty(PropertySourcesProcessor.AUTO_UPDATE_INJECTED_SPRING_PROPERTIES_OPTION)) && beanFactory instanceof BeanDefinitionRegistry) {
            this.beanName2SpringValueDefinitions = SpringValueDefinitionProcessor.getBeanName2SpringValueDefinitions((BeanDefinitionRegistry) beanFactory);
        }
    }

    private void processBeanPropertyValues(Object bean, String beanName) {
        Collection<SpringValueDefinition> propertySpringValues = this.beanName2SpringValueDefinitions.get(beanName);
        if(CollectionUtils.isEmpty(propertySpringValues)) return;
        for (SpringValueDefinition definition : propertySpringValues) {
            try {
                PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(bean.getClass(), definition.getPropertyName());
                Method method = pd.getWriteMethod();
                if (method != null) {
                    SpringValue springValue = new SpringValue(definition.getKey(), definition.getPlaceholder(), bean, beanName, method, false);
                    this.springValueRegistry.register(this.beanFactory, definition.getKey(), springValue);
                    logger.debug("Monitoring {}", springValue);
                }
            } catch (Throwable var9) {
                logger.error("Failed to enable auto update feature for {}.{}", bean.getClass(), definition.getPropertyName());
            }
        }
        this.beanName2SpringValueDefinitions.removeAll(beanName);
    }

    /**
     * 以配置PropertyName作键 {@link SpringValue}作值缓存到内存中
     * @param bean 持有配置值的Bean实例
     * @param member 要么通过属性注入 要么通过setter方法注入
     * @param value {@link Value}注解的可以是类成员 也可以是方法的参数 即支持成员注入和setter注入
     */
    private void doRegister(Object bean, String beanName, Member member, Value value) {
        // {@link Value}注解方法指定的配置PropertyName
        Set<String> keys = this.placeholderHelper.extractPlaceholderKeys(value.value());
        if(CollectionUtils.isEmpty(keys)) return;
        for (String key : keys) {
            SpringValue springValue = null;
            if (member instanceof Field field) {
                springValue = new SpringValue(key, value.value(), bean, beanName, field, false);
            } else {
                if (!(member instanceof Method method)) {
                    logger.error("Apollo @Value annotation currently only support to be used on methods and fields, but is used on {}",
                        member.getClass());
                    return;
                }
                springValue = new SpringValue(key, value.value(), bean, beanName, method, false);
            }
            // Apollo API缓存到内存中
            this.springValueRegistry.register(this.beanFactory, key, springValue);
            logger.info("Monitoring {}", springValue);
        }
    }
}
