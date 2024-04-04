package com.github.bannirui.msb.remotecfg.spring.bean;

import java.lang.ref.WeakReference;

/**
 * 打上了{@link org.springframework.beans.factory.annotation.Value}注解的Bean.
 * 用了@Value注解的Bean 用BeanName标识 从Spring角度看 BeanName是唯一的.
 * <ul>
 *     <li>@Value注解可能打在了Bean的成员上</li>
 *     <li>@Value注解可能打在了Bean的方法的形参上</li>
 * </ul>
 * 一个Bean可能用了多个@Value注解
 */
public class BeanWithValueAnnotation {

    /**
     * Bean的引用.
     * 将来修改Bean实例的field的值要用反射.
     * 所以最好是持有Bean实例的引用.
     * 弱引用是为了内存使用友好放弃一些特性支持 将来轮询到该属性为null了就进行清理.
     * {@link com.github.bannirui.msb.remotecfg.executor.HotReplaceMgr}
     */
    private WeakReference<Object> bean;

    private ValueAnnotationAttr annotationAttr;

    public BeanWithValueAnnotation(WeakReference<Object> bean, ValueAnnotationAttr annotationAttr) {
        this.bean = bean;
        this.annotationAttr = annotationAttr;
    }

    public WeakReference<Object> getBean() {
        return bean;
    }

    public ValueAnnotationAttr getAnnotationAttr() {
        return annotationAttr;
    }
}
