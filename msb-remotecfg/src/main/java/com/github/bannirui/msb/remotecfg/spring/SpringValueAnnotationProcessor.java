package com.github.bannirui.msb.remotecfg.spring;

import com.alibaba.nacos.common.utils.ConcurrentHashSet;
import com.github.bannirui.msb.remotecfg.MySpringApplicationEventListener;
import com.github.bannirui.msb.remotecfg.spring.bean.BeanWithValueAnnotation;
import com.github.bannirui.msb.remotecfg.spring.bean.MethodValueAnnotationAttr;
import com.github.bannirui.msb.remotecfg.spring.bean.ValueAnnotatedObj;
import com.github.bannirui.msb.remotecfg.spring.bean.ValueAnnotationAttr;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

/**
 * 处理使用了Spring的注解{@link org.springframework.beans.factory.annotation.Value}的Bean.
 * <ul>
 *     <li>成员</li>
 *     <li>方法</li>
 * </ul>
 * 那些要热更新的远程配置在项目中的使用方式是打上了注解@Value.
 * <p>
 * 需要前置去收集哪些BeanClass用了@Value注解 因此当前这个处理器的回调时机一定要在{@link CollectAnnotationValueAttrProcessor}之后.
 * 因此
 * <ul>
 *     <li>派生BeanPostProcessor 是为了在Bean实例化后初始化前把Bean实例跟@Value注解信息绑定 后面反射注入新值要用</li>
 *     <li>派生BeanFactoryPostProcessor 因为{@link CollectAnnotationValueAttrProcessor}的类型是{@link }BeanDefinitionRegistryPostProcessor} 所以要确保在它之后 才能拿到它缓存起来的信息</li>
 *     <li>派生BeanFactoryAware 因为拿缓存的key是BeanDefinitionRegistry</li>
 * </ul>
 * <p>
 * 把Bean实例绑定关联之后还要负责维护
 */
public class SpringValueAnnotationProcessor extends NacosProcessor implements BeanFactoryPostProcessor {

    /**
     * 从{@link CollectAnnotationValueAttrProcessor}取BeanName维度的缓存.
     */
    private Map<String, List<ValueAnnotationAttr>> springValueAnnotation8BeanName;

    private final Set<String> processedBean = new ConcurrentHashSet<>();

    /**
     * 以placeholder为维度缓存.
     * 方便热更新的时候检索
     * 设计成静态成员的原因是多个地方要用 我又不想其他的实例跟这个实例耦合 所以就声明为静态的
     */
    private static final Map<String, ValueAnnotatedObj> PLACEHOLDER_PAIR = new ConcurrentHashMap<>();

    /**
     * 给Spring实例化Bean推断构造方法用.
     */
    public SpringValueAnnotationProcessor() {
    }

    /**
     * BeanDefinition都加载好了 把当时缓存的使用了@Value注解的BeanName拿出来.
     * {@link CollectAnnotationValueAttrProcessor}的回调时机在当前处理器之前 所以可以拿到已经缓存好的数据.
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (beanFactory instanceof BeanDefinitionRegistry beanDefinitionRegistry) {
            this.springValueAnnotation8BeanName =
                CollectAnnotationValueAttrProcessor.getSpringValueAttrMapBaseOnBean8Registry(beanDefinitionRegistry);
        }
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        // 反射拿到Bean所有的成员和方法 回调的方法在下面实现 容器中打上了@Value注解的BeanDefinition都筛选归类好延迟到收到Spring应用启动成功再处理
        super.postProcessBeforeInitialization(bean, beanName);
        return bean;
    }

    /**
     * 过滤出被@Value注解标识的成员 缓存起来.
     *
     * @param field 待考察的成员
     */
    @Override
    protected void processFieldOfClass(String beanName, Object bean, Field field) {
        if (field.isAnnotationPresent(Value.class)) {
            this.doCache(beanName, bean, field);
        }
    }

    /**
     * 过滤出方法形参被@Value修饰的 缓存起来.
     *
     * @param method 待考察的方法
     */
    @Override
    protected void processMethodOfClass(String beanName, Object bean, Method method) {
        Value value = method.getAnnotation(Value.class);
        if (value == null) {
            return;
        }
        // 相当于setter方法
        int cnt = method.getParameterCount();
        if (cnt != 1) {
            return;
        }
        // 理论上setter方法的返回值是void 用户自行承担非标准用法的后果
        Class<?> returnType = method.getReturnType();
        if (!Void.TYPE.equals(returnType)) {
            return;
        }
        this.doCache(beanName, bean, method);
    }

    /**
     * 关联上Bean实例.
     * 并且关联上{@link com.github.bannirui.msb.remotecfg.MySpringApplicationEventListener}中已经缓存好的远程配置中心的数据.
     */
    private void doCache(String beanName, Object bean, Member member) {
        if (processedBean.contains(beanName)) {
            return;
        }
        processedBean.add(beanName);
        Map<String, String> remoteData = MySpringApplicationEventListener.getRemoteData();
        if (this.springValueAnnotation8BeanName.containsKey(beanName)) {
            List<ValueAnnotationAttr> ls = this.springValueAnnotation8BeanName.get(beanName);
            for (ValueAnnotationAttr cur : ls) {
                String placeHolder = cur.getPlaceHolder();
                List<BeanWithValueAnnotation> list = null;
                ValueAnnotatedObj valueAnnotatedObj = PLACEHOLDER_PAIR.get(placeHolder);
                if (valueAnnotatedObj == null || (list = valueAnnotatedObj.getList()) == null) {
                    list = new ArrayList<>();
                }
                BeanWithValueAnnotation ans = null;
                if (member instanceof Field) {
                    ans = new BeanWithValueAnnotation(new WeakReference<>(bean), cur);
                } else if (member instanceof Method method) {
                    MethodValueAnnotationAttr tmp = (MethodValueAnnotationAttr) cur;
                    tmp.setMethod(method);
                    ans = new BeanWithValueAnnotation(new WeakReference<>(bean), tmp);
                }
                if (ans != null) {
                    list.add(ans);
                }
                if (!list.isEmpty()) {
                    PLACEHOLDER_PAIR.put(placeHolder, new ValueAnnotatedObj(remoteData.get(placeHolder), list));
                }
            }
        }
    }

    public static Map<String, ValueAnnotatedObj> getPlaceholderPair() {
        return PLACEHOLDER_PAIR;
    }
}
