package com.github.bannirui.msb.config.annotation;

import com.github.bannirui.msb.config.processor.DefaultFlowControlProcessor;
import com.github.bannirui.msb.config.processor.FlowControlProcessor;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FlowControlSwitch {
    int weightRatio() default 50;

    String fallback();

    Class<? extends FlowControlProcessor> processor() default DefaultFlowControlProcessor.class;
}
