package com.github.bannirui.msb.config.spring;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.spring.config.ConfigPropertySource;
import com.github.bannirui.msb.common.ex.FrameworkException;
import com.google.common.collect.Lists;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;

public class ConfigPropertySourceFactory {

    private final List<ConfigPropertySource> configPropertySources = Lists.newLinkedList();

    /**
     * @param name Apollo的nameSpace
     * @param source Apollo的Config
     */
    public ConfigPropertySource getConfigPropertySource(String name, Config source) {
        try {
            Class<ConfigPropertySource> clazz = ConfigPropertySource.class;
            Constructor<ConfigPropertySource> constructor = clazz.getDeclaredConstructor(String.class, Config.class);
            constructor.setAccessible(true);
            ConfigPropertySource configPropertySource = constructor.newInstance(name, source);
            Class<? super ConfigPropertySource> superclass = clazz.getSuperclass().getSuperclass();
            Field nameField = superclass.getDeclaredField("name");
            nameField.setAccessible(true);
            nameField.set(configPropertySource, name);
            Field sourceField = superclass.getDeclaredField("source");
            sourceField.setAccessible(true);
            sourceField.set(configPropertySource, source);
            this.configPropertySources.add(configPropertySource);
            return configPropertySource;
        } catch (Exception e) {
            throw new FrameworkException(FrameworkException.ERR_DEF, e.getMessage());
        }
    }

    public List<ConfigPropertySource> getAllConfigPropertySources() {
        return Lists.newLinkedList(this.configPropertySources);
    }
}
