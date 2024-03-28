package com.github.bannirui.msb.common.startup;

import com.github.bannirui.msb.common.constant.EnableType;
import java.util.HashSet;
import java.util.Set;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

/**
 * SpringBoot自动装配的实现原理.
 * <ul>
 *     <li>实现ImportSelector接口</li>
 *     <li>用@Import将这个类注册到Spring容器等待回调</li>
 *     <li>在回调方法selectImports中完成配置类的Bean注入</li>
 * </ul>
 */
public abstract class MyAutoConfigImportSelector implements ImportSelector {

    /**
     * xxxImportSelector.
     *
     * @see EnableType
     */
    private static Set<String> enableStarterSet = new HashSet<>();

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        // xxxImportSelector
        String clz = this.getClass().getSimpleName();
        EnableType type = null;
        if (StringUtils.isEmpty(clz) || (type = EnableType.get8Importer(clz)) == null) {
            return new String[0];
        }
        enableStarterSet.add(clz);
        return this.mySelectImports(importingClassMetadata);
    }

    protected abstract String[] mySelectImports(AnnotationMetadata metadata);
}
