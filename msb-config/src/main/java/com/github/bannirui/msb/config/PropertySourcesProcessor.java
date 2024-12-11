package com.github.bannirui.msb.config;

import com.ctrip.framework.apollo.spring.config.ConfigPropertySource;
import com.ctrip.framework.apollo.spring.config.ConfigPropertySourceFactory;
import com.ctrip.framework.apollo.spring.util.SpringInjector;
import com.github.bannirui.msb.common.env.EnvironmentMgr;
import com.github.bannirui.msb.config.spring.AutoUpdateApolloConfigChangeListener;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

/**
 * Apollo配置的热更新.
 */
public class PropertySourcesProcessor implements BeanDefinitionRegistryPostProcessor, EnvironmentAware, PriorityOrdered {
    // key是apollo配置namespace对应的优先级
    private static final Multimap<Integer, String> namespace_names = HashMultimap.create();
    private ConfigurableEnvironment environment;
    private final ConfigPropertySourceFactory configPropertySourceFactory = SpringInjector.getInstance(ConfigPropertySourceFactory.class);
    // -DautoUpdateInjectedSpringProperties=true开启
    public static final String AUTO_UPDATE_INJECTED_SPRING_PROPERTIES_OPTION = "autoUpdateInjectedSpringProperties";

    public static boolean addNamespaces(Collection<String> namespaces, int order) {
        return namespace_names.putAll(order, namespaces);
    }

    /**
     * 为Apollo的所有配置都注册观察者{@link AutoUpdateApolloConfigChangeListener}监听到Apollo服务端有配置更新后更新Spring内存中的值 达到热更新效果
     */
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        if (Objects.equals("true", EnvironmentMgr.getProperty(PropertySourcesProcessor.AUTO_UPDATE_INJECTED_SPRING_PROPERTIES_OPTION))) {
            AutoUpdateApolloConfigChangeListener listener = new AutoUpdateApolloConfigChangeListener(this.environment, (ConfigurableListableBeanFactory) registry);
            List<ConfigPropertySource> configPropertySources = this.configPropertySourceFactory.getAllConfigPropertySources();
            for (ConfigPropertySource configPropertySource : configPropertySources) {
                // 为apollo的每个配置注册监听器 实现热更新
                configPropertySource.getSource().addChangeListener(listener);
            }
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
