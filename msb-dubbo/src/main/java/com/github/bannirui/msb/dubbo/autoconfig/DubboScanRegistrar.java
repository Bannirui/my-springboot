package com.github.bannirui.msb.dubbo.autoconfig;

import com.github.bannirui.msb.dubbo.annotation.CustomDubboComponentScan;
import com.github.bannirui.msb.dubbo.annotation.EnableMsbDubbo;
import com.github.bannirui.msb.dubbo.registry.MsbDubboComponentScanRegistrar;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class DubboScanRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {
    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        // dubbo扫描路径
        Set<String> packagesToScan = this.getPackagesToScan(importingClassMetadata);
        /**
         * 扫描到到服务提供者和消费者注册到容器
         * <ul>
         *     <l>{@link org.apache.dubbo.config.annotation.DubboService} 生产者</l>
         *     <l>{@link org.apache.dubbo.config.annotation.DubboReference} 消费者</l>
         * </ul>
         */
        MsbDubboComponentScanRegistrar dubboComponentScanRegistrar = new MsbDubboComponentScanRegistrar();
        // 注册服务提供者
        Method registerServiceProcessor = ReflectionUtils.findMethod(MsbDubboComponentScanRegistrar.class, "registerServiceAnnotationBeanPostProcessor", Set.class, BeanDefinitionRegistry.class);
        ReflectionUtils.makeAccessible(registerServiceProcessor);
        ReflectionUtils.invokeMethod(registerServiceProcessor, dubboComponentScanRegistrar, packagesToScan, registry);
        // 注册服务消费者
        Method registerReferenceProcessor = ReflectionUtils.findMethod(MsbDubboComponentScanRegistrar.class, "registerReferenceAnnotationBeanPostProcessor", BeanDefinitionRegistry.class);
        ReflectionUtils.makeAccessible(registerReferenceProcessor);
        ReflectionUtils.invokeMethod(registerReferenceProcessor, dubboComponentScanRegistrar, registry);
    }

    /**
     * dubbo扫描路径
     * 优先级
     * <ul>
     *     <li>注解显性指定<ul>
     *         <li>{@link EnableMsbDubbo}继承的{@link CustomDubboComponentScan#value()}</li>
     *         <li>{@link EnableMsbDubbo#scanBasePackages()}</li>
     *         <li>{@link EnableMsbDubbo#scanBasePackageClasses()}</li>
     *     </ul></li>
     *     <li>配置文件指定msb.dubbo.scanPackageName</li>
     *     <li>当前{@link EnableMsbDubbo}根路径</li>
     * </ul>
     * 一般将注解{@link EnableMsbDubbo}打在启动类扫描启动类根路径下所有类
     * @return dubbo扫描路径
     */
    private Set<String> getPackagesToScan(AnnotationMetadata metadata) {
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(CustomDubboComponentScan.class.getName()));
        String[] value = attributes.getStringArray("value");
        String[] basePackages = attributes.getStringArray("basePackages");
        Class<?>[] basePackageClasses = attributes.getClassArray("basePackageClasses");
        /**
         * dubbo扫描路径
         * 优先{@link EnableMsbDubbo}注解指定扫描路径
         */
        Set<String> packagesToScan = new LinkedHashSet<>(Arrays.asList(value));
        packagesToScan.addAll(Arrays.asList(basePackages));
        for (Class<?> basePackageClass : basePackageClasses) {
            packagesToScan.add(ClassUtils.getPackageName(basePackageClass));
        }
        // 其次配置文件指定扫描路径
        if (packagesToScan.isEmpty()) {
            String scanPackageName = this.environment.getProperty("msb.dubbo.scanPackageName");
            if (!StringUtils.isNotBlank(scanPackageName)) {
                AnnotationAttributes enableDubboAttributes = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(EnableMsbDubbo.class.getName()));
                if (enableDubboAttributes != null) {
                    scanPackageName = enableDubboAttributes.getString("scanPackageName");
                }
            }
            if (StringUtils.isNotBlank(scanPackageName)) {
                String[] packages = scanPackageName.split("\\s*[,]+\\s*");
                packagesToScan.addAll(Arrays.asList(packages));
            }
        }
        /**
         * 最后{@link EnableMsbDubbo}注解场景启动根路径
         */
        return packagesToScan.isEmpty() ? Collections.singleton(ClassUtils.getPackageName(metadata.getClassName())) : packagesToScan;
    }
}
