package com.github.bannirui.msb.orm.autoconfig;

import com.github.bannirui.msb.orm.aop.MultiTransactionalInterceptor;
import com.github.bannirui.msb.orm.configuration.ShardingConfigChangeEventListener;
import com.github.bannirui.msb.orm.configuration.ShardingJdbcConfiguration;
import com.github.bannirui.msb.startup.MsbImportSelectorController;
import org.springframework.core.type.AnnotationMetadata;

public class EnableShardingJdbcImportSelector extends MsbImportSelectorController {

    @Override
    protected String[] mySelectorImports(AnnotationMetadata metadata) {
        return new String[]{
            ShardingJdbcConfiguration.class.getName(),
            ShardingConfigChangeEventListener.class.getName(),
            MultiTransactionalInterceptor.class.getName()
        };
    }
}
