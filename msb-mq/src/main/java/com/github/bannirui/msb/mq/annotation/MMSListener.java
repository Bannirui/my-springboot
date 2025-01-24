package com.github.bannirui.msb.mq.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 打在方法上标识监听MQ
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface MMSListener {
    String consumerGroup();

    /**
     * 多个tag用||分割
     */
    String tags() default "*";

    String consumeThreadMin() default "";

    String consumeThreadMax() default "";

    String orderlyConsumePartitionParallelism() default "";

    String maxBatchRecords() default "";

    boolean easy() default false;

    String isOrderly() default "false";

    String consumeTimeoutMs() default "";

    String maxReconsumeTimes() default "";

    String isNewPush() default "false";

    String orderlyConsumeThreadSize() default "";
}
