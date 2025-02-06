package com.github.bannirui.msb.plugin;

import com.github.bannirui.msb.annotation.MsbPlugin;
import com.github.bannirui.msb.enums.ExceptionEnum;
import com.github.bannirui.msb.ex.ErrorCodeException;
import com.github.bannirui.msb.ex.FrameworkException;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

public class PluginConfigManger {

    // cglib动态代理拦截器
    private static final String dynamic_proxy_interceptor = "classpath*:META-INF/msb/plugin/*";
    /**
     * 缓存拦截器
     * <ul>
     *     <li>key 配置文件名</li>
     *     <li>val 拦截器信息<ul>
     *         <li>k 标识</li>
     *         <li>v 拦截器的全限定路径</li>
     *     </ul></li>
     * </ul>
     */
    private static final Map<String, Properties> CACHE_INTERCEPTOR_BY_FILENAME = new HashMap<>();

    static {
        ResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = patternResolver.getResources("classpath*:/META-INF/msb/plugin/**");
            for (Resource resource : resources) {
                Properties props = new Properties();
                props.load(resource.getInputStream());
                String fileName = resource.getFilename();
                CACHE_INTERCEPTOR_BY_FILENAME.computeIfAbsent(fileName, k -> new Properties()).putAll(props);
            }
        } catch (Exception e) {
            throw new ErrorCodeException(e, ExceptionEnum.INITIALIZATION_EXCEPTION, "msb资源文件");
        }
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
        return CACHE_INTERCEPTOR_BY_FILENAME.get(fileName);
    }

    public static String getProperty(String fileName, String key) {
        Properties properties = getProperty(fileName);
        if (Objects.isNull(properties)) {
            return null;
        }
        return Objects.nonNull(properties.getProperty(key)) ? properties.getProperty(key) : null;
    }

    public static void setProperty(String fileName, String key, String value) {
        Properties properties = getProperty(fileName);
        if (Objects.nonNull(properties) && Objects.nonNull(properties.get(key))) {
            properties.setProperty(key, value);
        }
    }

    /**
     * @param fileName classpath:/META-INF/msb/plugin下拦截器配置文件名
     * @param prefix ${fileName}中配置内容的key前缀 用于模糊搜索所有key
     * @return 配置文件中所有指定前缀的key
     */
    public static Set<String> getPropertyKeySetWithPrefix(String fileName, String prefix) {
        // 配置文件中所有key
        Set<String> allKeySet = getPropertyKeySet(fileName);
        Set<String> result = new HashSet<>();
        if (Objects.nonNull(allKeySet) && !allKeySet.isEmpty()) {
            allKeySet.forEach(propertyKey -> {
                if (propertyKey.startsWith(prefix)) {
                    result.add(propertyKey);
                }
            });
        }
        return result;
    }

    public static List<PluginDecorator<Class<?>>> getOrderedPluginClasses(String fileName, boolean reverse) {
        // 所有拦截器
        Set<String> classNames = getPropertyValueSet(fileName);
        // 拦截器封装
        return getOrderedPluginClass(classNames, reverse);
    }

    /**
     * @param fileName classpath:/META-INF/msb/plugin下的配置文件 用于配置动态代理的拦截器
     * @param prefix classpath:/META-INF/msb/plugin下配置了拦截器 拦截器的property key的前缀 用于模糊匹配到所有的拦截器
     * @param reverse 指定拦截器的回调顺序 <t>TRUE</t>拦截器优先级逆序 默认优先级升序
     * @return 拦截器封装
     */
    public static List<PluginDecorator<Class<?>>> getOrderedPluginClasses(String fileName, String prefix, boolean reverse) {
        // 配置文件中所有指定前缀的key
        Set<String> classNameKeys = getPropertyKeySetWithPrefix(fileName, prefix);
        if (Objects.isNull(classNameKeys) || classNameKeys.isEmpty()) {
            return new ArrayList<>();
        }
        // 拦截器实现的类全限定名
        Set<String> classNames = new HashSet<>();
        for (String className : classNameKeys) {
            classNames.add(getProperty(fileName, className));
        }
        return getOrderedPluginClass(classNames, reverse);
    }

    /**
     * @param classNames 拦截器类路径全限定名
     * @param reverse {@link MsbPlugin}注解指定拦截器有优先级 没有通过该注解标识使用默认值0 该字段用于指定逆序执行拦截器 默认优先级升序
     * @return 拦截器的封装
     */
    public static List<PluginDecorator<Class<?>>> getOrderedPluginClass(Set<String> classNames, boolean reverse) {
        if (Objects.isNull(classNames) || classNames.isEmpty()) {
            return new ArrayList<>();
        }
        List<PluginDecorator<Class<?>>> result = new ArrayList<>();
        for (String className : classNames) {
            String errorClassNames = "";
            try {
                Class<?> clazz = Class.forName(className);
                // 注解标识优先级
                MsbPlugin mp = clazz.getAnnotation(MsbPlugin.class);
                PluginDecorator<Class<?>> pd = null;
                if (Objects.nonNull(mp)) {
                    pd = new PluginDecorator<>(clazz, mp.order());
                } else {
                    pd = new PluginDecorator<>(clazz, 0);
                }
                result.add(pd);
            } catch (Exception e) {
                errorClassNames = errorClassNames + className + ",";
            }
            if (StringUtils.isNotEmpty(errorClassNames)) {
                throw FrameworkException.getInstance("Cannot init {} class", errorClassNames);
            }
        }
        if (!result.isEmpty()) {
            // 默认order升序 即优先级降序 order越小优先级越大
            Collections.sort(result);
            if (reverse) {
                Collections.reverse(result);
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

    public static void insertIntoOrderedList(List<PluginDecorator<Class<?>>> srcList, boolean reverse, PluginDecorator<Class<?>> target) {
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
