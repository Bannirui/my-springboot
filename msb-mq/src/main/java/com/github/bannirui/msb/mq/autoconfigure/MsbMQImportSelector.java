package com.github.bannirui.msb.mq.autoconfigure;

import com.github.bannirui.msb.common.startup.MsbImportSelectorController;
import com.github.bannirui.msb.mq.configuration.MMSConfiguration;
import org.springframework.core.type.AnnotationMetadata;

public class MsbMQImportSelector extends MsbImportSelectorController {

    @Override
    protected String[] mySelectorImports(AnnotationMetadata metadata) {
        return new String[]{MMSConfiguration.class.getName()};
    }
}
