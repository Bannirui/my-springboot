package com.github.bannirui.msb.config;

import com.github.bannirui.msb.common.startup.MsbImportSelectorController;
import org.springframework.core.type.AnnotationMetadata;

public class EnableIdGeneratorImportSelector extends MsbImportSelectorController {

    public EnableIdGeneratorImportSelector() {
    }

    @Override
    protected String[] mySelectorImports(AnnotationMetadata metadata) {
        return new String[] {IdGeneratorConfiguration.class.getName()};
    }
}
