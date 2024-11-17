package com.github.bannirui.msb.log.autoconfigure;

import com.github.bannirui.msb.common.startup.MsbImportSelectorController;
import com.github.bannirui.msb.log.configuration.LogConfiguration;
import org.springframework.core.type.AnnotationMetadata;

/**
 * {@link com.github.bannirui.msb.log.annotation.EnableMsbLog}
 */
public class EnableMsbLogImportSelector extends MsbImportSelectorController {
    public EnableMsbLogImportSelector() {
    }

    @Override
    protected String[] mySelectImports(AnnotationMetadata metadata) {
        return new String[]{LogConfiguration.class.getName()};
    }
}
