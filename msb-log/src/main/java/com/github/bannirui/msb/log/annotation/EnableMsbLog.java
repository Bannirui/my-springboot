package com.github.bannirui.msb.log.annotation;

import com.github.bannirui.msb.log.autoconfigure.MsbLogImportSelector;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/**
 * 启用日志框架.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({MsbLogImportSelector.class})
public @interface EnableMsbLog {

}
