package com.github.bannirui.msb.config;

import com.github.bannirui.msb.common.startup.MsbImportSelectorController;
import com.github.bannirui.msb.config.aop.FlowControlAop;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 由{@link com.github.bannirui.msb.config.annotation.EnableMsbConfig}注入Spring
 * 监听Apollo的配置变更实现热更新
 */
public class MsbConfigChangeListenerSelector extends MsbImportSelectorController {

    @Override
    protected String[] mySelectorImports(AnnotationMetadata metadata) {
        return new String[] {
            ConfigChangeListenerConfiguration.class.getName(),
            FlowControlAop.class.getName(),
            ConfigChangeListenerAdapter.class.getName()
        };
    }
}
