package com.github.bannirui.msb.config;

import com.ctrip.framework.apollo.spring.config.ConfigPropertySource;
import com.ctrip.framework.apollo.spring.config.ConfigPropertySourceFactory;
import com.ctrip.framework.apollo.spring.util.SpringInjector;
import com.github.bannirui.msb.common.env.EnvironmentMgr;
import com.github.bannirui.msb.config.spring.AutoUpdateConfigChangeListener;
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

public class PropertySourcesProcessor implements BeanDefinitionRegistryPostProcessor, EnvironmentAware, PriorityOrdered {
    private static final String APOLLO_PROPERTY_SOURCE_NAME = "ApolloPropertySources";
    // key是apollo配置namespace对应的优先级
    private static final Multimap<Integer, String> NAMESPACE_NAMES = HashMultimap.create();
    private ConfigurableEnvironment environment;
    private ConfigPropertySourceFactory configPropertySourceFactory =
        (ConfigPropertySourceFactory) SpringInjector.getInstance(ConfigPropertySourceFactory.class);

    public PropertySourcesProcessor() {
    }

    public static boolean addNamespaces(Collection<String> namespaces, int order) {
        return NAMESPACE_NAMES.putAll(order, namespaces);
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        if (Objects.equals("true", EnvironmentMgr.getProperty("autoUpdateInjectedSpringProperties"))) {
            AutoUpdateConfigChangeListener autoUpdateConfigChangeListener =
                new AutoUpdateConfigChangeListener(this.environment, (ConfigurableListableBeanFactory) registry);
            List<ConfigPropertySource> configPropertySources = this.configPropertySourceFactory.getAllConfigPropertySources();
            for (ConfigPropertySource configPropertySource : configPropertySources) {
                // 为apollo的每个配置注册监听器 实现热更新
                configPropertySource.getSource().addChangeListener(autoUpdateConfigChangeListener);
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
