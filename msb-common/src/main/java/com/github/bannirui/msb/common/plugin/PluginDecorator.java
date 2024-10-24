package com.github.bannirui.msb.common.plugin;

import com.github.bannirui.msb.common.ex.FrameworkException;

public class PluginDecorator<T extends Class> implements Comparable {
    T plugin;
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
            throw FrameworkException.getInstance("PluginDecorator can not compare to other type", new Object[0]);
        }
        PluginDecorator other = (PluginDecorator) o;
        if (this.order == other.order) {
            return 0;
        }
        return this.order > other.order ? 1 : -1;
    }
}
