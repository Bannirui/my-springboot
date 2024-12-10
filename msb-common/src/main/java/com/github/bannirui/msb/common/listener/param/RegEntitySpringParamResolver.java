package com.github.bannirui.msb.common.listener.param;

import com.github.bannirui.msb.common.annotation.ConfigEntity;
import com.github.bannirui.msb.common.properties.ConfigChange;
import com.github.bannirui.msb.common.util.StringUtil;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.env.Environment;

public class RegEntitySpringParamResolver implements SpringParamResolver {

    private static final Logger logger = LoggerFactory.getLogger(RegEntitySpringParamResolver.class);
    DefaultConversionService conversionService = new DefaultConversionService();

    @Override
    public List<String> attentionKeys(Parameter parameter) {
        List<String> result = new ArrayList<>();
        ParameterizedType paramGenericType = null;
        try {
            paramGenericType = (ParameterizedType) parameter.getParameterizedType();
            Class<?> clz = Class.forName(paramGenericType.getActualTypeArguments()[1].getTypeName());
            ConfigEntity configEntity = clz.getAnnotation(ConfigEntity.class);
            String regExp = configEntity.regexp() + ".";
            Field[] fields = clz.getDeclaredFields();
            for (Field field : fields) {
                result.add(regExp + field.getName());
            }
        } catch (Exception e) {
            logger.error("RegEntitySpringParamResolver build attentionKeys for param[objectType=Map<{}, {}>] error",
                Objects.nonNull(paramGenericType) ? paramGenericType.getActualTypeArguments()[0].getTypeName() : "Unknown",
                Objects.nonNull(paramGenericType) ? paramGenericType.getActualTypeArguments()[1].getTypeName() : "Unknown");
        }
        return result;
    }

    @Override
    public Object resolveParameter(Parameter parameter, String paramName, ConfigChange configChange, Set<String> changedKeys, Environment environment,
                                   ApplicationContext applicationContext)
        throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return paramName.startsWith("old") ? this.buildParamObject(false, parameter, paramName, configChange, changedKeys, environment) :
            this.buildParamObject(true, parameter, paramName, configChange, changedKeys, environment);
    }

    private Object buildParamObject(boolean isNew, Parameter parameter, String paramName, ConfigChange configChange, Set<String> changedKeys,
                                    Environment environment) {
        Map<Object, Object> result = new HashMap<>();
        ParameterizedType paramGenericType = null;
        try {
            paramGenericType = (ParameterizedType) parameter.getParameterizedType();
            Class<?> keyClass = Class.forName(paramGenericType.getActualTypeArguments()[0].getTypeName());
            Class<?> entityClass = Class.forName(paramGenericType.getActualTypeArguments()[1].getTypeName());
            ConfigEntity configEntity = entityClass.getAnnotation(ConfigEntity.class);
            String regExp = configEntity.regexp() + ".";
            Field[] entityFields = entityClass.getDeclaredFields();
            for (Field field : entityFields) {
                Pattern p = Pattern.compile(regExp + field.getName());
                for (String changedKey : changedKeys) {
                    Matcher m = p.matcher(changedKey);
                    if (m.find() && m.groupCount() == 1) {
                        String strConfigIndex = m.group(1);
                        Object key = this.conversionService.convert(strConfigIndex, keyClass);
                        Object entity = result.get(key);
                        if (Objects.isNull(entity)) {
                            entity = entityClass.newInstance();
                            result.put(key, entity);
                        }
                    }
                }
            }
            for (Object key : result.keySet()) {
                Object entity = result.get(key);
                for (Field field : entityFields) {
                    Method setMethod = entityClass.getMethod("set" + field.getName(), field.getType());
                    if (Objects.isNull(setMethod)) {
                        logger.warn("@ConfigEntity注解类中的属性{}缺少set方法 无法赋值", field.getName());
                        return null;
                    }
                    boolean isChanged = false;
                    Pattern p = Pattern.compile(regExp + field.getName());
                    String recordChangedKey = null;
                    for (String changedKey : changedKeys) {
                        Matcher m = p.matcher(changedKey);
                        if (m.find() && m.groupCount() == 1) {
                            String strConfigIndex = m.group(1);
                            Object targetKey = this.conversionService.convert(strConfigIndex, keyClass);
                            if (Objects.equals(targetKey, key)) {
                                isChanged = true;
                                recordChangedKey = changedKey;
                                break;
                            }
                        }
                    }
                    String strConfigValue = null;
                    if (isChanged) {
                        if (isNew) {
                            strConfigValue = configChange.getChangedConfigs().get(recordChangedKey).getNewValue();
                        } else {
                            strConfigValue = configChange.getChangedConfigs().get(recordChangedKey).getOldValue();
                        }
                    } else {
                        String changedKey = (regExp + field.getName()).replaceAll("[(].*[)]", String.valueOf(key)).replaceAll("\\\\", "");
                        strConfigValue = environment.getProperty(changedKey);
                    }
                    if (this.conversionService.canConvert(String.class, field.getType())) {
                        Object convertedFieldObject = this.conversionService.convert(strConfigValue, field.getType());
                        setMethod.invoke(entity, convertedFieldObject);
                    } else {
                        logger.warn("@ConfigEntity注解类中的属性{} {} 不支持String转化 无法赋值", field.getName(),
                            field.getType().getClass().getSimpleName());
                    }

                }
            }
        } catch (Exception e) {
            logger.error("RegEntitySpringParamResolver process param[objectType=Map<{}, {}>] error]",
                Objects.nonNull(paramGenericType) ? paramGenericType.getActualTypeArguments()[0].getTypeName() : "Unknown",
                Objects.nonNull(paramGenericType) ? paramGenericType.getActualTypeArguments()[1].getTypeName() : "Unknown");
        }
        return result;
    }

    @Override
    public boolean isSupport(Parameter parameter) {
        try {
            if (parameter.getParameterizedType() instanceof ParameterizedType paramGenericType) {
                if (paramGenericType.getActualTypeArguments().length != 2) {
                    return false;
                }
                if (!this.conversionService.canConvert(String.class, Class.forName(paramGenericType.getActualTypeArguments()[0].getTypeName()))) {
                    return false;
                }
                ConfigEntity configEntity =
                    Class.forName(paramGenericType.getActualTypeArguments()[1].getTypeName()).getAnnotation(ConfigEntity.class);
                if (Objects.nonNull(configEntity) && StringUtil.isNotEmpty(configEntity.regexp())) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
