package com.github.bannirui.msb.cache.autoconfig;

import com.github.bannirui.msb.cache.CompositeCacheProperties;
import com.github.bannirui.msb.cache.redis.autoconfig.DynamicRedisAutoConfiguration;
import com.github.bannirui.msb.cache.refresh.CacheRefreshTask;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizers;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.cache.CacheType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.couchbase.CouchbaseAutoConfiguration;
import org.springframework.boot.autoconfigure.hazelcast.HazelcastAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheAspectSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

@Configuration
@ConditionalOnClass({CacheManager.class})
@ConditionalOnBean({CacheAspectSupport.class})
@ConditionalOnMissingBean(
    value = {CacheManager.class},
    name = {"cacheResolver"}
)
@AutoConfigureBefore({org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration.class})
@EnableConfigurationProperties({CacheProperties.class, CompositeCacheProperties.class})
@AutoConfigureAfter({CouchbaseAutoConfiguration.class, HazelcastAutoConfiguration.class, HibernateJpaAutoConfiguration.class, DynamicRedisAutoConfiguration.class})
@Import({CacheAutoConfiguration.CacheConfigurationImportSelector.class})
public class CacheAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CacheManagerCustomizers cacheManagerCustomizers(ObjectProvider<CacheManagerCustomizer<?>> customizers) {
        return new CacheManagerCustomizers(customizers.orderedStream().collect(Collectors.toList()));
    }

    @Bean
    @ConditionalOnProperty(
        name = {"cache.refresh.enable"},
        havingValue = "true"
    )
    public CacheRefreshTask cacheRefreshTask() {
        return new CacheRefreshTask();
    }

    static class CacheConfigurationImportSelector implements ImportSelector {

        @Override
        public String[] selectImports(AnnotationMetadata importingClassMetadata) {
            CacheType[] types = CacheType.values();
            List<String> imports = new LinkedList<>();
            imports.add(CompositeCacheConfiguration.class.getName());
            for (CacheType type : types) {
                if (Objects.equals(type, CacheType.REDIS) || Objects.equals(type, CacheType.CAFFEINE)) {
                    imports.add(CacheConfigurations.getConfigurationClass(type));
                }
            }
            return imports.toArray(new String[imports.size()]);
        }
    }
}
