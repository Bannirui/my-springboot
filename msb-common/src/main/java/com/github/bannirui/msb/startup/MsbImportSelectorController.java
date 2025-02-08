package com.github.bannirui.msb.startup;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
     * cache starter and importer.
     */
    private static Set<String> starters = new HashSet<>();
    private static Set<String> importers = new HashSet<>();

    /**
     * @param importingClassMetadata 通过场景启动器import的Bean
     */
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        // xxxImportSelector
        EnableType type = EnableType.get8Importer(this.getClass().getSimpleName());
        if (Objects.nonNull(type) && this.isEnable(type)) {
            starters.add(type.getStarter());
            importers.add(type.getImporter());
            return this.mySelectorImports(importingClassMetadata);
        } else {
            return new String[0];
        }
    }

    protected abstract String[] mySelectorImports(AnnotationMetadata metadata);

    private boolean isEnable(EnableType enableType) {
        return true;
    }

    public static Set<String> getEnableModules() {
        return importers;
    }


    /**
     * 场景启动器 n:1 一个场景启动器starter可能需要import多个Bean
     */
    enum EnableType {
        MsbConfigChangeListener("EnableMsbConfig", "MsbConfigChangeListenerSelector"),
        MsbLog("EnableMsbLog", "MsbLogImportSelector"),
        MsbMQ("EnableMsbMQ", "MsbMQImportSelector"),
        MsbMybatis("EnableMyBatis", "EnableMyBatisImportSelector"),
        MsbShardingJdbc("EnableShardingJdbc", "EnableShardingJdbcImportSelector"),
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

        private static final Map<String, EnableType> cache_by_importer = new HashMap<>();
        static {
            for (EnableType e : EnableType.values()) {
                cache_by_importer.put(e.getImporter(), e);
            }
        }
        public static EnableType get8Importer(String name) {
            return cache_by_importer.get(name);
        }
    }
}
