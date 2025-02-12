package com.github.bannirui.msb.dubbo;

import com.github.bannirui.msb.dubbo.config.DubboProperties;
import com.github.bannirui.msb.enums.ExceptionEnum;
import com.github.bannirui.msb.env.MsbEnvironmentMgr;
import com.github.bannirui.msb.ex.ErrorCodeException;
import com.github.bannirui.msb.plugin.PluginConfigManager;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.apache.dubbo.config.*;
import org.apache.dubbo.config.spring.context.config.DubboConfigBeanCustomizer;
import org.apache.dubbo.rpc.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import java.util.HashSet;
import java.util.Set;

public class DubboConfigDefaultCustomizer implements DubboConfigBeanCustomizer, InitializingBean, PriorityOrdered, EnvironmentAware {
    private static final Logger logger = LoggerFactory.getLogger(DubboConfigDefaultCustomizer.class);
    private ConfigurableEnvironment env;
    private DubboProperties dubboProperties;
    private String dubboProtocol;
    private Integer dubboPort;
    private Set<String> filterList;

    public void setDubboPort(Integer dubboPort) {
        this.dubboPort = dubboPort;
    }

    public void setDubboProtocol(String dubboProtocol) {
        this.dubboProtocol = dubboProtocol;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.env = (ConfigurableEnvironment)environment;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.initFilter();
        this.dubboProperties = Binder.get(this.env).bind("dubbo", DubboProperties.class).orElse(new DubboProperties());
        String tag = this.env.getProperty("dubbo.consumer.tag");
        DubboProperties.consumerTag = StringUtils.isNotBlank(tag) ? tag : this.dubboProperties.getTag();
        DubboProperties.consumerTagforce = this.dubboProperties.getTagforce();
    }

    @Override
    public int getOrder() {
        return 0;
    }

    public void customize(String beanName, AbstractConfig dubboConfig) {
        if (dubboConfig instanceof ApplicationConfig cfg) {
            this.applicationConfig(cfg);
        }
        if (dubboConfig instanceof ProtocolConfig cfg) {
            this.protocolConfig(cfg);
        }
        if (dubboConfig instanceof RegistryConfig cfg) {
            this.registryConfig(beanName, cfg);
        }
        if (dubboConfig instanceof ProviderConfig cfg) {
            this.providerConfig(cfg);
        }
        if (dubboConfig instanceof ConsumerConfig cfg) {
            this.consumerConfig(cfg);
        }
    }

    private void applicationConfig(ApplicationConfig applicationConfig) {
        applicationConfig.setName(MsbEnvironmentMgr.getAppName());
        if (applicationConfig.getLogger() == null) {
            applicationConfig.setLogger(this.dubboProperties.getLogger());
        }
    }

    private void protocolConfig(ProtocolConfig protocolConfig) {
        if (protocolConfig.getName() == null) {
            if (StringUtils.isNotBlank(this.dubboProperties.getProtocol())) {
                protocolConfig.setName(this.dubboProperties.getProtocol());
            } else {
                protocolConfig.setName(this.dubboProtocol != null ? this.dubboProtocol : "dubbo");
            }
        }
        if (protocolConfig.getPort() == null) {
            if (this.dubboProperties.getPort() != null) {
                protocolConfig.setPort(this.dubboProperties.getPort());
            } else {
                protocolConfig.setPort(this.dubboPort != null ? this.dubboPort : 20880);
            }
        }
        if (protocolConfig.getHost() == null) {
            protocolConfig.setHost(this.dubboProperties.getHost());
        }
    }

    private void registryConfig(String beanName, RegistryConfig registryConfig) {
        if (registryConfig.getProtocol() == null && this.dubboProperties.getRegistryProtocol() != null) {
            registryConfig.setProtocol(this.dubboProperties.getRegistryProtocol());
        }
        if (MapUtils.isNotEmpty(this.dubboProperties.getRegistries()) && this.dubboProperties.getRegistries().get(beanName) != null) {
            registryConfig.setAddress(this.dubboProperties.getRegistries().get(beanName).getAddress());
            registryConfig.setProtocol(this.dubboProperties.getRegistries().get(beanName).getProtocol());
        } else {
            if (!StringUtils.isNotEmpty(this.dubboProperties.getRegistryAddress())) {
                throw new IllegalArgumentException("illegal registry config" + registryConfig.toString());
            }
            registryConfig.setAddress(this.dubboProperties.getRegistryAddress());
        }
        if (registryConfig.isRegister() == null) {
            registryConfig.setRegister(true);
        }
        if (registryConfig.isSubscribe() == null) {
            registryConfig.setSubscribe(true);
        }
    }

    private void providerConfig(ProviderConfig providerConfig) {
        if (providerConfig.getTimeout() == null) {
            providerConfig.setTimeout(this.dubboProperties.getTimeout());
        }
        if (providerConfig.getRetries() == null) {
            providerConfig.setRetries(this.dubboProperties.getRetries());
        }
        if (providerConfig.getDelay() == null) {
            providerConfig.setDelay(this.dubboProperties.getDelay());
        }
        if (StringUtils.isEmpty(providerConfig.getTag()) && StringUtils.isNotBlank(this.dubboProperties.getTag())) {
            providerConfig.setTag(this.dubboProperties.getTag());
        }
        String msbFilter = StringUtils.join(",", this.getProviderFilter());
        String filter = this.appendFiltersStr(providerConfig.getFilter(), msbFilter);
        if (StringUtils.isNotBlank(filter)) {
            providerConfig.setFilter(filter);
        }
    }

    private void consumerConfig(ConsumerConfig consumerConfig) {
        if (consumerConfig.getTimeout() == null) {
            consumerConfig.setTimeout(this.dubboProperties.getTimeout());
        }
        if (consumerConfig.getRetries() == null) {
            consumerConfig.setRetries(this.dubboProperties.getRetries());
        }
        String msbFilter = StringUtils.join(",", this.getConsumerFilter());
        String filter = this.appendFiltersStr(consumerConfig.getFilter(), msbFilter);
        if (StringUtils.isNotBlank(filter)) {
            consumerConfig.setFilter(filter);
        }
    }

    private String appendFiltersStr(String dubboFilter, String msbFilter) {
        String appendFilter = StringUtils.isNotBlank(msbFilter) ? msbFilter : "";
        return StringUtils.isNotBlank(dubboFilter) ? dubboFilter + "," + appendFilter : appendFilter;
    }

    private String buildFilterBeanName(String filteClassName) {
        return filteClassName.substring(filteClassName.lastIndexOf(".") + 1);
    }

    private Set<String> getProviderFilter() {
        Set<String> filter = new HashSet<>();
        String fileName = Filter.class.getName();
        for (String str : this.filterList) {
            if(!str.contains(".all.") && !str.contains(".provider.")) continue;
            String cla = PluginConfigManager.getProperty(fileName, str);
            filter.add(this.buildFilterBeanName(cla));
        }
        return filter;
    }

    private Set<String> getConsumerFilter() {
        Set<String> filter = new HashSet();
        String fileName = Filter.class.getName();
        for (String str : this.filterList) {
            if (!str.contains(".all.") && !str.contains(".consumer.")) continue;
            String cla = PluginConfigManager.getProperty(fileName, str);
            filter.add(this.buildFilterBeanName(cla));
        }
        return filter;
    }

    private void initFilter() {
        Set<String> filterName = PluginConfigManager.getPropertyKeySet(Filter.class.getName());
        this.filterList = (filterName != null ? filterName : new HashSet<>());
        Set<String> classSet = PluginConfigManager.getPropertyValueSet(Filter.class.getName());
        for (String cla : classSet) {
            try {
                String filterBeanName = this.buildFilterBeanName(cla);
                if (!ExtensionLoader.getExtensionLoader(Filter.class).hasExtension(filterBeanName)) {
                    Filter filter = (Filter)Class.forName(cla).newInstance();
                    ExtensionLoader.getExtensionLoader(Filter.class).addExtension(filterBeanName, filter.getClass());
                }
            } catch (Exception e) {
                throw new ErrorCodeException(e, ExceptionEnum.INITIALIZATION_EXCEPTION, "Dubbo插件", cla);
            }
        }
    }
}
