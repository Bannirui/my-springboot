package com.github.bannirui.msb.mq.annotation;

import com.github.bannirui.msb.mq.autoconfigure.MsbMQImportSelector;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/**
 * MQ场景启动器.
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({MsbMQImportSelector.class})
public @interface EnableMsbMQ {

}
