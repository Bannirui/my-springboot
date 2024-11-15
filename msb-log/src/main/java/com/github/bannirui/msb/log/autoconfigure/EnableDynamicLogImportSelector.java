package com.github.bannirui.msb.log.autoconfigure;

import com.github.bannirui.msb.common.startup.MsbImportSelectorController;
import com.github.bannirui.msb.log.configuration.LogConfiguration;
import org.springframework.core.type.AnnotationMetadata;

public class EnableDynamicLogImportSelector extends MsbImportSelectorController {
    public EnableDynamicLogImportSelector() {
    }

    @Override
    protected String[] mySelectImports(AnnotationMetadata metadata) {
        return new String[]{LogConfiguration.class.getName()};
    }
}
