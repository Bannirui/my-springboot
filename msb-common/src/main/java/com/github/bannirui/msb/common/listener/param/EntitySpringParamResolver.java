package com.github.bannirui.msb.common.listener.param;

import com.github.bannirui.msb.common.annotation.ConfigEntity;
import com.github.bannirui.msb.common.properties.ConfigChange;
import com.github.bannirui.msb.common.properties.ConfigChangeEntry;
import com.github.bannirui.msb.common.properties.ConfigChangeType;
import com.github.bannirui.msb.common.util.StringUtil;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.env.Environment;

public class EntitySpringParamResolver implements SpringParamResolver {

    private static final Logger logger = LoggerFactory.getLogger(EntitySpringParamResolver.class);

    DefaultConversionService conversionService = new DefaultConversionService();

    public EntitySpringParamResolver() {
    }

    @Override
    public List<String> attentionKeys(Parameter parameter) {
        ConfigEntity configEntity = parameter.getType().getAnnotation(ConfigEntity.class);
        if (Objects.isNull(configEntity)) {
            return null;
        }
        Class<?> paramClass = parameter.getType();
        List<String> result = new ArrayList<>();
        String prefix = null;
        if (StringUtil.isNotEmpty(configEntity.prefix())) {
            prefix = configEntity.prefix();
        } else {
            prefix = StringUtil.toLowerCaseFirstOne(paramClass.getSimpleName());
        }
        prefix = prefix + ".";
        Field[] fields = paramClass.getDeclaredFields();
        for (Field field : fields) {
            result.add(prefix + field.getName());
        }
        return result;
    }

    @Override
    public Object resolveParameter(Parameter parameter, String paramName, ConfigChange configChange, Set<String> changedKeys, Environment environment,
                                   ApplicationContext applicationContext)
        throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return paramName.startsWith("old") ?
            this.buildParamObjectFromOldValue(false, parameter, configChange, changedKeys, environment, applicationContext) :
            this.buildParamObjectFromOldValue(true, parameter, configChange, changedKeys, environment, applicationContext);
    }

    @Override
    public boolean isSupport(Parameter parameter) {
        return Objects.nonNull(parameter.getType());
    }

    private Object buildParamObjectFromOldValue(boolean isNew, Parameter parameter, ConfigChange configChange, Set<String> changedKeys,
                                                Environment environment, ApplicationContext applicationContext)
        throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        ConfigEntity configEntity = parameter.getType().getAnnotation(ConfigEntity.class);
        Class<?> paramClass = parameter.getType();
        String prefix = null;
        if (StringUtil.isNotEmpty(configEntity.prefix())) {
            prefix = configEntity.prefix();
        } else {
            prefix = StringUtil.toLowerCaseFirstOne(paramClass.getSimpleName());
        }
        prefix = prefix + ".";
        Object paramObject = paramClass.newInstance();
        Field[] fields = paramClass.getDeclaredFields();
        for (Field field : fields) {
            // setter method
            Method method = paramClass.getMethod("set" + StringUtils.capitalize(field.getName()), field.getType());
            if (Objects.isNull(method)) {
                logger.warn("@ConfigEntity注解类中的属性{}缺少setter方法 没法赋值", field.getName());
                return null;
            }
            boolean changed = false;
            for (String changedKey : changedKeys) {
                if (Objects.equals(changedKey, prefix + field.getName())) {
                    changed = true;
                    break;
                }
            }
            String strConfigValue = null;
            if (changed) {
                if (isNew) {
                    strConfigValue = this.composeNewConfigValue(configChange.getChangedConfigs().get(prefix + field.getName()));
                } else {
                    strConfigValue = this.composeOldConfigValue(configChange.getChangedConfigs().get(prefix + field.getName()));
                }
            } else {
                strConfigValue = environment.getProperty(prefix + field.getName());
            }
            if (this.conversionService.canConvert(String.class, field.getType())) {
                Object onvertedFieldObject = this.conversionService.convert(strConfigValue, field.getType());
                method.invoke(paramObject, onvertedFieldObject);
            } else {
                logger.warn("@ConfigEntity注解类中的属性{} {}类型不支持String转换 不支持赋值", field.getName(),
                    field.getType().getClass().getSimpleName());
            }
        }
        return paramObject;
    }

    private String composeNewConfigValue(ConfigChangeEntry entry) {
        if (Objects.isNull(entry)) {
            return null;
        }
        String result = null;
        if (Objects.equals(ConfigChangeType.ADD, entry.getConfigChangeType())) {
            result = entry.getNewValue();
        } else if (Objects.equals(ConfigChangeType.UPDATE, entry.getConfigChangeType())) {
            result = entry.getNewValue();
        }
        return result;
    }

    private String composeOldConfigValue(ConfigChangeEntry entry) {
        if (Objects.isNull(entry)) {
            return null;
        }
        String result = null;
        if (Objects.equals(ConfigChangeType.DELETE, entry.getConfigChangeType())) {
            result = entry.getOldValue();
        } else if (Objects.equals(ConfigChangeType.UPDATE, entry.getConfigChangeType())) {
            result = entry.getOldValue();
        }
        return result;
    }
}
