package com.github.bannirui.msb.dubbo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import org.apache.dubbo.config.spring.beans.factory.annotation.ReferenceAnnotationBeanPostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.util.ReflectionUtils;

public class ZptResolveReferenceAnnotationProcessor implements MergedBeanDefinitionPostProcessor, Ordered, ApplicationContextAware, InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(ReferenceAnnotationBeanPostProcessor.class);
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
        InjectionMetadata injectionMetadata = this.reflectFindInjectionMetadata(beanName, bean.getClass(), (PropertyValues)null);
        Collection<InjectionMetadata.InjectedElement> injectedElements = this.reflectFieldValue(injectionMetadata, "checkedElements");
        if (injectedElements == null) {
            injectedElements = this.reflectFieldValue(injectionMetadata, "injectedElements");
        }

        Iterator var5 = injectedElements.iterator();

        while(var5.hasNext()) {
            InjectedElement injectedElement = (InjectedElement)var5.next();
            Member member = injectedElement.getMember();
            if (member instanceof Field) {
                Field field = (Field)member;
                Reference reference = (Reference)AnnotationUtils.getAnnotation(field, Reference.class);
                Object injectedObject = this.reflectGetInjectedObject(reference, bean, beanName, field.getType(), injectedElement);
                log.debug("Field......{}, {}, {}", new Object[]{field, reference, injectedObject.toString()});
                this.applicationContext.publishEvent(ComponentScanedEvent.build(this.applicationContext, new DubboFieldRefScanedEvent(field, reference, injectedObject)));
            } else if (member instanceof Method) {
                Method method = (Method)member;
                Field pdField = ReflectionUtils.findField(injectedElement.getClass(), "pd");
                ReflectionUtils.makeAccessible(pdField);
                PropertyDescriptor pd = (PropertyDescriptor)ReflectionUtils.getField(pdField, injectedElement);
                Reference reference = (Reference)AnnotationUtils.findAnnotation(method, Reference.class);
                Object injectedObject = this.reflectGetInjectedObject(reference, bean, beanName, pd.getPropertyType(), injectedElement);
                log.debug("Method......{}, {}, {}", new Object[]{method, reference, injectedObject.toString()});
                this.applicationContext.publishEvent(ComponentScanedEvent.build(this.applicationContext, new DubboMethodRefScanedEvent(method, reference, injectedObject)));
            }
        }

        return bean;
    }

    public int getOrder() {
        return 0;
    }

    public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
    }

    private InjectionMetadata reflectFindInjectionMetadata(String beanName, Class<?> clazz, PropertyValues pvs) {
        return (InjectionMetadata)ReflectionUtils.invokeMethod(this.findInjectionMetadata, this.referenceProcessor, new Object[]{beanName, clazz, pvs});
    }

    private Object reflectGetInjectedObject(Annotation annotation, Object bean, String beanName, Class<?> injectedType, InjectedElement injectedElement) {
        return ReflectionUtils.invokeMethod(this.getInjectedObject, this.referenceProcessor, new Object[]{annotation, bean, beanName, injectedType, injectedElement});
    }

    private Collection<InjectionMetadata.InjectedElement> reflectFieldValue(InjectionMetadata injectionMetadata, String fieldName) {
        Field field = ReflectionUtils.findField(injectionMetadata.getClass(), fieldName);
        ReflectionUtils.makeAccessible(field);
        return (Collection)ReflectionUtils.getField(field, injectionMetadata);
    }
}
