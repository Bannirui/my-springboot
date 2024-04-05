package com.github.bannirui.msb.remotecfg.autoconfig;

import com.github.bannirui.msb.common.startup.MyAutoConfigImportSelector;
import com.github.bannirui.msb.remotecfg.EnableRemoteCfgAnnotationCheck;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 用于注入远程配置中心相关bean.
 */
public class MyRemoteCfgImportSelector extends MyAutoConfigImportSelector {

    @Override
    protected String[] mySelectImports(AnnotationMetadata metadata) {
        return new String[] {
            EnableRemoteCfgAnnotationCheck.class.getName()
        };
    }
}
