package com.github.bannirui.msb.hbase.annotation;

import com.github.bannirui.msb.hbase.codec.HbaseCellDataCodec;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HColumn {
    String name() default "";

    String family() default "info";

    Class<? extends HbaseCellDataCodec> codec() default HbaseCellDataCodec.class;
}
