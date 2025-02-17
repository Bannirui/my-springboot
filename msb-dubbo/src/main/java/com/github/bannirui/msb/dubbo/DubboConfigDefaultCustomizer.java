package com.github.bannirui.msb.dubbo;

import com.github.bannirui.msb.dubbo.config.DubboProperties;
import com.github.bannirui.msb.enums.ExceptionEnum;
import com.github.bannirui.msb.env.MsbEnvironmentMgr;
import com.github.bannirui.msb.ex.ErrorCodeException;
import com.github.bannirui.msb.plugin.Interceptor;
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

/**
 * 用配置文件内容更新dubbo组件的配置
 */
public class DubboConfigDefaultCustomizer implements DubboConfigBeanCustomizer, InitializingBean, PriorityOrdered, EnvironmentAware {
    private static final Logger logger = LoggerFactory.getLogger(DubboConfigDefaultCustomizer.class);
    private ConfigurableEnvironment env;
    private DubboProperties dubboProperties;
    private String dubboProtocol;
    private Integer dubboPort;
    /**
     * classpath*:META-INF/msb/plugin/org.apache.dubbo.rpc.Filter文件中配置的拦截器的key
     */
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

    /**
     * 缓存配置文件中dubbo前缀的配置
     */
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

    /**
     * 根据配置文件定义更新dubbo配置
     * @param beanName        the name of {@link AbstractConfig Dubbo Config Bean}
     */
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

    /**
     * dubbo.registries
     * 根据配置文件补充dubbo注册中心配置
     * @param beanName dubbo注册中心名字 比如zookeeper
     * @param registryConfig dubbo注册中心配置
     */
    private void registryConfig(String beanName, RegistryConfig registryConfig) {
        if (registryConfig.getProtocol() == null && this.dubboProperties.getRegistryProtocol() != null) {
            registryConfig.setProtocol(this.dubboProperties.getRegistryProtocol());
        }
        if (MapUtils.isNotEmpty(this.dubboProperties.getRegistries()) && this.dubboProperties.getRegistries().get(beanName) != null) {
            registryConfig.setAddress(this.dubboProperties.getRegistries().get(beanName).getAddress());
            registryConfig.setProtocol(this.dubboProperties.getRegistries().get(beanName).getProtocol());
        } else {
            if (!StringUtils.isNotEmpty(this.dubboProperties.getRegistryAddress())) {
                throw new IllegalArgumentException("illegal registry config" + registryConfig);
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

    /**
     * 服务生产者配置
     */
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
        String msbFilter = StringUtils.join(this.getProviderFilter(), ",");
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
        String msbFilter = StringUtils.join(this.getConsumerFilter(), ",");
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

    /**
     * classpath*:META-INF/msb/plugin/org.apache.dubbo.rpc.Filter文件中配置的dubbo服务提供方拦截器
     * @return dubbo服务提供方拦截器实现全限定路径名
     */
    private Set<String> getProviderFilter() {
        Set<String> filter = new HashSet<>();
        for (String str : this.filterList) {
            if(!str.contains(".all.") && !str.contains(".provider.")) continue;
            String cla = PluginConfigManager.getProperty(Filter.class.getName(), str);
            filter.add(this.buildFilterBeanName(cla));
        }
        return filter;
    }

    /**
     * classpath*:META-INF/msb/plugin/org.apache.dubbo.rpc.Filter文件中配置的dubbo服务消费方拦截器
     * @return dubbo服务消费方拦截器实现全限定路径名
     */
    private Set<String> getConsumerFilter() {
        Set<String> filter = new HashSet<>();
        for (String str : this.filterList) {
            if (!str.contains(".all.") && !str.contains(".consumer.")) continue;
            String cla = PluginConfigManager.getProperty(Filter.class.getName(), str);
            filter.add(this.buildFilterBeanName(cla));
        }
        return filter;
    }

    /**
     * 将classpath*:META-INF/msb/plugin/org.apache.dubbo.rpc.Filter文件中配置的拦截器的key缓存到{@link DubboConfigDefaultCustomizer#filterList}
     */
    private void initFilter() {
        /**
         * msb拦截器
         * 自定义{@link Interceptor}实现并用注解{@link com.github.bannirui.msb.annotation.MsbPlugin}标识 放在classpath:resources/META-INF/msb/plugin下的文件中
         * <ul>
         *     <li>文件名是{@link Interceptor}的全限定路径名</li>
         *     <li>文件内容键值对
         *       <ul>
         *           <li>key 给拦截器命名</li>
         *           <li>val 自定义拦截器实现的全限定路径名</li>
         *       </ul>
         *     </li>
         * </ul>
         */
        Set<String> filterName = PluginConfigManager.getPropertyKeySet(Filter.class.getName());
        this.filterList = (filterName != null ? filterName : new HashSet<>());
        // classpath*:META-INF/msb/plugin下org.apache.dubbo.rpc.Filter文件中配置的拦截器
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
