package com.github.bannirui.msb.mq.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface MMSBatchListener {
    String consumerGroup();

    String tags() default "*";

    int consumeThreadMin() default -1;

    int consumeThreadMax() default -1;

    int orderlyConsumePartitionParallelism() default 1;

    int maxBatchRecords() default -1;

    int consumeBatchSize() default 1;

    boolean isOrderly() default false;

    int consumeTimeoutMs() default -1;

    int maxReconsumeTimes() default -1;

    String isNewPush() default "false";
}
