package com.github.bannirui.msb.endpoint.condition;

import java.lang.annotation.Annotation;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

public abstract class OnEndpointElementCondition extends SpringBootCondition {
    private final String prefix;
    private final Class<? extends Annotation> annotationType;

    public OnEndpointElementCondition(String prefix, Class<? extends Annotation> annotationType) {
        this.prefix = prefix;
        this.annotationType = annotationType;
    }

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        AnnotationAttributes annotationAttributes = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(this.annotationType.getName()));
        String healthIndicator = annotationAttributes.getString("name");
        boolean enabled = annotationAttributes.getBoolean("enabled");
        Environment environment = context.getEnvironment();
        String enabledProperty = this.prefix + healthIndicator + ".enabled";
        boolean match = environment.getProperty(enabledProperty, Boolean.class, enabled);
        return new ConditionOutcome(match, ConditionMessage.forCondition(this.annotationType).because(enabledProperty + " is " + match));
    }
}
