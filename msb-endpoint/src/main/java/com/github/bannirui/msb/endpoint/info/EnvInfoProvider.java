package com.github.bannirui.msb.endpoint.info;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.core.env.StandardEnvironment;

public class EnvInfoProvider implements InfoProvider {
    private final Sanitizer sanitizer = new Sanitizer();
    private ApplicationContext applicationContext;

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public String id() {
        return "env";
    }

    @Override
    public String info() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("profiles", this.applicationContext.getEnvironment().getActiveProfiles());
        PropertyResolver resolver = this.getResolver();
        for (Map.Entry<String, PropertySource<?>> entry : this.getPropertySourcesAsMap().entrySet()) {
            String sourceName = entry.getKey();
            PropertySource<?> source = entry.getValue();
            if(!(source instanceof EnumerablePropertySource<?>)) continue;
            EnumerablePropertySource<?> enumerable = (EnumerablePropertySource)source;
            Map<String, Object> properties = new LinkedHashMap<>();
            for (String name : enumerable.getPropertyNames()) {
                Object resolved = source.getProperty(name);
                if (resolved instanceof String) {
                    resolved = resolver.resolvePlaceholders((String)resolved);
                }
                properties.put(name, this.sanitize(name, resolved));
            }
            Map<String, Object> map = this.postProcessSourceProperties(sourceName, properties);
            if(Objects.nonNull(map)) result.put(sourceName, map);
        }
        return JSON.toJSONString(result, SerializerFeature.PrettyFormat);
    }

    public PropertyResolver getResolver() {
        PlaceholderSanitizingPropertyResolver resolver = new EnvInfoProvider.PlaceholderSanitizingPropertyResolver(this.getPropertySources(), this.sanitizer);
        resolver.setIgnoreUnresolvableNestedPlaceholders(true);
        return resolver;
    }

    private MutablePropertySources getPropertySources() {
        Environment environment = this.applicationContext.getEnvironment();
        MutablePropertySources sources = null;
        if (Objects.nonNull(environment) && environment instanceof ConfigurableEnvironment) {
            sources = ((ConfigurableEnvironment)environment).getPropertySources();
        } else {
            sources = (new StandardEnvironment()).getPropertySources();
        }
        return sources;
    }

    private Map<String, PropertySource<?>> getPropertySourcesAsMap() {
        Map<String, PropertySource<?>> map = new LinkedHashMap<>();
        for (PropertySource<?> source : this.getPropertySources()) {
            this.extract("", map, source);
        }
        return map;
    }

    private void extract(String root, Map<String, PropertySource<?>> map, PropertySource<?> source) {
        if (source instanceof CompositePropertySource) {
            for (PropertySource<?> nest : ((CompositePropertySource) source).getPropertySources()) {
                this.extract(source.getName() + ":", map, nest);
            }
        } else {
            map.put(root + source.getName(), source);
        }
    }

    protected Map<String, Object> postProcessSourceProperties(String sourceName, Map<String, Object> properties) {
        return properties;
    }

    public Object sanitize(String name, Object object) {
        return this.sanitizer.sanitize(name, object);
    }

    private class PlaceholderSanitizingPropertyResolver extends PropertySourcesPropertyResolver {
        private final Sanitizer sanitizer;

        PlaceholderSanitizingPropertyResolver(PropertySources propertySources, Sanitizer sanitizer) {
            super(propertySources);
            this.sanitizer = sanitizer;
        }

        @Override
        protected String getPropertyAsRawString(String key) {
            String value = super.getPropertyAsRawString(key);
            return (String)this.sanitizer.sanitize(key, value);
        }
    }
}
