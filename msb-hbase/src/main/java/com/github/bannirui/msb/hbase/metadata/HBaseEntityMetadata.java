package com.github.bannirui.msb.hbase.metadata;

import com.github.bannirui.msb.hbase.util.Bytes;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * hbase表跟java实体信息
 */
public class HBaseEntityMetadata {
    // hbase表名
    private String tableName;
    // hbase表名
    private byte[] tabName;
    // hbase实体对应的java entity
    private Class<?> tabClass;
    // hbase实体中哪个字段是rowkey rowkey是必需的
    private Field rowKeyField;
    // hbase实体中哪个字段是version
    private Field versionField;
    // hbase列 key是列簇#列
    private Map<String, HColumnInfo> hcolumnInfos;

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
        this.tabName = Bytes.toBytes(tableName);
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
