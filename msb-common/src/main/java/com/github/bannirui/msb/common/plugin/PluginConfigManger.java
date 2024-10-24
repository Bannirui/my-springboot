package com.github.bannirui.msb.common.plugin;

import com.github.bannirui.msb.common.annotation.MsbPlugin;
import com.github.bannirui.msb.common.enums.ExceptionEnum;
import com.github.bannirui.msb.common.ex.ErrorCodeException;
import com.github.bannirui.msb.common.ex.FrameworkException;
import com.github.bannirui.msb.common.util.StringUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

public class PluginConfigManger {

    private static final String APP_CAT_PROPERTIES_CLASSPATH = "classpath*:META-INF/msb/plugin/**";
    private static final Map<String, Properties> CANT_PROPERTIES_MAP = new HashMap<>();

    static {
        ResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = patternResolver.getResources("classpath*:META-INF/msb/plugin/**");
            for (Resource resource : resources) {
                Properties props = new Properties();
                props.load(resource.getInputStream());
                String fileName = resource.getFilename();
                if (CANT_PROPERTIES_MAP.containsKey(fileName)) {
                    Properties properties = CANT_PROPERTIES_MAP.get(fileName);
                    for (Map.Entry<Object, Object> e : props.entrySet()) {
                        properties.setProperty((String) e.getKey(), (String) e.getValue());
                    }
                } else {
                    CANT_PROPERTIES_MAP.put(fileName, props);
                }
            }
        } catch (Exception e) {
            throw new ErrorCodeException(e, ExceptionEnum.INITIALIZATION_EXCEPTION, new Object[] {"msb资源文件"});
        }
    }

    public PluginConfigManger() {
    }

    public static Set<String> getPropertyValueSet(String fileName) {
        Set<String> setStr = new HashSet<>();
        Properties property = getProperty(fileName);
        if (Objects.nonNull(property)) {
            Set<String> strings = property.stringPropertyNames();
            for (String s : strings) {
                setStr.add(property.get(s).toString());
            }
        }
        return setStr;
    }

    public static Set<String> getPropertyKeySet(String fileName) {
        Properties property = getProperty(fileName);
        return Objects.nonNull(property) ? property.stringPropertyNames() : new HashSet<>();
    }

    public static Properties getProperty(String fileName) {
        return CANT_PROPERTIES_MAP.get(fileName);
    }

    public static String getProperty(String fileName, String key) {
        Properties properties = getProperty(fileName);
        if (Objects.isNull(properties)) {
            return null;
        }
        return Objects.nonNull(properties.getProperty(key)) ? properties.getProperty(key).toString() : null;
    }

    public static void setProperty(String fileName, String key, String value) {
        Properties properties = getProperty(fileName);
        if (Objects.nonNull(properties) && Objects.nonNull(properties.get(key))) {
            properties.setProperty(key, value);
        }
    }

    public static Set<String> getPropertyKeySetWithPrefix(String fileName, String prefix) {
        Set<String> allKeySet = getPropertyKeySet(fileName);
        Set<String> result = new HashSet<>();
        if (Objects.nonNull(allKeySet) && !allKeySet.isEmpty()) {
            allKeySet.forEach(x -> {
                if (x.startsWith(prefix)) {
                    result.add(x);
                }
            });
        }
        return result;
    }

    public static List<PluginDecorator<Class>> getOrderedPluginClasses(String fileName, boolean reverse) {
        Set<String> classNames = getPropertyValueSet(fileName);
        return getOrderedPluginClass(classNames, reverse);
    }

    public static List<PluginDecorator<Class>> getOrderedPluginClasses(String fileName, String prefix, boolean reverse) {
        Set<String> classNameKeys = getPropertyKeySetWithPrefix(fileName, prefix);
        if (Objects.isNull(classNameKeys) || classNameKeys.isEmpty()) {
            return new ArrayList<>();
        }
        Set<String> classNames = new HashSet<>();
        for (String className : classNames) {
            classNames.add(getProperty(fileName, className));
        }
        return getOrderedPluginClass(classNames, reverse);
    }

    public static List<PluginDecorator<Class>> getOrderedPluginClass(Set<String> classNames, boolean reverse) {
        if (Objects.isNull(classNames) || classNames.isEmpty()) {
            return new ArrayList<>();
        }
        List<PluginDecorator<Class>> result = new ArrayList<>();
        for (String className : classNames) {
            String errorClassNames = "";
            try {
                Class<?> clazz = Class.forName(className);
                MsbPlugin mp = clazz.getAnnotation(MsbPlugin.class);
                PluginDecorator pd = null;
                if (Objects.nonNull(mp)) {
                    pd = new PluginDecorator(clazz, mp.order());
                } else {
                    pd = new PluginDecorator(clazz, 0);
                }
                result.add(pd);
            } catch (Exception e) {
                errorClassNames = errorClassNames + className + ",";
            }
            if (StringUtil.isNotEmpty(errorClassNames)) {
                throw FrameworkException.getInstance("Cannot init {} class", new Object[] {errorClassNames});
            }
        }
        if (!result.isEmpty()) {
            if (reverse) {
                Collections.sort(result);
                Collections.reverse(result);
            } else {
                Collections.sort(result);
            }
        }
        return result;
    }

    public static String getKeyByValue(String fileName, String value) {
        Properties properties = getProperty(fileName);
        Enumeration<Object> keys = properties.keys();
        Object key = null;
        do {
            if (!keys.hasMoreElements()) {
                return null;
            }
            key = keys.nextElement();
        } while (!Objects.equals(properties.get(key), value));
        return (String) key;
    }

    public static void insertIntoOrderedList(List<PluginDecorator<Class>> srcList, boolean reverse, PluginDecorator<Class> target) {
        int pos = 0;
        if (reverse) {
            for (PluginDecorator pd : srcList) {
                if (pd.getOrder() <= target.getOrder()) {
                    break;
                }
            }
        } else {
            for (PluginDecorator pd : srcList) {
                if (pd.getOrder() >= target.getOrder()) {
                    break;
                }
            }
        }
        srcList.add(pos, target);
    }
}
