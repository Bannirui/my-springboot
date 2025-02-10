package com.github.bannirui.msb.orm.annotation;

import com.github.bannirui.msb.orm.autoconfig.EnableMyBatisPlusImportSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用mybatis-plus
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({EnableMyBatisPlusImportSelector.class})
public @interface EnableMyBatisPlus {
}
