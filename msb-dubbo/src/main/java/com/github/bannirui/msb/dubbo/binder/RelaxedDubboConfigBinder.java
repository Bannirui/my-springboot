package com.github.bannirui.msb.dubbo.binder;

import org.apache.dubbo.config.AbstractConfig;
import org.apache.dubbo.config.spring.context.properties.AbstractDubboConfigBinder;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.PropertySourcesPlaceholdersResolver;
import org.springframework.boot.context.properties.bind.handler.IgnoreErrorsBindHandler;
import org.springframework.boot.context.properties.bind.handler.NoUnboundElementsBindHandler;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.context.properties.source.UnboundElementsSourceFilter;
import org.springframework.core.env.PropertySource;

public class RelaxedDubboConfigBinder extends AbstractDubboConfigBinder {

    @Override
    public <C extends AbstractConfig> void bind(String prefix, C dubboConfig) {
        Iterable<PropertySource<?>> propertySources = this.getPropertySources();
        Iterable<ConfigurationPropertySource> configurationPropertySources = ConfigurationPropertySources.from(propertySources);
        Bindable<C> bindable = Bindable.ofInstance(dubboConfig);
        Binder binder = new Binder(configurationPropertySources, new PropertySourcesPlaceholdersResolver(propertySources));
        BindHandler bindHandler = this.getBindHandler();
        binder.bind(prefix, bindable, bindHandler);
    }

    private BindHandler getBindHandler() {
        BindHandler handler = BindHandler.DEFAULT;
        if (this.isIgnoreInvalidFields()) {
            handler = new IgnoreErrorsBindHandler((BindHandler)handler);
        }
        if (!this.isIgnoreUnknownFields()) {
            UnboundElementsSourceFilter filter = new UnboundElementsSourceFilter();
            handler = new NoUnboundElementsBindHandler((BindHandler)handler, filter);
        }
        return handler;
    }
}
