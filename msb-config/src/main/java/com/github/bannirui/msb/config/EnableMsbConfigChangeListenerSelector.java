package com.github.bannirui.msb.config;

import com.github.bannirui.msb.common.startup.MsbImportSelectorController;
import com.github.bannirui.msb.config.aop.FlowControlAop;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 监听Apollo的配置变更实现热更新
 */
public class EnableMsbConfigChangeListenerSelector extends MsbImportSelectorController {

    @Override
    protected String[] mySelectImports(AnnotationMetadata metadata) {
        return new String[] {
            ConfigChangeListenerConfiguration.class.getName(),
            FlowControlAop.class.getName(),
            ConfigChangeListenerAdapter.class.getName()
        };
    }
}
