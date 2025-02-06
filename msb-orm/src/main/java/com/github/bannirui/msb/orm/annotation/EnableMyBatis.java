package com.github.bannirui.msb.orm.annotation;

import com.github.bannirui.msb.orm.autoconfig.EnableMyBatisImportSelector;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({EnableMyBatisImportSelector.class})
public @interface EnableMyBatis {
}
