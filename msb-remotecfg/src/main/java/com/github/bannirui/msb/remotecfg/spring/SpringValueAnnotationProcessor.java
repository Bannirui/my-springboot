package com.github.bannirui.msb.remotecfg.spring;

import com.alibaba.nacos.common.utils.ConcurrentHashSet;
import com.github.bannirui.msb.remotecfg.executor.HotReplaceMgr;
import com.github.bannirui.msb.remotecfg.spring.bean.BeanWithValueAnnotation;
import com.github.bannirui.msb.remotecfg.spring.bean.MethodValueAnnotationAttr;
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
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.event.SpringApplicationEvent;
import org.springframework.context.ApplicationListener;

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
public class SpringValueAnnotationProcessor extends NacosProcessor implements BeanFactoryPostProcessor, ApplicationListener<SpringApplicationEvent> {

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
    private static final Map<String, List<BeanWithValueAnnotation>> valueAnnotationCache = new ConcurrentHashMap<>();

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
     * Spring应用启动成功了再开始处理远程配置的热更新
     * <ul>
     *     考虑到2个问题
     *     <li>首先 防止应用启动过程中 远程配置发生更新 其实在应用没有启动成功之前这个窗口时期根本不需要关注热更新问题</li>
     *     <li>其次 确保所有Bean实例都已经完成整个Bean生命周期</li>
     * </ul>
     * 怎么管理之前缓存好的关注热更新的列表呢
     * <ul>
     *     <li>列表管理器 定期清楚那些被淘汰的 可能发生过内存紧张回收了弱引用的Bean实例 毕竟热更新不是第一优先级 对内存使用进行让步</li>
     *     <li>热更新回调处理器</li>
     * </ul>
     */
    @Override
    public void onApplicationEvent(SpringApplicationEvent event) {
        if (event instanceof ApplicationStartedEvent e) {
            Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "HotReplace-Thread"))
                .scheduleAtFixedRate(new HotReplaceMgr(), 5L, 5L, TimeUnit.SECONDS);
        }
    }

    // 关联上Bean实例.
    private void doCache(String beanName, Object bean, Member member) {
        if (processedBean.contains(beanName)) {
            return;
        }
        processedBean.add(beanName);
        if (this.springValueAnnotation8BeanName.containsKey(beanName)) {
            List<ValueAnnotationAttr> ls = this.springValueAnnotation8BeanName.get(beanName);
            for (ValueAnnotationAttr cur : ls) {
                String placeHolder = cur.getPlaceHolder();
                List<BeanWithValueAnnotation> list = valueAnnotationCache.get(placeHolder);
                if (list == null) {
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
                    valueAnnotationCache.put(placeHolder, list);
                }
            }
        }
    }

    public static Map<String, List<BeanWithValueAnnotation>> getValueAnnotationCache() {
        return valueAnnotationCache;
    }
}
