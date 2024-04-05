package com.github.bannirui.msb.remotecfg.spring;

import com.alibaba.nacos.common.utils.ConcurrentHashSet;
import com.github.bannirui.msb.remotecfg.spring.bean.FieldValueAnnotationAttr;
import com.github.bannirui.msb.remotecfg.spring.bean.MethodValueAnnotationAttr;
import com.github.bannirui.msb.remotecfg.spring.bean.ValueAnnotationAttr;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.ReflectionUtils;

/**
 * 这个处理器职责是收集哪些BeanClass使用了{@link org.springframework.beans.factory.annotation.Value}注解.
 * 并且缓存好BeanClass被@Value标识的成员.
 * 当前处理器是{@link SpringValueAnnotationProcessor}的前置 因此回调时机要在它之前.
 * 所以当前处理器派生自BeanDefinitionRegistryPostProcessor BeanDefinition加载好就回调.
 */
public class CollectAnnotationValueAttrProcessor implements BeanDefinitionRegistryPostProcessor {

    // 已经处理好的
    private static final Set<BeanDefinitionRegistry> PROCESSED_REGISTRY = new ConcurrentHashSet<>();

    /**
     * 缓存BeanName对应的BeanClass中使用了@Value的地方
     * 内层map放的是
     * <ul>
     *     <li>key=BeanName</li>
     *     <li>val=@Value注解属性</li>
     * </ul>
     */
    private static final Map<BeanDefinitionRegistry, Map<String, List<ValueAnnotationAttr>>> SPRING_VALUE_ANNOTATION_ATTR_8_BEAN_NAME =
        new ConcurrentHashMap<>();

    public static Map<String, List<ValueAnnotationAttr>> getSpringValueAttrMapBaseOnBean8Registry(BeanDefinitionRegistry registry) {
        Map<String, List<ValueAnnotationAttr>> ans = SPRING_VALUE_ANNOTATION_ATTR_8_BEAN_NAME.get(registry);
        return ans == null ? new HashMap<>() : ans;
    }

    /**
     * BeanDefinition都加载到Spring容器后.
     */
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        if (!PROCESSED_REGISTRY.add(registry)) {
            return;
        }
        SPRING_VALUE_ANNOTATION_ATTR_8_BEAN_NAME.computeIfAbsent(registry, k -> new HashMap<>());
        // 拿到Spring容器中注册的所有BeanDefinition
        String[] beanNames = registry.getBeanDefinitionNames();
        Map<String, List<ValueAnnotationAttr>> map8Bean = SPRING_VALUE_ANNOTATION_ATTR_8_BEAN_NAME.get(registry);
        for (String beanName : beanNames) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
            String className = beanDefinition.getBeanClassName();
            if (className == null || className.isBlank()) {
                continue;
            }
            Class<?> clazz = null;
            try {
                clazz = Class.forName(className);
            } catch (Exception ignored) {
            }
            if (clazz == null) {
                continue;
            }
            // 解析@Value(value="${place_holder}")注解中value属性的值
            PropertyPlaceholderHelper propertyPlaceholderHelper = new PropertyPlaceholderHelper("{", "}");
            List<ValueAnnotationAttr> ls8Bean = new ArrayList<>();
            List<FieldValueAnnotationAttr> ls8Bean4Field = this.processField(clazz, propertyPlaceholderHelper);
            if (!ls8Bean4Field.isEmpty()) {
                ls8Bean.addAll(ls8Bean4Field);
            }
            List<MethodValueAnnotationAttr> ls8Bean4Method = this.processMethod(clazz, propertyPlaceholderHelper);
            if (!ls8Bean4Method.isEmpty()) {
                ls8Bean.addAll(ls8Bean4Method);
            }
            if (!ls8Bean.isEmpty()) {
                map8Bean.put(beanName, ls8Bean);
            }
        }
        SPRING_VALUE_ANNOTATION_ATTR_8_BEAN_NAME.put(registry, map8Bean);
    }

    /**
     * BeanDefinition对应的BeanClass的成员 解析出用了@Value注解的
     */
    private List<FieldValueAnnotationAttr> processField(Class<?> clazz, PropertyPlaceholderHelper parser) {
        List<FieldValueAnnotationAttr> ans = new ArrayList<>();
        ReflectionUtils.doWithFields(clazz, field -> {
            if (field.isAnnotationPresent(Value.class)) {
                Value value = field.getDeclaredAnnotation(Value.class);
                String attr = value.value();
                parser.replacePlaceholders(attr, placeholderName -> {
                    FieldValueAnnotationAttr annotationAttr = new FieldValueAnnotationAttr(placeholderName, field.getName(), field.getType(), field);
                    // BeanName维度要缓存
                    ans.add(annotationAttr);
                    return null;
                });
            }
        });
        return ans;
    }

    /**
     * BeanDefinition对应的BeanClass的setter方法 解析出用了@Value注解的
     */
    private List<MethodValueAnnotationAttr> processMethod(Class<?> clazz, PropertyPlaceholderHelper parser) {
        List<MethodValueAnnotationAttr> ans = new ArrayList<>();
        ReflectionUtils.doWithMethods(clazz, method -> {
            if (method.isAnnotationPresent(Value.class) && method.getParameterCount() == 1 && Void.TYPE.equals(method.getReturnType())) {
                Value value = method.getDeclaredAnnotation(Value.class);
                String attr = value.value();
                parser.replacePlaceholders(attr, placeholderName -> {
                    MethodValueAnnotationAttr annotationAttr =
                        new MethodValueAnnotationAttr(placeholderName, method.getName(), method.getParameterTypes()[0], method);
                    ans.add(annotationAttr);
                    return null;
                });
            }
        });
        return ans;
    }
}
