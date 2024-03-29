package com.github.bannirui.msb.remotecfg.autoconfig;

import com.github.bannirui.msb.common.startup.MyAutoConfigImportSelector;
import com.github.bannirui.msb.remotecfg.config.RemoteCfgChangeListenerConfig;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 远程配置变更.
 */
public class MyRemoteCfgChangeListenerImportSelector extends MyAutoConfigImportSelector {

    @Override
    protected String[] mySelectImports(AnnotationMetadata metadata) {
        return new String[] {RemoteCfgChangeListenerConfig.class.getName()};
    }
}
