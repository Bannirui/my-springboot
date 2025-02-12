package com.github.bannirui.msb.dubbo;

import com.github.bannirui.msb.dubbo.envent.DubboFieldRefScanedEvent;
import com.github.bannirui.msb.dubbo.envent.DubboMethodRefScanedEvent;
import com.github.bannirui.msb.listener.spring.ComponentScanEvent;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.spring.beans.factory.annotation.ReferenceAnnotationBeanPostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collection;

public class ResolveReferenceAnnotationProcessor implements MergedBeanDefinitionPostProcessor, Ordered, ApplicationContextAware, InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(ResolveReferenceAnnotationProcessor.class);
    /**
     * 负责解析{@link org.apache.dubbo.config.annotation.DubboReference}注解标识的类注入到容器
     */
    private ReferenceAnnotationBeanPostProcessor referenceProcessor;
    private Method findInjectionMetadata;
    private Method getInjectedObject;
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        this.referenceProcessor = applicationContext.getBean("referenceAnnotationBeanPostProcessor", ReferenceAnnotationBeanPostProcessor.class);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.findInjectionMetadata = ReflectionUtils.findMethod(ReferenceAnnotationBeanPostProcessor.class, "findInjectionMetadata", String.class, Class.class, PropertyValues.class);
        ReflectionUtils.makeAccessible(this.findInjectionMetadata);
        this.getInjectedObject = ReflectionUtils.findMethod(ReferenceAnnotationBeanPostProcessor.class, "getInjectedObject", Annotation.class, Object.class, String.class, Class.class, InjectionMetadata.InjectedElement.class);
        ReflectionUtils.makeAccessible(this.getInjectedObject);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        InjectionMetadata injectionMetadata = this.reflectFindInjectionMetadata(beanName, bean.getClass(), null);
        Collection<InjectionMetadata.InjectedElement> injectedElements = this.reflectFieldValue(injectionMetadata, "checkedElements");
        if (injectedElements == null) {
            injectedElements = this.reflectFieldValue(injectionMetadata, "injectedElements");
        }
        for (InjectionMetadata.InjectedElement injectedElement : injectedElements) {
            Member member = injectedElement.getMember();
            if (member instanceof Field field) {
                Reference reference = AnnotationUtils.getAnnotation(field, Reference.class);
                Object injectedObject = this.reflectGetInjectedObject(reference, bean, beanName, field.getType(), injectedElement);
                log.debug("Field......{}, {}, {}", field, reference, injectedObject.toString());
                this.applicationContext.publishEvent(ComponentScanEvent.build(this.applicationContext, new DubboFieldRefScanedEvent(field, reference, injectedObject)));
            } else if (member instanceof Method method) {
                Field pdField = ReflectionUtils.findField(injectedElement.getClass(), "pd");
                ReflectionUtils.makeAccessible(pdField);
                PropertyDescriptor pd = (PropertyDescriptor)ReflectionUtils.getField(pdField, injectedElement);
                Reference reference = AnnotationUtils.findAnnotation(method, Reference.class);
                Object injectedObject = this.reflectGetInjectedObject(reference, bean, beanName, pd.getPropertyType(), injectedElement);
                log.debug("Method......{}, {}, {}", method, reference, injectedObject.toString());
                this.applicationContext.publishEvent(ComponentScanEvent.build(this.applicationContext, new DubboMethodRefScanedEvent(method, reference, injectedObject)));
            }
        }
        return bean;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
    }

    private InjectionMetadata reflectFindInjectionMetadata(String beanName, Class<?> clazz, PropertyValues pvs) {
        return (InjectionMetadata)ReflectionUtils.invokeMethod(this.findInjectionMetadata, this.referenceProcessor, new Object[]{beanName, clazz, pvs});
    }

    private Object reflectGetInjectedObject(Annotation annotation, Object bean, String beanName, Class<?> injectedType, InjectionMetadata.InjectedElement injectedElement) {
        return ReflectionUtils.invokeMethod(this.getInjectedObject, this.referenceProcessor, annotation, bean, beanName, injectedType, injectedElement);
    }

    private Collection<InjectionMetadata.InjectedElement> reflectFieldValue(InjectionMetadata injectionMetadata, String fieldName) {
        Field field = ReflectionUtils.findField(injectionMetadata.getClass(), fieldName);
        ReflectionUtils.makeAccessible(field);
        return (Collection)ReflectionUtils.getField(field, injectionMetadata);
    }
}
