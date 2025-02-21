package com.github.bannirui.msb.http.proxy;

import com.github.bannirui.msb.http.annotation.HttpService;
import com.github.bannirui.msb.http.config.HttpConfigPropertiesProvider;
import com.github.bannirui.msb.http.event.HttpServiceComponentScanedEventData;
import com.github.bannirui.msb.listener.spring.ComponentScanEvent;
import com.github.bannirui.msb.properties.bind.PropertyBinder;
import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

public class HttpScannerConfigurer implements BeanDefinitionRegistryPostProcessor, EnvironmentAware, ApplicationContextAware {
    private static final Log logger = LogFactory.getLog(HttpScannerConfigurer.class);
    private ApplicationContext applicationContext;
    private PropertyBinder propertyBinder;
    static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";
    private Environment environment;
    private String resourcePattern = "**/*.class";
    private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
    private MetadataReaderFactory metadataReaderFactory;

    public HttpScannerConfigurer() {
        this.metadataReaderFactory = new CachingMetadataReaderFactory(this.resourcePatternResolver);
    }

    protected String resolveBasePackage(String basePackage) {
        return ClassUtils.convertClassNameToResourcePath(this.environment.resolveRequiredPlaceholders(basePackage));
    }

    private String getPackageSearchPath(String basePackage) {
        String packageSearchPath = "classpath*:" + this.resolveBasePackage(basePackage) + "/" + this.resourcePattern;
        return packageSearchPath;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        try {
            Resource[] resources = this.resourcePatternResolver.getResources(this.getPackageSearchPath(HttpConfigPropertiesProvider.getHttpConfigProperties().getBasePackage()));
            for (Resource resource : resources) {
                if (!resource.isReadable()) continue;
                MetadataReader metadataReader = this.metadataReaderFactory.getMetadataReader(resource);
                AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();
                if (annotationMetadata.getAnnotationTypes().contains(HttpService.class.getName())) {
                    ScannedGenericBeanDefinition sbd = new ScannedGenericBeanDefinition(metadataReader);
                    Class<?> aClass = Class.forName(sbd.getBeanClassName());
                    if (aClass != null && aClass.getAnnotation(HttpService.class) != null) {
                        HttpFactoryBean bean = new HttpFactoryBean(aClass);
                        configurableListableBeanFactory.registerSingleton(toLowerCaseFirstOne(aClass.getSimpleName()), bean);
                        this.applicationContext.publishEvent(ComponentScanEvent.build(this.applicationContext, new HttpServiceComponentScanedEventData(aClass)));
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public static String toLowerCaseFirstOne(String s) {
        return Character.isLowerCase(s.charAt(0)) ? s : Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
