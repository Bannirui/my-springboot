package com.github.com.bannirui.msb.sso.autoconfig;

import com.github.bannirui.msb.common.startup.MsbImportSelectorController;
import com.github.com.bannirui.msb.sso.config.SsoConfig;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 注入自动配置.
 */
public class MySsoImportSelectorController extends MsbImportSelectorController {

    @Override
    protected String[] mySelectorImports(AnnotationMetadata metadata) {
        return new String[]{SsoConfig.class.getName()};
    }
}
