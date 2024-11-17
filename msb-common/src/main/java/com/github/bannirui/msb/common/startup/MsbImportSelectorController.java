package com.github.bannirui.msb.common.startup;

import java.util.Arrays;
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
     * 场景启动器.
     */
    static enum EnableType {
        EnableMsbConfigChangeListener("EnableMsbConfig", "EnableMsbConfigChangeListenerSelector"),
        EnableMsbLog("EnableMsbLog", "EnableMsbLogImportSelector"),
        ;

        private final String starter;
        private final String importer;

        EnableType(String starter, String importer) {
            this.starter = starter;
            this.importer = importer;
        }

        public String getStarter() {
            return starter;
        }

        public String getImporter() {
            return this.importer;
        }

        public static EnableType get8Importer(String name) {
            return Arrays.stream(values()).filter(e -> e.getImporter().equals(name)).findFirst().orElseGet(() -> null);
        }
    }

    /**
     * xxxImporter.
     */
    private static Set<String> enable_starter_set = new HashSet<>();

    public MsbImportSelectorController() {
    }

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        // xxxImportSelector
        EnableType type = EnableType.get8Importer(this.getClass().getSimpleName());
        if (Objects.nonNull(type) && this.isEnable(type)) {
            enable_starter_set.add(type.getImporter());
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
        return enable_starter_set;
    }
}
