package com.github.bannirui.msb.dubbo.postprocessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.dubbo.config.AbstractConfig;
import org.apache.dubbo.config.spring.context.config.DubboConfigBeanCustomizer;
import org.apache.dubbo.config.spring.context.properties.DefaultDubboConfigBinder;
import org.apache.dubbo.config.spring.context.properties.DubboConfigBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

/**
 * 负责从配置中解析dubbo注册中心
 * <ul>
 *     <li>zk</li>
 * </ul>
 */
public class DubboConfigBindingBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware, InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(DubboConfigBindingBeanPostProcessor.class);
    private final String prefix;
    private final String beanName;
    private DubboConfigBinder dubboConfigBinder;
    private ApplicationContext applicationContext;
    private List<DubboConfigBeanCustomizer> configBeanCustomizers = Collections.emptyList();

    /**
     * dubbo注册中心配置前缀
     * 比如配置为dubbo.registries.zookeeper.address
     * @param prefix 配置前缀 比如dubbo.registries
     * @param beanName 配置前缀后面的第一个属性 比如zookeeper
     */
    public DubboConfigBindingBeanPostProcessor(String prefix, String beanName) {
        Assert.notNull(prefix, "The prefix of Configuration Properties must not be null");
        Assert.notNull(beanName, "The name of bean must not be null");
        this.prefix = prefix;
        this.beanName = beanName;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (beanName.equals(this.beanName) && bean instanceof AbstractConfig) {
            AbstractConfig dubboConfig = (AbstractConfig)bean;
            this.bind(this.prefix, dubboConfig);
            this.customize(beanName, dubboConfig);
        }
        return bean;
    }

    private void bind(String prefix, AbstractConfig dubboConfig) {
        this.dubboConfigBinder.bind(prefix, dubboConfig);
        DubboConfigBindingBeanPostProcessor.log.info("The properties of bean [name: {}] have been binding by prefix of configuration properties: {}", this.beanName, prefix);
    }

    /**
     * 用配置文件内容更新dubbo组件配置
     * @param beanName zookeeper
     * @param dubboConfig dubbo.registries的配置
     */
    private void customize(String beanName, AbstractConfig dubboConfig) {
        for (DubboConfigBeanCustomizer customizer : this.configBeanCustomizers) {
            customizer.customize(beanName, dubboConfig);
        }
    }

    public DubboConfigBinder getDubboConfigBinder() {
        return this.dubboConfigBinder;
    }

    public void setDubboConfigBinder(DubboConfigBinder dubboConfigBinder) {
        this.dubboConfigBinder = dubboConfigBinder;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.initDubboConfigBinder();
        this.initConfigBeanCustomizers();
    }

    private void initDubboConfigBinder() {
        if (this.dubboConfigBinder == null) {
            try {
                this.dubboConfigBinder = this.applicationContext.getBean(DubboConfigBinder.class);
            } catch (BeansException e) {
                DubboConfigBindingBeanPostProcessor.log.error("DubboConfigBinder Bean can't be found in ApplicationContext.");
                this.dubboConfigBinder = this.createDubboConfigBinder(this.applicationContext.getEnvironment());
            }
        }
    }

    private void initConfigBeanCustomizers() {
        Collection<DubboConfigBeanCustomizer> configBeanCustomizers = BeanFactoryUtils.beansOfTypeIncludingAncestors(this.applicationContext, DubboConfigBeanCustomizer.class).values();
        this.configBeanCustomizers = new ArrayList<>(configBeanCustomizers);
        AnnotationAwareOrderComparator.sort(this.configBeanCustomizers);
    }

    protected DubboConfigBinder createDubboConfigBinder(Environment environment) {
        DefaultDubboConfigBinder defaultDubboConfigBinder = new DefaultDubboConfigBinder();
        defaultDubboConfigBinder.setEnvironment(environment);
        defaultDubboConfigBinder.setIgnoreUnknownFields(true);
        defaultDubboConfigBinder.setIgnoreInvalidFields(true);
        return defaultDubboConfigBinder;
    }
}
