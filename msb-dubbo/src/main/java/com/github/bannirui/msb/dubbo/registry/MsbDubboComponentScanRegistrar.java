package com.github.bannirui.msb.dubbo.registry;

import org.apache.dubbo.config.spring.beans.factory.annotation.ServiceAnnotationPostProcessor;
import org.apache.dubbo.config.spring.context.DubboSpringInitializer;
import org.apache.dubbo.config.spring.context.annotation.DubboComponentScanRegistrar;
import org.apache.dubbo.config.spring.util.SpringCompatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * dubbo扫描服务提供者和消费者
 * 根据扫描路径找到所有的注解标识的类
 * <ul>
 *     <li>{@link org.apache.dubbo.config.annotation.DubboService}</li>
 *     <li>{@link org.apache.dubbo.config.annotation.DubboReference}</li>
 * </ul>
 */
public class MsbDubboComponentScanRegistrar extends DubboComponentScanRegistrar {

    private static final Logger logger = LoggerFactory.getLogger(MsbDubboComponentScanRegistrar.class);

    /**
     * @param packages 待扫描路径
     */
    public void registerServiceAnnotationBeanPostProcessor(Set<String> packages, BeanDefinitionRegistry registry) {
        DubboSpringInitializer.initialize(registry);
        try {
            Class<?> superClz = this.getClass().getSuperclass();
            Method method = superClz.getDeclaredMethod("registerServiceAnnotationPostProcessor", Set.class, BeanDefinitionRegistry.class);
            method.setAccessible(true);
            method.invoke(this, packages, registry);
        } catch (Exception e) {
            MsbDubboComponentScanRegistrar.logger.error("dubbo扫描服务提供者失败 ", e);
        }
    }
    public void registerReferenceAnnotationBeanPostProcessor(BeanDefinitionRegistry registry) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(ServiceAnnotationPostProcessor.class);
        builder.addConstructorArgValue(packagesToScan);
        builder.setRole(2);
        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
        BeanDefinitionReaderUtils.registerWithGeneratedName(beanDefinition, registry);
    }
}
