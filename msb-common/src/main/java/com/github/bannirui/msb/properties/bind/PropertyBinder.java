package com.github.bannirui.msb.properties.bind;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.PropertySourcesPlaceholdersResolver;
import org.springframework.boot.context.properties.bind.handler.IgnoreTopLevelConverterNotFoundBindHandler;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.core.env.ConfigurableEnvironment;

public class PropertyBinder {

    private ConfigurableEnvironment environment;
    private Binder binder;
    private BindHandler handler;
    private Set<String> blacklist;

    public PropertyBinder(ConfigurableEnvironment environment) {
        this(environment, new HashSet<>());
    }

    public PropertyBinder(ConfigurableEnvironment environment, Set<String> blacklist) {
        this.environment = environment;
        this.blacklist = blacklist;
        this.binder = new Binder(this.getConfigurationPropertySources(), this.getPropertySourcesPlaceholdersResolver());
    }

    /**
     * 根据配置前缀将配置映射到java对象
     * @param configPrefix 配置前缀
     * @param target 配置映射到java实体
     * @return java对象
     */
    public <T> BindResult<T> bind(String configPrefix, Class<T> target) {
        return this.bind(configPrefix, Bindable.of(target));
    }

    public <T> BindResult<T> bind(String configPrefix, Bindable<T> bindable) {
        return this.binder.bind(configPrefix, bindable,
            Objects.nonNull(this.handler) ? this.handler : new BlackListBindHandler(new IgnoreTopLevelConverterNotFoundBindHandler(), this.blacklist));
    }

    private Iterable<ConfigurationPropertySource> getConfigurationPropertySources() {
        return ConfigurationPropertySources.from(this.environment.getPropertySources());
    }

    private PropertySourcesPlaceholdersResolver getPropertySourcesPlaceholdersResolver() {
        return new PropertySourcesPlaceholdersResolver(this.environment.getPropertySources());
    }
}
