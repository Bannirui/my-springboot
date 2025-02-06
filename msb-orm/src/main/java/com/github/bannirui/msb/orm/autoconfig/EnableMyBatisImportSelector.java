package com.github.bannirui.msb.orm.autoconfig;

import com.github.bannirui.msb.orm.aop.MultiTransactionalInterceptor;
import com.github.bannirui.msb.orm.configuration.DataSourceChangeEventListener;
import com.github.bannirui.msb.orm.configuration.MyBatisConfiguration;
import com.github.bannirui.msb.startup.MsbImportSelectorController;
import org.springframework.core.type.AnnotationMetadata;

public class EnableMyBatisImportSelector extends MsbImportSelectorController {
    @Override
    protected String[] mySelectorImports(AnnotationMetadata metadata) {
        return new String[]{
            MyBatisConfiguration.class.getName(),
            MultiTransactionalInterceptor.class.getName(),
            DataSourceChangeEventListener.class.getName()
        };
    }
}
