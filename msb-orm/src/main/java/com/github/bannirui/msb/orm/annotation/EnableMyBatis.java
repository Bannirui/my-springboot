package com.github.bannirui.msb.orm.annotation;

import com.github.bannirui.msb.orm.autoconfig.EnableMyBatisImportSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用mybatis
 * @see EnableMyBatisPlus mybatis-plus完全兼容mybatis
 */
@Deprecated
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({EnableMyBatisImportSelector.class})
public @interface EnableMyBatis {
}
