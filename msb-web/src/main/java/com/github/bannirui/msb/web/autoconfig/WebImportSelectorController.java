package com.github.bannirui.msb.web.autoconfig;

import com.github.bannirui.msb.startup.MsbImportSelectorController;
import com.github.bannirui.msb.web.annotation.EnableWeb;
import com.github.bannirui.msb.web.config.SSOConfiguration;
import com.github.bannirui.msb.web.config.WebConfiguration;
import com.github.bannirui.msb.web.session.SessionConfiguration;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 用于注入web相关bean.
 */
public class WebImportSelectorController extends MsbImportSelectorController {

    @Override
    protected String[] mySelectorImports(AnnotationMetadata metadata) {
        AnnotationAttributes annotationAttributes = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(EnableWeb.class.getName()));
        SessionConfiguration.sessionStorageClass = annotationAttributes.getString("sessionType");
        SessionConfiguration.sessionType = annotationAttributes.getString("sessionStrategy");
        return new String[]{
                SSOConfiguration.class.getName(),
                SessionConfiguration.class.getName(),
                WebConfiguration.class.getName()
        };
    }
}
