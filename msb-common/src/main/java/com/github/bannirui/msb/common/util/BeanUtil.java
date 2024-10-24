package com.github.bannirui.msb.common.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ReflectionUtils;

public class BeanUtil {
    private BeanUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static void copyPropertiesNew(final Object source, final Object target) {
        BeanUtils.copyProperties(source, target);
    }

    public static void copyFields(Object source, Object target, boolean copyNull, boolean forceCopy, List<String> excludeFields) {
        List<String> excludes = excludeFields == null ? Collections.emptyList() : excludeFields;
        Class<?> targetClass = target.getClass();
        ReflectionUtils.doWithFields(source.getClass(), (field) -> {
            ReflectionUtils.makeAccessible(field);
            Object value = ReflectionUtils.getField(field, source);
            if (value != null || copyNull) {
                Field targetField = ReflectionUtils.findField(targetClass, field.getName(), field.getType());
                if (targetField != null) {
                    ReflectionUtils.makeAccessible(targetField);
                    if (forceCopy) {
                        ReflectionUtils.setField(targetField, target, value);
                    } else {
                        Object targetFieldValue = ReflectionUtils.getField(targetField, target);
                        if (targetFieldValue == null) {
                            ReflectionUtils.setField(targetField, target, value);
                        }
                    }
                }
            }

        }, (field) -> {
            int modifiers = field.getModifiers();
            return !Modifier.isFinal(modifiers) && !excludes.contains(field.getName());
        });
    }

    public static void copyFields(Object source, Object target, boolean copyNull, boolean forceCopy) {
        copyFields(source, target, copyNull, forceCopy, (List) null);
    }

    public static void copyFields(Object source, Object target, List<String> excludeFields) {
        copyFields(source, target, false, true, excludeFields);
    }

    public static void copyFields(Object source, Object target) {
        copyFields(source, target, false, true, (List) null);
    }
}
