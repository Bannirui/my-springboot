package com.github.bannirui.msb.hbase.metadata;

import java.lang.reflect.Field;

/**
 * java实体映射的hbase列信息
 */
public class HColumnInfo {
    /**
     * 列所属列簇
     */
    private byte[] family;

    /**
     * hbase列
     */
    private byte[] qualifier;

    /**
     * java实体映射的hbase列
     */
    private Field field;

    public HColumnInfo(byte[] family, byte[] qualifier, Field field) {
        this.family = family;
        this.qualifier = qualifier;
        this.field = field;
    }

    public byte[] getFamily() {
        return this.family;
    }

    public byte[] getQualifier() {
        return this.qualifier;
    }

    public Field getField() {
        return this.field;
    }
}
