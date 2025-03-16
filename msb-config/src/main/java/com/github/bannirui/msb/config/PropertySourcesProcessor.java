package com.github.bannirui.msb.config;

import com.ctrip.framework.apollo.spring.config.ConfigPropertySource;
import com.ctrip.framework.apollo.spring.config.ConfigPropertySourceFactory;
import com.ctrip.framework.apollo.spring.util.SpringInjector;
import com.github.bannirui.msb.config.spring.AutoUpdateApolloConfigChangeListener;
import com.github.bannirui.msb.env.MsbEnvironmentMgr;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

/**
 * 向Apollo配置注册更新事件的监听器{@link AutoUpdateApolloConfigChangeListener} 实现的热更新.
 */
public class PropertySourcesProcessor implements BeanDefinitionRegistryPostProcessor, PriorityOrdered {
    // key是apollo配置namespace对应的优先级
    private static final Multimap<Integer, String> namespace_names = HashMultimap.create();
    private final ConfigPropertySourceFactory configPropertySourceFactory = SpringInjector.getInstance(ConfigPropertySourceFactory.class);
    // -Dapollo.autoUpdateInjectedSpringProperties=true开启热更新特性 Apollo中用的就是这个PropertyName
    private static final String auto_update_injected_spring_properties_option = "apollo.autoUpdateInjectedSpringProperties";

    public static boolean addNamespaces(Collection<String> namespaces, int order) {
        return namespace_names.putAll(order, namespaces);
    }

    /**
     * 为Apollo的所有配置都注册观察者{@link AutoUpdateApolloConfigChangeListener}监听到Apollo服务端有配置更新后更新Spring内存中的值 达到热更新效果
     */
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        if (Objects.equals("false", MsbEnvironmentMgr.getProperty(PropertySourcesProcessor.auto_update_injected_spring_properties_option))) return;
        AutoUpdateApolloConfigChangeListener listener = new AutoUpdateApolloConfigChangeListener((ConfigurableListableBeanFactory) registry);
        List<ConfigPropertySource> configPropertySources = this.configPropertySourceFactory.getAllConfigPropertySources();
        for (ConfigPropertySource configPropertySource : configPropertySources) {
            // 为apollo的每个配置注册监听器 实现热更新
            configPropertySource.getSource().addChangeListener(listener);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
