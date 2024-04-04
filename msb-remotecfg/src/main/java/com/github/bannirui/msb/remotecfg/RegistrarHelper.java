package com.github.bannirui.msb.remotecfg;

import com.github.bannirui.msb.remotecfg.annotation.EnableMyRemoteCfg;
import com.github.bannirui.msb.remotecfg.spring.CollectAnnotationValueAttrProcessor;
import com.github.bannirui.msb.remotecfg.spring.SpringValueAnnotationProcessor;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 向Spring批量导入BeanDefinition.
 * 时机在Spring加载好BeanDefinition之后.
 * 根据{@link EnableMyRemoteCfg}方法决定是否开启热更新支持.
 */
public class RegistrarHelper implements ImportBeanDefinitionRegistrar {

    public RegistrarHelper() {
    }

    /**
     * 要拿到{@link EnableMyRemoteCfg}注解方法的返回值.
     * 决定向Spring注册自定义的Bean来处理远程远程配置中心的回调.
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Map<String, Object> annotationAttributes = importingClassMetadata.getAnnotationAttributes(EnableMyRemoteCfg.class.getName());
        if (annotationAttributes == null || annotationAttributes.isEmpty()) {
            return;
        }
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(annotationAttributes);
        // 是否需要开启远程配置中心热更新支持
        boolean hotReplace = attributes.getBoolean(EnableMyRemoteCfg.AttrName.Hot_REPLACE);
        if (!hotReplace) {
            return;
        }
        // 收集使用了@Value注解的BeanDefinition
        this.registerBeanDefinitionIfAbsent(registry, CollectAnnotationValueAttrProcessor.class.getName(), CollectAnnotationValueAttrProcessor.class);
        // 匹配使用了@Value的对应的Bean实例
        this.registerBeanDefinitionIfAbsent(registry, SpringValueAnnotationProcessor.class.getName(), SpringValueAnnotationProcessor.class);
    }

    private void registerBeanDefinitionIfAbsent(BeanDefinitionRegistry registry, String beanName, Class<?> clazz) {
        if (registry.containsBeanDefinition(beanName)) {
            return;
        }
        String[] names = registry.getBeanDefinitionNames();
        for (String name : names) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(name);
            if (Objects.equals(beanDefinition.getBeanClassName(), clazz.getName())) {
                return;
            }
        }
        BeanDefinition ans = BeanDefinitionBuilder.genericBeanDefinition(clazz).getBeanDefinition();
        registry.registerBeanDefinition(beanName, ans);
    }
}
