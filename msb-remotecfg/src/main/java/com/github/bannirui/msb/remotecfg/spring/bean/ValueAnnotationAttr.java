package com.github.bannirui.msb.remotecfg.spring.bean;

/**
 * 记录{@link org.springframework.beans.factory.annotation.Value}的属性信息.
 * 缓存起来 这样就知道哪个Bean实例的什么成员需要关注远程配置 并且远程配置的key是什么
 * 缓存的时候
 * <ul>
 *     <li>key=BeanName</li>
 *     <li>key=SpringValueAttr实例</li>
 * </ul>
 */
public class ValueAnnotationAttr implements ValueAnnotationTarget {

    /**
     * <t>@Value("${placeHolder}")</t>即远程配置中心的key.
     * 从配置角度看 PlaceHolder是唯一的
     */
    protected String placeHolder;

    /**
     * 注解修饰的成员名称.
     * <ul>
     *     <li>要么是类的成员名称</li>
     *     <li>要么是setter方法的形参名称</li>
     * </ul>
     */
    protected String propertyName;

    /**
     * 注解修饰的成员类型.
     */
    protected Class<?> propertyType;

    public ValueAnnotationAttr(String placeHolder, String propertyName, Class<?> propertyType) {
        this.placeHolder = placeHolder;
        this.propertyName = propertyName;
        this.propertyType = propertyType;
    }

    public String getPlaceHolder() {
        return placeHolder;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public Class<?> getPropertyType() {
        return propertyType;
    }

    @Override
    public TargetType getTargetType() {
        return null;
    }
}
