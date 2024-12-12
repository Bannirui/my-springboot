package com.github.bannirui.msb.config.spring;

import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.property.PlaceholderHelper;
import com.ctrip.framework.apollo.spring.property.SpringValue;
import com.ctrip.framework.apollo.spring.property.SpringValueRegistry;
import com.ctrip.framework.apollo.spring.util.SpringInjector;
import com.google.gson.Gson;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.CollectionUtils;

/**
 * Apollo的观察者 监听配置变更 反射更新Spring内存值.
 * {@link com.ctrip.framework.apollo.spring.property.AutoUpdateConfigChangeListener}
 */
public class AutoUpdateApolloConfigChangeListener implements ConfigChangeListener {
    private static final Logger logger = LoggerFactory.getLogger(AutoUpdateApolloConfigChangeListener.class);
    private final boolean typeConverterHasConvertIfNecessaryWithFieldParameter = this.testTypeConverterHasConvertIfNecessaryWithFieldParameter();
    private final ConfigurableBeanFactory beanFactory;
    private final TypeConverter typeConverter;
    // 解析${}
    private final PlaceholderHelper placeholderHelper;
    /**
     * 缓存配置 持有配置的实例.
     * <ul>
     *     <li>key 配置PropertyName</li>
     *     <li>val 持有该配置的对象实例信息 包括对象 方法 通过反射setter方法注入新值</li>
     * </ul>
     */
    private final SpringValueRegistry springValueRegistry;
    private final Gson gson;

    public AutoUpdateApolloConfigChangeListener(ConfigurableListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        this.typeConverter = this.beanFactory.getTypeConverter();
        this.placeholderHelper = SpringInjector.getInstance(PlaceholderHelper.class);
        this.springValueRegistry = SpringInjector.getInstance(SpringValueRegistry.class);
        this.gson = new Gson();
    }

    /**
     * 针对所有关注配置值的对象实例
     * Apollo配置值更新后反射setter方法注入.
     */
    @Override
    public void onChange(ConfigChangeEvent configChangeEvent) {
        Set<String> keys = configChangeEvent.changedKeys();
        if (CollectionUtils.isEmpty(keys)) return;
        for (String key : keys) {
            // 所有关注该配置的Java对象实例
            Collection<SpringValue> targetVals = this.springValueRegistry.get(this.beanFactory, key);
            if(!CollectionUtils.isEmpty(targetVals)) {
                for (SpringValue val : targetVals) {
                    this.updateSpringValue(val);
                }
            }
        }
    }

    /**
     * Apollo配置值更新后反射setter方法注入.
     */
    private void updateSpringValue(SpringValue springValue) {
        try {
            Object value = this.resolvePropertyValue(springValue);
            springValue.update(value);
            logger.info("Auto update apollo changed value successfully, new value: {}, {}", value, springValue);
        } catch (Throwable e) {
            logger.error("Auto update apollo changed value failed, {}", springValue.toString(), e);
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
