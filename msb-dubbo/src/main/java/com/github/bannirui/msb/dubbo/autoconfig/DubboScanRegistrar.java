package com.github.bannirui.msb.dubbo.autoconfig;

import com.github.bannirui.msb.dubbo.annotation.CustomDubboComponentScan;
import com.github.bannirui.msb.dubbo.annotation.EnableDubbo;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.spring.context.annotation.DubboComponentScanRegistrar;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

public class DubboScanRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {
    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Set<String> packagesToScan = this.getPackagesToScan(importingClassMetadata);
        DubboComponentScanRegistrar dubboComponentScanRegistrar = new DubboComponentScanRegistrar();
        Method registerServiceProcessor = ReflectionUtils.findMethod(DubboComponentScanRegistrar.class, "registerServiceAnnotationBeanPostProcessor", Set.class, BeanDefinitionRegistry.class);
        ReflectionUtils.makeAccessible(registerServiceProcessor);
        ReflectionUtils.invokeMethod(registerServiceProcessor, dubboComponentScanRegistrar, packagesToScan, registry);
        Method registerReferenceProcessor = ReflectionUtils.findMethod(DubboComponentScanRegistrar.class, "registerReferenceAnnotationBeanPostProcessor", BeanDefinitionRegistry.class);
        ReflectionUtils.makeAccessible(registerReferenceProcessor);
        ReflectionUtils.invokeMethod(registerReferenceProcessor, dubboComponentScanRegistrar, new Object[]{registry});
    }

    private Set<String> getPackagesToScan(AnnotationMetadata metadata) {
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(CustomDubboComponentScan.class.getName()));
        String[] basePackages = attributes.getStringArray("basePackages");
        Class<?>[] basePackageClasses = attributes.getClassArray("basePackageClasses");
        String[] value = attributes.getStringArray("value");
        Set<String> packagesToScan = new LinkedHashSet<>(Arrays.asList(value));
        packagesToScan.addAll(Arrays.asList(basePackages));
        for (Class<?> basePackageClass : basePackageClasses) {
            packagesToScan.add(ClassUtils.getPackageName(basePackageClass));
        }
        if (packagesToScan.isEmpty()) {
            String scanPackageName = this.environment.getProperty("msb.dubbo.scanPackageName");
            if (!StringUtils.isNotBlank(scanPackageName)) {
                AnnotationAttributes enableDubboAttributes = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(EnableDubbo.class.getName()));
                if (enableDubboAttributes != null) {
                    scanPackageName = enableDubboAttributes.getString("scanPackageName");
                }
            }
            if (StringUtils.isNotBlank(scanPackageName)) {
                String[] packages = scanPackageName.split("\\s*[,]+\\s*");
                packagesToScan.addAll(Arrays.asList(packages));
            }
        }
        return packagesToScan.isEmpty() ? Collections.singleton(ClassUtils.getPackageName(metadata.getClassName())) : packagesToScan;
    }
}
