package com.github.bannirui.msb.config;

import com.github.bannirui.msb.common.startup.MsbImportSelectorController;
import com.github.bannirui.msb.config.aop.FlowControlAop;
import org.springframework.core.type.AnnotationMetadata;

public class EnableMsbConfigChangeListenerSelector extends MsbImportSelectorController {
    public EnableMsbConfigChangeListenerSelector() {
    }

    @Override
    protected String[] mySelectImports(AnnotationMetadata metadata) {
        return new String[] {ConfigChangeListenerConfiguration.class.getName(),
            FlowControlAop.class.getName(),
            ConfigChangeListenerAdapter.class.getName()};
    }
}
