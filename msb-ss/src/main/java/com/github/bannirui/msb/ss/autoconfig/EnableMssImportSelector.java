package com.github.bannirui.msb.ss.autoconfig;

import com.github.bannirui.msb.ss.config.MssWorkerConfiguration;
import com.github.bannirui.msb.startup.MsbImportSelectorController;
import com.github.bannirui.mss.worker.common.MssWorkerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.core.type.AnnotationMetadata;

public class EnableMssImportSelector extends MsbImportSelectorController {

    /**
     * 注入容器 后面在{@link com.github.bannirui.msb.ss.config.MssWorkerConfiguration}中使用
     * @return
     */
    @Bean
    public MssWorkerConfig mssWorkerConfig() {
        return new MssWorkerConfig();
    }

    @Override
    protected String[] mySelectorImports(AnnotationMetadata metadata) {
        return new String[]{MssWorkerConfiguration.class.getName()};
    }
}
