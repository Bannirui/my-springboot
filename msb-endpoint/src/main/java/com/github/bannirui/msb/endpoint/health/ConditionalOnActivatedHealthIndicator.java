package com.github.bannirui.msb.endpoint.health;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Conditional;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Conditional({OnActivatedHealthIndicatorCondition.class})
public @interface ConditionalOnActivatedHealthIndicator {
    String name();

    boolean enabled() default true;
}
