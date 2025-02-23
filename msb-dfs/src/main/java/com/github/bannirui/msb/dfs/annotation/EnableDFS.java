package com.github.bannirui.msb.dfs.annotation;

import com.github.bannirui.msb.dfs.autoconfig.EnableDfsImportSelector;
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
@Import({EnableDfsImportSelector.class})
public @interface EnableDFS {
}
