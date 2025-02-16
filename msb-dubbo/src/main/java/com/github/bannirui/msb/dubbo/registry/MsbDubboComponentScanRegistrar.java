package com.github.bannirui.msb.dubbo.registry;

import java.lang.reflect.Method;
import java.util.Set;
import org.apache.dubbo.config.spring.beans.factory.annotation.ReferenceAnnotationBeanPostProcessor;
import org.apache.dubbo.config.spring.context.DubboSpringInitializer;
import org.apache.dubbo.config.spring.context.annotation.DubboComponentScanRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;

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
     * 注册{@link org.apache.dubbo.config.spring.beans.factory.annotation.ServiceAnnotationPostProcessor}用于扫描{@link org.apache.dubbo.config.annotation.DubboService}标识的服务提供者
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

    /**
     * 注册{@link ReferenceAnnotationBeanPostProcessor}用于扫描{@link org.apache.dubbo.config.annotation.DubboReference}标识的服务消费者
     */
    public void registerReferenceAnnotationBeanPostProcessor(BeanDefinitionRegistry registry) {
        String beanName = "referenceAnnotationBeanPostProcessor";
        if (!registry.containsBeanDefinition(beanName)) {
            RootBeanDefinition beanDefinition = new RootBeanDefinition(ReferenceAnnotationBeanPostProcessor.class);
            beanDefinition.setRole(2);
            registry.registerBeanDefinition(beanName, beanDefinition);
        }
    }
}
