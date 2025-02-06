package com.github.bannirui.msb.plugin;

import com.github.bannirui.msb.annotation.MsbPlugin;
import com.github.bannirui.msb.ex.FrameworkException;

/**
 * 封装{@link Interceptor}拦截器
 */
public class PluginDecorator<T extends Class<?>> implements Comparable<Object> {
    /**
     * 动态代理回调的拦截器类 是{@link Interceptor}的派生
     */
    T plugin;
    /**
     * 用于标识{@link PluginDecorator#plugin}的优先级 值越大优先级越低 通过{@link MsbPlugin}指定
     */
    int order;
    Object instance;

    public PluginDecorator(T plugin) {
        this(plugin, 0);
    }

    public PluginDecorator(T plugin, int order) {
        this.plugin = plugin;
        this.order = order;
    }

    public T getPlugin() {
        return plugin;
    }

    public void setPlugin(T plugin) {
        this.plugin = plugin;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }

    @Override
    public int compareTo(Object o) {
        if (!(o instanceof PluginDecorator)) {
            throw FrameworkException.getInstance("PluginDecorator can not compare to other type");
        }
        PluginDecorator<T> other = (PluginDecorator<T>) o;
        return this.order-other.order;
    }
}
