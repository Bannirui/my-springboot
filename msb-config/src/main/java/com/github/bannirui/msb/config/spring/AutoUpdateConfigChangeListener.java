package com.github.bannirui.msb.config.spring;

import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.google.gson.Gson;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

public class AutoUpdateConfigChangeListener implements ConfigChangeListener {
    private static final Logger logger = LoggerFactory.getLogger(AutoUpdateConfigChangeListener.class);
    private final boolean typeConverterHasConvertIfNecessaryWithFieldParameter = this.testTypeConverterHasConvertIfNecessaryWithFieldParameter();
    private final Environment environment;
    private final ConfigurableBeanFactory beanFactory;
    private final TypeConverter typeConverter;
    private final PlaceholderHelper placeholderHelper;
    private final SpringValueRegistry springValueRegistry;
    private final Gson gson;

    public AutoUpdateConfigChangeListener(Environment environment, ConfigurableListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        this.typeConverter = this.beanFactory.getTypeConverter();
        this.environment = environment;
        this.placeholderHelper = (PlaceholderHelper) SpringInjector.getInstance(PlaceholderHelper.class);
        this.springValueRegistry = (SpringValueRegistry) SpringInjector.getInstance(SpringValueRegistry.class);
        this.gson = new Gson();
    }

    @Override
    public void onChange(ConfigChangeEvent configChangeEvent) {
        Set<String> keys = configChangeEvent.changedKeys();
        if (!CollectionUtils.isEmpty(keys)) {
            Iterator var3 = keys.iterator();
            while (true) {
                Collection targetValues;
                do {
                    do {
                        if (!var3.hasNext()) {
                            return;
                        }

                        String key = (String) var3.next();
                        targetValues = this.springValueRegistry.get(this.beanFactory, key);
                    } while (targetValues == null);
                } while (targetValues.isEmpty());
                Iterator var6 = targetValues.iterator();
                while (var6.hasNext()) {
                    SpringValue val = (SpringValue) var6.next();
                    this.updateSpringValue(val);
                }
            }
        }
    }

    private void updateSpringValue(SpringValue springValue) {
        try {
            Object value = this.resolvePropertyValue(springValue);
            springValue.update(value);
            logger.info("Auto update apollo changed value successfully, new value: {}, {}", value, springValue);
        } catch (Throwable var3) {
            Throwable ex = var3;
            logger.error("Auto update apollo changed value failed, {}", springValue.toString(), ex);
        }

    }

    private Object resolvePropertyValue(SpringValue springValue) {
        Object value = this.placeholderHelper.resolvePropertyValue(this.beanFactory, springValue.getBeanName(), springValue.getPlaceholder());
        if (springValue.isJson()) {
            value = this.parseJsonValue((String) value, springValue.getGenericType());
        } else if (springValue.isField()) {
            if (this.typeConverterHasConvertIfNecessaryWithFieldParameter) {
                value = this.typeConverter.convertIfNecessary(value, springValue.getTargetType(), springValue.getField());
            } else {
                value = this.typeConverter.convertIfNecessary(value, springValue.getTargetType());
            }
        } else {
            value = this.typeConverter.convertIfNecessary(value, springValue.getTargetType(), springValue.getMethodParameter());
        }

        return value;
    }

    private Object parseJsonValue(String json, Type targetType) {
        try {
            return this.gson.fromJson(json, targetType);
        } catch (Throwable e) {
            logger.error("Parsing json '{}' to type {} failed!", json, targetType, e);
            throw e;
        }
    }

    private boolean testTypeConverterHasConvertIfNecessaryWithFieldParameter() {
        try {
            TypeConverter.class.getMethod("convertIfNecessary", Object.class, Class.class, Field.class);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }
}
