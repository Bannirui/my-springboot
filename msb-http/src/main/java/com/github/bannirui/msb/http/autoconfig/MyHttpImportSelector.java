package com.github.bannirui.msb.http.autoconfig;

import com.github.bannirui.msb.common.startup.MyAutoConfigImportSelector;
import com.github.bannirui.msb.http.config.HttpConfig;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 用于注入http相关的bean.
 */
public class MyHttpImportSelector extends MyAutoConfigImportSelector {

    @Override
    protected String[] mySelectImports(AnnotationMetadata metadata) {
        return new String[] {HttpConfig.class.getName()};
    }
}
