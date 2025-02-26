package com.github.bannirui.msb.endpoint.condition;

import java.util.Arrays;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OnPropertyCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Environment environment = context.getEnvironment();
        AnnotationAttributes annotationAttributes = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(ConditionalOnConfig.class.getName()));
        String prefix = annotationAttributes.getString("prefix");
        Class<?> entity = annotationAttributes.getClass("entity");
        if (entity == Object.class) {
            String finalPrefix = prefix + ".";
            MutablePropertySources propertySources = ((ConfigurableEnvironment)environment).getPropertySources();
            boolean anyMatch = propertySources.stream().filter((propertySource) -> propertySource instanceof EnumerablePropertySource).anyMatch((propertySource) -> Arrays.stream(((EnumerablePropertySource)propertySource).getPropertyNames()).anyMatch((a) -> a.startsWith(finalPrefix)));
            return anyMatch ? ConditionOutcome.match("找到前缀为[" + prefix + "]的属性") : ConditionOutcome.noMatch("未找到前缀为[" + prefix + "]的属性");
        } else {
            Object result = Binder.get(environment).bind(prefix, entity).orElseGet(()->null);
            return result != null ? ConditionOutcome.match("找到前缀为[" + prefix + "],实体类为[" + entity.getName() + "]的属性") : ConditionOutcome.noMatch("未找到前缀为[" + prefix + "],实体类为[" + entity.getName() + "]的属性");
        }
    }
}
