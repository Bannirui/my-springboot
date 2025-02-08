package com.github.bannirui.msb.orm.annotation;

import com.github.bannirui.msb.orm.autoconfig.EnableMyBatisImportSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用mybatis
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({EnableMyBatisImportSelector.class})
public @interface EnableMyBatis {
}
