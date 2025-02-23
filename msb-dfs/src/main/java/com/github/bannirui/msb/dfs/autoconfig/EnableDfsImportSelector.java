package com.github.bannirui.msb.dfs.autoconfig;

import com.github.bannirui.msb.dfs.config.DfsConfiguration;
import com.github.bannirui.msb.startup.MsbImportSelectorController;
import org.springframework.core.type.AnnotationMetadata;

public class EnableDfsImportSelector extends MsbImportSelectorController {

    @Override
    protected String[] mySelectorImports(AnnotationMetadata metadata) {
        return new String[]{DfsConfiguration.class.getName()};
    }
}
