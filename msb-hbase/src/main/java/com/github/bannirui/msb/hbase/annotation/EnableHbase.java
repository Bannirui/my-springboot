package com.github.bannirui.msb.hbase.annotation;

import com.github.bannirui.msb.hbase.autoconfig.EnableHbaseImportSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * hbase场景启动器
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({EnableHbaseImportSelector.class})
public @interface EnableHbase {
}
