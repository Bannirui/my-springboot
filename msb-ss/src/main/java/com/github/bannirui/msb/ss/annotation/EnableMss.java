package com.github.bannirui.msb.ss.annotation;

import com.github.bannirui.msb.ss.autoconfig.EnableMssImportSelector;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/**
 * 定时任务场景启动器
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({EnableMssImportSelector.class})
public @interface EnableMss {
    String packageName() default "com.zto";
}
