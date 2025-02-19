package com.github.bannirui.msb.hbase.annotation;

import com.github.bannirui.msb.hbase.codec.HbaseCellDataCodec;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注解在java实体指定hbase列信息
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HColumn {
    /**
     * 指定hbase列名 不指定就用java实体成员字段名作为列名
     */
    String name() default "";

    /**
     * 指定hbase列的列簇
     */
    String family() default "info";

    Class<? extends HbaseCellDataCodec> codec() default HbaseCellDataCodec.class;
}
