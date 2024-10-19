package com.github.bannirui.msb.config.spring;

public class SpringValueDefinition {
    private final String key;
    private final String placeholder;
    private final String propertyName;

    public SpringValueDefinition(String key, String placeholder, String propertyName) {
        this.key = key;
        this.placeholder = placeholder;
        this.propertyName = propertyName;
    }

    public String getKey() {
        return this.key;
    }

    public String getPlaceholder() {
        return this.placeholder;
    }

    public String getPropertyName() {
        return this.propertyName;
    }
}
