package com.github.bannirui.msb.hbase.metadata;

import java.lang.reflect.Field;
import java.util.Map;

public class HBaseEntityMetadata {
    private String tableName;
    private byte[] tabName;
    private Class<?> tabClass;
    private Field rowKeyField;
    private Field versionField;
    private Map<String, HColumnInfo> hcolumnInfos;

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public byte[] getTabName() {
        return this.tabName;
    }

    public void setTabName(byte[] tabName) {
        this.tabName = tabName;
    }

    public Class<?> getTabClass() {
        return this.tabClass;
    }

    public void setTabClass(Class<?> tabClass) {
        this.tabClass = tabClass;
    }

    public Field getRowKeyField() {
        return this.rowKeyField;
    }

    public void setRowKeyField(Field rowKeyField) {
        this.rowKeyField = rowKeyField;
    }

    public Field getVersionField() {
        return this.versionField;
    }

    public void setVersionField(Field versionField) {
        this.versionField = versionField;
    }

    public Map<String, HColumnInfo> getHcolumnInfos() {
        return this.hcolumnInfos;
    }

    public void setHcolumnInfos(Map<String, HColumnInfo> hcolumnInfos) {
        this.hcolumnInfos = hcolumnInfos;
    }
}
