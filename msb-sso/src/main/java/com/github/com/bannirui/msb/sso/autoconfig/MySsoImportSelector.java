package com.github.com.bannirui.msb.sso.autoconfig;

import com.github.bannirui.msb.common.startup.MyAutoConfigImportSelector;
import com.github.com.bannirui.msb.sso.config.SsoConfig;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 注入自动配置.
 */
public class MySsoImportSelector extends MyAutoConfigImportSelector {

    @Override
    protected String[] mySelectImports(AnnotationMetadata metadata) {
        return new String[]{SsoConfig.class.getName()};
    }
}
