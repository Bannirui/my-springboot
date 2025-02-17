package com.github.bannirui.msb.hbase.config;

import java.util.List;
import java.util.Map;

public class HbasePutEntity {
    private byte[] tableName;
    private byte[] key;
    private Map<String, List<HbasePutEntity.PutInfo>> groupedPutInfos;
    private long version;

    public byte[] getTableName() {
        return this.tableName;
    }

    public void setTableName(byte[] tableName) {
        this.tableName = tableName;
    }

    public byte[] getKey() {
        return this.key;
    }

    public void setKey(byte[] key) {
        this.key = key;
    }

    public long getVersion() {
        return this.version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Map<String, List<PutInfo>> getGroupedPutInfos() {
        return this.groupedPutInfos;
    }

    public void setGroupedPutInfos(Map<String, List<HbasePutEntity.PutInfo>> groupedPutInfos) {
        this.groupedPutInfos = groupedPutInfos;
    }

    public static class PutInfo {
        private byte[] family;
        private byte[] qualifier;
        private byte[] value;

        public PutInfo(byte[] family, byte[] qualifier, byte[] value) {
            this.family = family;
            this.qualifier = qualifier;
            this.value = value;
        }

        public byte[] getFamily() {
            return this.family;
        }

        public void setFamily(byte[] family) {
            this.family = family;
        }

        public byte[] getQualifier() {
            return this.qualifier;
        }

        public void setQualifier(byte[] qualifier) {
            this.qualifier = qualifier;
        }

        public byte[] getValue() {
            return this.value;
        }

        public void setValue(byte[] value) {
            this.value = value;
        }
    }
}
