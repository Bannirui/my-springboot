package com.github.bannirui.msb.web.autoconfig;

import com.github.bannirui.msb.common.startup.MsbImportSelectorController;
import com.github.bannirui.msb.web.config.WebConfig;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 用于注入web相关bean.
 */
public class MyWebImportSelectorController extends MsbImportSelectorController {

    @Override
    protected String[] mySelectImports(AnnotationMetadata metadata) {
        return new String[]{WebConfig.class.getName()};
    }
}
