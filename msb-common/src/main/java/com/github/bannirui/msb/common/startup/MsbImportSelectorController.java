package com.github.bannirui.msb.common.startup;

import com.github.bannirui.msb.common.constant.EnableType;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * SpringBoot自动装配的实现原理.
 * <ul>
 *     <li>实现ImportSelector接口</li>
 *     <li>用@Import将这个类注册到Spring容器等待回调</li>
 *     <li>在回调方法selectImports中完成配置类的Bean注入</li>
 * </ul>
 */
public abstract class MsbImportSelectorController implements ImportSelector {

    /**
     * xxxImportSelector.
     *
     * @see EnableType
     */
    private static Set<String> enableStarterSet = new HashSet<>();

    public MsbImportSelectorController() {
    }

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        // xxxImportSelector
        EnableType type = EnableType.get8Importer(this.getClass().getSimpleName());
        if (Objects.nonNull(type) && this.isEnable(type)) {
            enableStarterSet.add(type.getImporter());
            return this.mySelectImports(importingClassMetadata);
        } else {
            return new String[0];
        }
    }

    protected abstract String[] mySelectImports(AnnotationMetadata metadata);

    private boolean isEnable(EnableType enableType) {
        return true;
    }

    public static Set<String> getEnableModules() {
        return enableStarterSet;
    }
}
