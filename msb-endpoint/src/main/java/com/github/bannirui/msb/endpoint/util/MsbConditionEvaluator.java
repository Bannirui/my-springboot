package com.github.bannirui.msb.endpoint.util;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.Environment;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.MultiValueMap;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MsbConditionEvaluator {
    private final MsbConditionEvaluator.ConditionContextImpl context;

    public MsbConditionEvaluator(@Nullable BeanDefinitionRegistry registry, @Nullable Environment environment,
                                 @Nullable ResourceLoader resourceLoader) {
        this.context = new MsbConditionEvaluator.ConditionContextImpl(registry, environment, resourceLoader);
    }

    public boolean shouldSkip(@Nullable AnnotatedTypeMetadata metadata) {
        if (Objects.isNull(metadata) || !metadata.isAnnotated(Conditional.class.getName())) return false;
        List<Condition> conditions = new ArrayList<>();
        for (String[] conditionClasses : this.getConditionClasses(metadata)) {
            for (String conditionClass : conditionClasses) {
                Condition condition = this.getCondition(conditionClass, this.context.getClassLoader());
                conditions.add(condition);
            }
        }
        AnnotationAwareOrderComparator.sort(conditions);
        for (Condition condition : conditions) {
            if (condition instanceof ConfigurationCondition configurationCondition) {
                ConfigurationCondition.ConfigurationPhase configurationPhase = configurationCondition.getConfigurationPhase();
                if (Objects.isNull(configurationPhase) && condition.matches(this.context, metadata)) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<String[]> getConditionClasses(AnnotatedTypeMetadata metadata) {
        MultiValueMap<String, Object> attributes = metadata.getAllAnnotationAttributes(Conditional.class.getName(), true);
        Object values = attributes != null ? attributes.get("value") : null;
        return values != null ? (List<String[]>) values : Collections.emptyList();
    }

    private Condition getCondition(String conditionClassName, @Nullable ClassLoader classloader) {
        Class<?> conditionClass = ClassUtils.resolveClassName(conditionClassName, classloader);
        return (Condition) BeanUtils.instantiateClass(conditionClass);
    }

    private static class ConditionContextImpl implements ConditionContext {
        @Nullable
        private final BeanDefinitionRegistry registry;
        @Nullable
        private final ConfigurableListableBeanFactory beanFactory;
        private final Environment environment;
        private final ResourceLoader resourceLoader;
        @Nullable
        private final ClassLoader classLoader;

        public ConditionContextImpl(@Nullable BeanDefinitionRegistry registry, @Nullable Environment environment,
                                    @Nullable ResourceLoader resourceLoader) {
            this.registry = registry;
            this.beanFactory = this.deduceBeanFactory(registry);
            this.environment = environment != null ? environment : this.deduceEnvironment(registry);
            this.resourceLoader = resourceLoader != null ? resourceLoader : this.deduceResourceLoader(registry);
            this.classLoader = this.deduceClassLoader(resourceLoader, this.beanFactory);
        }

        @Nullable
        private ConfigurableListableBeanFactory deduceBeanFactory(@Nullable BeanDefinitionRegistry source) {
            if (source instanceof ConfigurableListableBeanFactory) {
                return (ConfigurableListableBeanFactory) source;
            } else {
                return source instanceof ConfigurableApplicationContext ? ((ConfigurableApplicationContext) source).getBeanFactory() : null;
            }
        }

        private Environment deduceEnvironment(@Nullable BeanDefinitionRegistry source) {
            return source instanceof EnvironmentCapable ? ((EnvironmentCapable) source).getEnvironment() : new StandardEnvironment();
        }

        private ResourceLoader deduceResourceLoader(@Nullable BeanDefinitionRegistry source) {
            return source instanceof ResourceLoader ? (ResourceLoader) source : new DefaultResourceLoader();
        }

        @Nullable
        private ClassLoader deduceClassLoader(@Nullable ResourceLoader resourceLoader, @Nullable ConfigurableListableBeanFactory beanFactory) {
            if (resourceLoader != null) {
                ClassLoader classLoader = resourceLoader.getClassLoader();
                if (classLoader != null) {
                    return classLoader;
                }
            }
            return beanFactory != null ? beanFactory.getBeanClassLoader() : ClassUtils.getDefaultClassLoader();
        }

        @Override
        public BeanDefinitionRegistry getRegistry() {
            Assert.state(this.registry != null, "No BeanDefinitionRegistry available");
            return this.registry;
        }

        @Nullable
        @Override
        public ConfigurableListableBeanFactory getBeanFactory() {
            return this.beanFactory;
        }

        @Override
        public Environment getEnvironment() {
            return this.environment;
        }

        @Override
        public ResourceLoader getResourceLoader() {
            return this.resourceLoader;
        }

        @Nullable
        @Override
        public ClassLoader getClassLoader() {
            return this.classLoader;
        }
    }
}
