package com.github.bannirui.msb.config;

import com.github.bannirui.msb.common.startup.MsbImportSelectorController;
import com.github.bannirui.msb.config.aop.FlowControlAop;
import org.springframework.core.type.AnnotationMetadata;

public class EnableConfigChangeListenerSelector extends MsbImportSelectorController {
    public EnableConfigChangeListenerSelector() {
    }

    @Override
    protected String[] mySelectImports(AnnotationMetadata metadata) {
        return new String[] {ConfigChangeListenerConfiguration.class.getName(),
            FlowControlAop.class.getName(),
            ConfigChangeListenerAdapter.class.getName()};
    }
}
