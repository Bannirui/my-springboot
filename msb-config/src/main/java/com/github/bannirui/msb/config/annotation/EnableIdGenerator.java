package com.github.bannirui.msb.config.annotation;

import com.github.bannirui.msb.config.EnableIdGeneratorImportSelector;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EnableMsbConfig
@Import({EnableIdGeneratorImportSelector.class})
public @interface EnableIdGenerator {
}
