package com.github.bannirui.msb.log.autoconfigure;

import com.github.bannirui.msb.common.startup.MsbImportSelectorController;
import com.github.bannirui.msb.log.configuration.LogConfiguration;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 由{@link com.github.bannirui.msb.log.annotation.EnableMsbLog}注入Spring
 */
public class MsbLogImportSelector extends MsbImportSelectorController {

    @Override
    protected String[] mySelectorImports(AnnotationMetadata metadata) {
        return new String[]{LogConfiguration.class.getName()};
    }
}
