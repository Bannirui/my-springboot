package com.github.bannirui.msb.es.autoconfig;

import com.github.bannirui.msb.es.config.ElasticsearchRestClientConfiguration;
import com.github.bannirui.msb.es.config.ElasticsearchRestTemplateConfiguration;
import com.github.bannirui.msb.startup.MsbImportSelectorController;
import org.springframework.core.type.AnnotationMetadata;

public class EnableEsImportSelector extends MsbImportSelectorController {

    @Override
    protected String[] mySelectorImports(AnnotationMetadata metadata) {
        return new String[] {ElasticsearchRestClientConfiguration.class.getName(),
            ElasticsearchRestTemplateConfiguration.class.getName()};
    }
}
