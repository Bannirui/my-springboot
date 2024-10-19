package com.github.bannirui.msb.common.properties.bind;

import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.bind.AbstractBindHandler;
import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;

public class BlackListBindHandler extends AbstractBindHandler {
    private static final Logger logger = LoggerFactory.getLogger(BlackListBindHandler.class);
    private Set<String> blacklist = new HashSet();

    public BlackListBindHandler() {
    }

    public BlackListBindHandler(BindHandler handler) {
        super(handler);
    }

    public BlackListBindHandler(BindHandler parent, Set<String> blacklist) {
        super(parent);
        this.blacklist = blacklist;
    }

    @Override
    public <T> Bindable<T> onStart(ConfigurationPropertyName name, Bindable<T> target, BindContext context) {
        if (this.checkBlackList(name.toString())) {
            logger.info("property name: {} hits black list, result {}", name.toString(), blacklist.size());
            return null;
        }
        return super.onStart(name, target, context);
    }

    private boolean checkBlackList(String propertyName) {
        return this.blacklist.stream().anyMatch((exclude) -> exclude.equals(propertyName));
    }
}