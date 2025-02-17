package com.github.bannirui.msb.hbase.metadata;

import java.lang.reflect.Field;

public class HColumnInfo {
    private byte[] family;
    private byte[] qualifier;
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
