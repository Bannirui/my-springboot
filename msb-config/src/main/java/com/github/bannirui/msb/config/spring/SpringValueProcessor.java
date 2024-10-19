package com.github.bannirui.msb.config.spring;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class SpringValueProcessor extends ApolloProcessor {
    private static final Logger logger = LoggerFactory.getLogger(SpringValueProcessor.class);
    private final PlaceholderHelper placeholderHelper = (PlaceholderHelper) SpringInjector.getInstance(PlaceholderHelper.class);
    private final SpringValueRegistry springValueRegistry = (SpringValueRegistry) SpringInjector.getInstance(SpringValueRegistry.class);
    private BeanFactory beanFactory;
    private Multimap<String, SpringValueDefinition> beanName2SpringValueDefinitions = LinkedListMultimap.create();

    public SpringValueProcessor() {
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        super.postProcessBeforeInitialization(bean, beanName);
        this.processBeanPropertyValues(bean, beanName);
        return bean;
    }

    @Override
    protected void processField(Object bean, String beanName, Field field) {
        Value value = (Value) field.getAnnotation(Value.class);
        if (value != null) {
            this.doRegister(bean, beanName, field, value);
        }
    }

    @Override
    protected void processMethod(Object bean, String beanName, Method method) {
        Value value = (Value) method.getAnnotation(Value.class);
        if (value != null) {
            if (method.getAnnotation(Bean.class) == null) {
                if (method.getParameterTypes().length != 1) {
                    logger.error("Ignore @Value setter {}.{}, expecting 1 parameter, actual {} parameters",
                        new Object[] {bean.getClass().getName(), method.getName(), method.getParameterTypes().length});
                } else {
                    this.doRegister(bean, beanName, method, value);
                }
            }
        }
    }

    private void processBeanPropertyValues(Object bean, String beanName) {
        Collection<SpringValueDefinition> propertySpringValues = this.beanName2SpringValueDefinitions.get(beanName);
        if (propertySpringValues != null && !propertySpringValues.isEmpty()) {
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
    }

    private void doRegister(Object bean, String beanName, Member member, Value value) {
        Set<String> keys = this.placeholderHelper.extractPlaceholderKeys(value.value());
        if (!keys.isEmpty()) {
            for (String key : keys) {
                SpringValue springValue;
                if (member instanceof Field) {
                    Field field = (Field) member;
                    springValue = new SpringValue(key, value.value(), bean, beanName, field, false);
                } else {
                    if (!(member instanceof Method)) {
                        logger.error("Apollo @Value annotation currently only support to be used on methods and fields, but is used on {}",
                            member.getClass());
                        return;
                    }
                    Method method = (Method) member;
                    springValue = new SpringValue(key, value.value(), bean, beanName, method, false);
                }
                this.springValueRegistry.register(this.beanFactory, key, springValue);
                logger.info("Monitoring {}", springValue);
            }
        }
    }
}
