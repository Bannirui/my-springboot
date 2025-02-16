package com.github.bannirui.msb.dubbo;

import com.github.bannirui.msb.dubbo.binder.RelaxedDubboConfigBinder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.dubbo.config.AbstractConfig;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ConsumerConfig;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.ProviderConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.spring.context.config.DubboConfigBeanCustomizer;
import org.apache.dubbo.config.spring.context.config.NamePropertyDefaultValueDubboConfigBeanCustomizer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.Environment;

public class DubboAutoConfiguration implements InitializingBean, ApplicationContextAware, EnvironmentAware {
    private Environment env;
    private ApplicationContext applicationContext;
    private List<DubboConfigBeanCustomizer> configBeanCustomizers;

    @Override
    public void setEnvironment(Environment environment) {
        this.env = environment;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        /**
         * 找到所有类型{@link DubboConfigBeanCustomizer}的Bean
         * <ul>
         *     <li>{@link DubboConfigDefaultCustomizer}</li>
         * </ul>
         */
        Map<String, DubboConfigBeanCustomizer> dubboConfigBeanCustomizerMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(this.applicationContext, DubboConfigBeanCustomizer.class);
        this.configBeanCustomizers = new ArrayList<>(dubboConfigBeanCustomizerMap.values());
        if (this.configBeanCustomizers.stream().noneMatch(NamePropertyDefaultValueDubboConfigBeanCustomizer.class::isInstance)) {
            this.configBeanCustomizers.add(new NamePropertyDefaultValueDubboConfigBeanCustomizer());
        }
        AnnotationAwareOrderComparator.sort(this.configBeanCustomizers);
    }

    private void invokerCustomizers(String beanName, AbstractConfig config) {
        if(CollectionUtils.isEmpty(this.configBeanCustomizers)) return;
        for (DubboConfigBeanCustomizer configBeanCustomizer : this.configBeanCustomizers) {
            configBeanCustomizer.customize(beanName, config);
        }
    }

    @Bean
    public RelaxedDubboConfigBinder relaxedDubboConfigBinder() {
        return new RelaxedDubboConfigBinder();
    }

    @ConditionalOnMissingBean
    @Bean
    public ApplicationConfig applicationConfig() {
        ApplicationConfig config = new ApplicationConfig();
        this.invokerCustomizers("applicationConfig", config);
        return config;
    }

    @ConditionalOnMissingBean
    @Bean
    public RegistryConfig registryConfig() {
        RegistryConfig registryConfig = new RegistryConfig();
        this.invokerCustomizers("registryConfig", registryConfig);
        return registryConfig;
    }

    @ConditionalOnMissingBean
    @Bean
    public ProtocolConfig protocolConfig() {
        ProtocolConfig protocolConfig = new ProtocolConfig();
        this.invokerCustomizers("protocolConfig", protocolConfig);
        return protocolConfig;
    }

    @ConditionalOnMissingBean
    @Bean
    public ProviderConfig providerConfig() {
        ProviderConfig providerConfig = new ProviderConfig();
        this.invokerCustomizers("providerConfig", providerConfig);
        return providerConfig;
    }

    @ConditionalOnMissingBean
    @Bean
    public ConsumerConfig consumerConfig() {
        ConsumerConfig consumerConfig = new ConsumerConfig();
        this.invokerCustomizers("consumerConfig", consumerConfig);
        return consumerConfig;
    }
}
