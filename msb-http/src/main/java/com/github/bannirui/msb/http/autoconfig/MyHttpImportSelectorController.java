package com.github.bannirui.msb.http.autoconfig;

import com.github.bannirui.msb.common.startup.MsbImportSelectorController;
import com.github.bannirui.msb.http.config.HttpConfig;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 用于注入http相关的bean.
 */
public class MyHttpImportSelectorController extends MsbImportSelectorController {

    @Override
    protected String[] mySelectorImports(AnnotationMetadata metadata) {
        return new String[] {HttpConfig.class.getName()};
    }
}
