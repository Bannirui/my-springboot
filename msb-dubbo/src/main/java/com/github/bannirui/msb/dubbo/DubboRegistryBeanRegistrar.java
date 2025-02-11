package com.github.bannirui.msb.dubbo;

import com.github.bannirui.msb.env.MsbEnvironmentMgr;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.MapUtils;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.spring.util.PropertySourcesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;

public class DubboRegistryBeanRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {
    private static final Logger log = LoggerFactory.getLogger(DubboRegistryBeanRegistrar.class);
    private ConfigurableEnvironment environment;
    private final String PREFIX = "dubbo.registries";
    private final String ZK_SWITCH_PREFIX = "dubbo.zookeeper.switch";
    private final String SOFA_SWITCH_PREFIX = "dubbo.sofa.switch";
    private final String ZOOKEEPER = "zookeeper";
    private final String SOFA = "sofa";
    private final String IS_ON = "on";

    @Override
    public void setEnvironment(Environment environment) {
        Assert.isInstanceOf(ConfigurableEnvironment.class, environment);
        this.environment = (ConfigurableEnvironment)environment;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Map<String, Object> properties = PropertySourcesUtils.getSubProperties(this.environment.getPropertySources(), "dubbo.registries");
        String zkSwitch = MsbEnvironmentMgr.getProperty(this.environment, "dubbo.zookeeper.switch");
        String sofaSwitch = MsbEnvironmentMgr.getProperty(this.environment, "dubbo.sofa.switch");
        if (!"on".equals(zkSwitch) && !"on".equals(sofaSwitch)) {
            throw new IllegalArgumentException("all registry center switch is off");
        } else if (MapUtils.isEmpty(properties)) {
            if (DubboRegistryBeanRegistrar.log.isDebugEnabled()) {
                DubboRegistryBeanRegistrar.log.debug("There is no property for binding to dubbo config class [" + RegistryConfig.class.getName() + "] within prefix [dubbo.registries]");
            }
        } else {
            Set<String> beanNames = this.resolveMultipleBeanNames(properties);
            for (String beanName : beanNames) {
                if(!"on".equals(sofaSwitch) && "sofa".equals(beanName)) {
                    continue;
                }
                if (!"on".equals(zkSwitch) && "zookeeper".equals(beanName)) {
                    continue;
                }
                BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(RegistryConfig.class);
                AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
                registry.registerBeanDefinition(beanName, beanDefinition);
                this.registerDubboConfigBindingBeanPostProcessor("dubbo.registries", beanName, registry);
            }
        }
    }

    private void registerDubboConfigBindingBeanPostProcessor(String prefix, String beanName, BeanDefinitionRegistry registry) {
        Class<?> processorClass = DubboConfigBindingBeanPostProcessor.class;
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(processorClass);
        String actualPrefix = PropertySourcesUtils.normalizePrefix(prefix) + beanName;
        builder.addConstructorArgValue(actualPrefix).addConstructorArgValue(beanName);
        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
        beanDefinition.setRole(2);
        BeanDefinitionReaderUtils.registerWithGeneratedName(beanDefinition, registry);
        if (DubboRegistryBeanRegistrar.log.isInfoEnabled()) {
            DubboRegistryBeanRegistrar.log.info("The BeanPostProcessor bean definition [{}] for dubbo config bean [name : {}] has been registered.", processorClass.getName(), beanName);
        }
    }

    private Set<String> resolveMultipleBeanNames(Map<String, Object> properties) {
        Set<String> beanNames = new LinkedHashSet<>();
        properties.forEach((propertyName, v) -> {
            int index = propertyName.indexOf(".");
            if (index > 0) {
                String beanName = propertyName.substring(0, index);
                beanNames.add(beanName);
            }
        });
        return beanNames;
    }
}
