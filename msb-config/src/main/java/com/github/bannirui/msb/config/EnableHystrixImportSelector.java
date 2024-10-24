package com.github.bannirui.msb.config;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

public class EnableHystrixImportSelector implements ImportSelector {

    public EnableHystrixImportSelector() {
    }

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[] {HystrixConfiguration.class.getName()};
    }
}
