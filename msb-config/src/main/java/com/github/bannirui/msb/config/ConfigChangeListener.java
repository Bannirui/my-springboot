package com.github.bannirui.msb.config;

import com.github.bannirui.msb.common.util.StringUtil;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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

public class ConfigChangeListener implements ApplicationContextAware, EnvironmentAware {
    private Logger logger = LoggerFactory.getLogger(ConfigChangeListener.class);
    private String classResourcePattern = "classpath*:com/github/bannirui/**/*.class";
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
            resources = this.resourcePatternResolver.getResources(this.classResourcePattern);
        } catch (IOException e) {
            this.logger.error("Resource 加载异常 errorMessage=", e);
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

    private Class readResources(Resource resource) {
        try {
            MetadataReader metadataReader = this.metadataReaderFactory.getMetadataReader(resource);
            AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();
            if (annotationMetadata.getAnnotationTypes().contains(ConfigurationProperties.class.getName())) {
                ScannedGenericBeanDefinition sbd = new ScannedGenericBeanDefinition(metadataReader);
                Class<?> aClass = Class.forName(sbd.getBeanClassName());
                if (aClass != null && aClass.getAnnotation(ConfigurationProperties.class) != null) {
                    return aClass;
                }
            }
            return null;
        } catch (Exception var6) {
            return null;
        }
    }

    private void putApolloValue(Class cla) {
        try {
            if (cla != null) {
                Object bean = this.getBean(this.applicationContext, cla);
                if (bean != null) {
                    Class<?> targetClass = AopUtils.getTargetClass(bean);
                    Field[] declaredFields = cla.getDeclaredFields();
                    String prefix = "";
                    ConfigurationProperties configurationProperties = AnnotationUtils.findAnnotation(targetClass, ConfigurationProperties.class);
                    if (configurationProperties != null) {
                        if (StringUtil.isNotEmpty(configurationProperties.value())) {
                            prefix = configurationProperties.value();
                        } else if (StringUtil.isNotEmpty(configurationProperties.prefix())) {
                            prefix = configurationProperties.prefix();
                        }
                    }

                    Field[] fields = declaredFields;
                    for (Field field : fields) {
                        String key = "";
                        if (StringUtil.isNotEmpty(prefix)) {
                            key = prefix + "." + field.getName();
                        } else {
                            key = field.getName();
                        }
                        String methodName = "set" + captureName(field.getName());
                        Method method;
                        try {
                            method = cla.getMethod(methodName, field.getType());
                        } catch (Exception var15) {
                            this.logger.warn("className={} 配置类 propertyName={} 不存在set方法或者set方法不规范,不支持自动配置变更",
                                bean.getClass().getName(), field.getName());
                            method = null;
                        }

                        if (method != null) {
                            ApolloValue apolloValue = new ApolloValue();
                            apolloValue.setMethod(method);
                            apolloValue.setObj(bean);
                            ApolloValue.getApolloValueMap().put(key, apolloValue);
                        }
                    }
                }
            }
        } catch (Exception var16) {
            this.logger.warn("className={} 配置类不支持Apollo,配置自动变更", cla.getName());
        }
    }

    private Object getBean(ApplicationContext applicationContext, Class<?> aClass) {
        try {
            return applicationContext.getBean(aClass);
        } catch (Exception e) {
            return null;
        }
    }

    public static String captureName(String name) {
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        return name;
    }
}
