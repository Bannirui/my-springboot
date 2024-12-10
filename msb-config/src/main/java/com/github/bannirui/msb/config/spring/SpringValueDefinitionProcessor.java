package com.github.bannirui.msb.config.spring;

import com.github.bannirui.msb.common.env.EnvironmentMgr;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

public class SpringValueDefinitionProcessor implements BeanDefinitionRegistryPostProcessor {
    private static final Map<BeanDefinitionRegistry, Multimap<String, SpringValueDefinition>> beanName2SpringValueDefinitions =
        Maps.newConcurrentMap();
    private static final Set<BeanDefinitionRegistry> PROPERTY_VALUES_PROCESSED_BEAN_FACTORIES = Sets.newConcurrentHashSet();
    private final PlaceholderHelper placeholderHelper = SpringInjector.getInstance(PlaceholderHelper.class);

    public SpringValueDefinitionProcessor() {
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        if ("true".equals(EnvironmentMgr.getProperty("autoUpdateInjectedSpringProperties"))) {
            this.processPropertyValues(registry);
        }
    }

    public static Multimap<String, SpringValueDefinition> getBeanName2SpringValueDefinitions(BeanDefinitionRegistry registry) {
        Multimap<String, SpringValueDefinition> springValueDefinitions = (Multimap) beanName2SpringValueDefinitions.get(registry);
        if (springValueDefinitions == null) {
            springValueDefinitions = LinkedListMultimap.create();
        }
        return (Multimap) springValueDefinitions;
    }

    private void processPropertyValues(BeanDefinitionRegistry beanRegistry) {
        if (PROPERTY_VALUES_PROCESSED_BEAN_FACTORIES.add(beanRegistry)) {
            if (!beanName2SpringValueDefinitions.containsKey(beanRegistry)) {
                beanName2SpringValueDefinitions.put(beanRegistry, LinkedListMultimap.create());
            }
            Multimap<String, SpringValueDefinition> springValueDefinitions = (Multimap) beanName2SpringValueDefinitions.get(beanRegistry);
            String[] beanNames = beanRegistry.getBeanDefinitionNames();
            for (String beanName : beanNames) {
                BeanDefinition beanDefinition = beanRegistry.getBeanDefinition(beanName);
                MutablePropertyValues mutablePropertyValues = beanDefinition.getPropertyValues();
                List<PropertyValue> propertyValues = mutablePropertyValues.getPropertyValueList();
                for (PropertyValue propertyValue : propertyValues) {
                    Object value = propertyValue.getValue();
                    if (value instanceof TypedStringValue) {
                        String placeholder = ((TypedStringValue) value).getValue();
                        Set<String> keys = this.placeholderHelper.extractPlaceholderKeys(placeholder);
                        for (String key : keys) {
                            springValueDefinitions.put(beanName, new SpringValueDefinition(key, placeholder, propertyValue.getName()));
                        }
                    }
                }
            }
        }
    }
}
