package com.github.bannirui.msb.config;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

/**
 * {@link ConfigurationProperties}注解标识的类中的成员作为配置key缓存起来.
 * 通过{@link org.springframework.context.annotation.Import}注入到Spring中
 * 监听Apollo更新事件
 */
public class ConfigChangeListener implements ApplicationContextAware, EnvironmentAware {
    private static final Logger logger = LoggerFactory.getLogger(ConfigChangeListener.class);
    /**
     * 要扫描的所有class文件.
     */
    private static final String class_resource_pattern = "classpath*:com/github/bannirui/**/*.class";
    private Environment environment;
    private ApplicationContext applicationContext;
    private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
    private MetadataReaderFactory metadataReaderFactory;

    public ConfigChangeListener() {
        this.metadataReaderFactory = new CachingMetadataReaderFactory(this.resourcePatternResolver);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        Resource[] resources = new Resource[0];
        try {
            resources = this.resourcePatternResolver.getResources(ConfigChangeListener.class_resource_pattern);
        } catch (IOException e) {
            ConfigChangeListener.logger.error("Resource加载异常 err=", e);
        }
        for (Resource resource : resources) {
            if (resource.isReadable()) {
                this.putApolloValue(this.readResources(resource));
            }
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * 找到被{@link ConfigurationProperties}注解的类
     * @param resource class文件
     * @return 没有被注解返回null
     */
    private Class<?> readResources(Resource resource) {
        try {
            MetadataReader metadataReader = this.metadataReaderFactory.getMetadataReader(resource);
            AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();
            if (annotationMetadata.getAnnotationTypes().contains(ConfigurationProperties.class.getName())) {
                ScannedGenericBeanDefinition sbd = new ScannedGenericBeanDefinition(metadataReader);
                Class<?> clz = Class.forName(sbd.getBeanClassName());
                if (clz.getAnnotation(ConfigurationProperties.class) != null) {
                    return clz;
                }
            }
            return null;
        } catch (Exception var6) {
            return null;
        }
    }

    /**
     * 被{@link ConfigurationProperties}注解的类的setter方法全部缓存到集合中
     * @param clz 被{@link ConfigurationProperties}注解的类
     */
    private void putApolloValue(Class<?> clz) {
        try {
            if (clz != null) {
                // 被{@link ConfigurationProperties}注解的Bean实例
                Object bean = this.getBean(this.applicationContext, clz);
                if (bean != null) {
                    Class<?> targetClass = AopUtils.getTargetClass(bean);
                    Field[] declaredFields = clz.getDeclaredFields();
                    String prefix = "";
                    ConfigurationProperties configurationProperties = AnnotationUtils.findAnnotation(targetClass, ConfigurationProperties.class);
                    if (configurationProperties != null) {
                        if (StringUtils.isNotEmpty(configurationProperties.value())) {
                            prefix = configurationProperties.value();
                        } else if (StringUtils.isNotEmpty(configurationProperties.prefix())) {
                            prefix = configurationProperties.prefix();
                        }
                    }
                    for (Field field : declaredFields) {
                        String key = "";
                        if (StringUtils.isNotEmpty(prefix)) {
                            key = prefix + "." + field.getName();
                        } else {
                            key = field.getName();
                        }
                        // setter方法名
                        String methodName = "set" + captureName(field.getName());
                        Method method = null;
                        try {
                            method = clz.getMethod(methodName, field.getType());
                        } catch (Exception var15) {
                            ConfigChangeListener.logger.warn("className={} 配置类 propertyName={} 不存在set方法或者set方法不规范,不支持自动配置变更",
                                bean.getClass().getName(), field.getName());
                        }
                        if (method != null) {
                            ApolloValue apolloValue = new ApolloValue();
                            apolloValue.setMethod(method);
                            apolloValue.setObj(bean);
                            // cache
                            ApolloValue.getApolloValueMap().put(key, apolloValue);
                        }
                    }
                }
            }
        } catch (Exception e) {
            ConfigChangeListener.logger.warn("className={} 配置类不支持Apollo,配置自动变更", clz.getName());
        }
    }

    private Object getBean(ApplicationContext applicationContext, Class<?> aClass) {
        try {
            return applicationContext.getBean(aClass);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 成员名称首字母大写 用于拼接到方法中的驼峰命名.
     * @param name 成员名 如myName
     * @return 如MyName
     */
    public static String captureName(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}
