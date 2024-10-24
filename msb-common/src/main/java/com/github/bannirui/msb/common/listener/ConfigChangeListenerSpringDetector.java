package com.github.bannirui.msb.common.listener;

import com.github.bannirui.msb.common.annotation.MsbConfigChangeListener;
import com.github.bannirui.msb.common.env.EnvironmentMgr;
import com.github.bannirui.msb.common.listener.param.SpringParamResolver;
import com.github.bannirui.msb.common.util.StringUtil;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

public class ConfigChangeListenerSpringDetector implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {
    private static final Log logger = LogFactory.getLog(ConfigChangeListenerSpringDetector.class);
    private Environment environment;
    static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";
    private String basePackage = "com.github.bannirui";
    private String resourcePattern = "**/*.class";
    private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
    private MetadataReaderFactory metadataReaderFactory;
    private List<SpringParamResolver> paramResolvers;
    private ParameterNameDiscoverer parameterNameDiscoverer;

    public ConfigChangeListenerSpringDetector() {
        this.metadataReaderFactory = new CachingMetadataReaderFactory(this.resourcePatternResolver);
        this.paramResolvers = ParamResolverDetector.getSpringParamResolverList();
        this.parameterNameDiscoverer = new StandardReflectionParameterNameDiscoverer();
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        this.init();
        try {
            Resource[] resources = this.resourcePatternResolver.getResources(this.getPackageSearchPath(this.basePackage));
            for (Resource resource : resources) {
                if (resource.isReadable()) {
                    MetadataReader metadataReader = this.metadataReaderFactory.getMetadataReader(resource);
                    AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();
                    if (annotationMetadata.getAnnotationTypes().contains(MsbConfigChangeListener.class.getName())) {
                        ScannedGenericBeanDefinition sbd = new ScannedGenericBeanDefinition(metadataReader);
                        Class<?> aClass = null;
                        String className = sbd.getBeanClassName();
                        aClass = Class.forName(sbd.getBeanClassName());
                        if (aClass != null) {
                            MsbConfigChangeListener ccl = aClass.getAnnotation(MsbConfigChangeListener.class);
                            if (ccl != null && ccl.methods() != null && ccl.methods().length > 0) {
                                Object o = aClass.newInstance();
                                BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(aClass);
                                registry.registerBeanDefinition(aClass.getSimpleName(), beanDefinitionBuilder.getBeanDefinition());
                                String[] methods = ccl.methods();
                                for (String methodName : methods) {
                                    Method[] allMethods = aClass.getDeclaredMethods();
                                    int foundNum = 0;
                                    Method targetMethod = null;
                                    for (Method method : allMethods) {
                                        if (method.getName().equals(methodName)) {
                                            ++foundNum;
                                            targetMethod = method;
                                        }
                                    }
                                    if (foundNum == 0) {
                                        logger.error(String.format("事件监听类[%s]中监听方法[%s]没有找到！", aClass.getSimpleName(), methodName));
                                    } else {
                                        if (foundNum > 1) {
                                            logger.error(
                                                String.format("事件监听类[%s]中监听方法[%s]找到多个，请保证方法名唯一性", aClass.getSimpleName(),
                                                    methodName));
                                        }
                                        if (targetMethod != null) {
                                            this.resolveAttentionKeys(o, targetMethod);
                                        }
                                        logger.info(
                                            String.format("扫描到事件监听类[%s]中监听方法[%s]!", aClass.getSimpleName(), targetMethod.getName()));
                                    }
                                }
                            } else {
                                logger.warn("扫描到MsbConfigChangeListener注解，但是没指定method!");
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void init() {
        String configBasePackage = EnvironmentMgr.getProperty("listener.basePackage");
        if (StringUtil.isNotEmpty(configBasePackage)) {
            this.basePackage = configBasePackage;
        }
    }

    private String getPackageSearchPath(String basePackage) {
        String packageSearchPath = "classpath*:" + this.resolveBasePackage(basePackage) + "/" + this.resourcePattern;
        return packageSearchPath;
    }

    protected String resolveBasePackage(String basePackage) {
        return ClassUtils.convertClassNameToResourcePath(this.environment.resolveRequiredPlaceholders(basePackage));
    }

    private void resolveAttentionKeys(Object target, Method method) {
        Parameter[] parameters = method.getParameters();
        if (parameters != null && parameters.length > 0) {
            Set<String> keys = new HashSet();
            boolean isAttentionAll = false;
            for (Parameter parameter : parameters) {
                for (SpringParamResolver paramResolver : this.paramResolvers) {
                    if (paramResolver.isSupport(parameter)) {
                        List<String> tmpKeys = paramResolver.attentionKeys(parameter);
                        if (tmpKeys.size() == 1 && "__all".equals(tmpKeys.get(0))) {
                            isAttentionAll = true;
                        } else {
                            keys.addAll(tmpKeys);
                        }
                    }
                }
            }
            String[] paramNames = this.parameterNameDiscoverer.getParameterNames(method);
            ConfigChangeListenerMetaData metaData = new ConfigChangeListenerMetaData(target, method, paramNames);
            if (isAttentionAll) {
                ConfigChangeListenerContainer.addListenAllKeysListenerMetaData(metaData);
            } else {
                keys.forEach((key) -> {
                    ConfigChangeListenerContainer.addConfigChangeListenerMetaData(key, metaData);
                });
            }
        }

    }
}
