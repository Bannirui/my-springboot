package com.github.bannirui.msb.dubbo.autoconfig;

import com.github.bannirui.msb.dubbo.ResolveReferenceAnnotationProcessor;
import com.github.bannirui.msb.dubbo.config.AuthConfigChangeEventListener;
import com.github.bannirui.msb.startup.MsbImportSelectorController;
import org.springframework.core.type.AnnotationMetadata;

public class ComponentImportSelector extends MsbImportSelectorController {

    @Override
    protected String[] mySelectorImports(AnnotationMetadata metadata) {
        return new String[] {
            ResolveReferenceAnnotationProcessor.class.getName(),
            AuthConfigChangeEventListener.class.getName()
        };
    }
}
