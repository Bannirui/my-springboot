package com.github.bannirui.msb.remotecfg.spring;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.PriorityOrdered;
import org.springframework.util.ReflectionUtils;

/**
 * 目的是为了缓存哪些远程配置需要关注更新回调.
 * 因此优先级尽量低.
 */
public abstract class NacosProcessor implements BeanPostProcessor, PriorityOrdered {



    public NacosProcessor() {
    }

    protected abstract void processFieldOfClass(String beanName, Object bean, Field field);

    /**
     * <p>如下这种使用场景</p>
     * <t>
     * @Value("${test.userName}")
     * public void setUserName(String userName) {
     *     UserService.userName = userName;
     * }
     * </t>
     */
    protected abstract void processMethodOfClass(String beanName, Object bean, Method method);

    /**
     * 优先级给到最低.
     */
    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }

    /**
     * 在所有bean初始化前拿到所有的成员和方法.
     * 比如在这2个方法之前回调
     * <ul>
     *     <li>PropertiesSet</li>
     *     <li>a custom init-method</li>
     * </ul>
     * 至于怎么处理这些成员或者方法交给子类去关注.
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        // bean的beanClass
        Class<?> clazz = bean.getClass();
        // 反射获取类的成员和方法
        List<Field> fields = this.allFieldOfClass(clazz);
        fields.forEach(o -> this.processFieldOfClass(beanName, bean, o));
        List<Method> methods = this.allMethodOfClass(clazz);
        methods.forEach(o -> this.processMethodOfClass(beanName, bean, o));
        return bean;
    }

    // 类的成员
    private List<Field> allFieldOfClass(Class<?> clazz) {
        List<Field> ls = new ArrayList<>();
        ReflectionUtils.doWithFields(clazz, ls::add);
        return ls;
    }

    // 类的方法
    private List<Method> allMethodOfClass(Class<?> clazz) {
        List<Method> ans = new ArrayList<>();
        ReflectionUtils.doWithMethods(clazz, ans::add);
        return ans;
    }
}
