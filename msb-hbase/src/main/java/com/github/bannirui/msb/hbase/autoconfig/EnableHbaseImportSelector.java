package com.github.bannirui.msb.hbase.autoconfig;

import com.github.bannirui.msb.hbase.config.HbaseConfiguration;
import com.github.bannirui.msb.startup.MsbImportSelectorController;
import org.springframework.core.type.AnnotationMetadata;

public class EnableHbaseImportSelector extends MsbImportSelectorController {

    @Override
    protected String[] mySelectorImports(AnnotationMetadata metadata) {
        return new String[]{HbaseConfiguration.class.getName()};
    }
}
