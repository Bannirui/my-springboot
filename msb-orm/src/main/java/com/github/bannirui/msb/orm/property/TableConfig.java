package com.github.bannirui.msb.orm.property;

public class TableConfig {
    private String name;
    private String shardingColumn;
    private Long size;
    private String strategy;
    private String format;
    private String algorithm;

    public String getAlgorithm() {
        return this.algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getStrategy() {
        return this.strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public String getFormat() {
        return this.format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShardingColumn() {
        return this.shardingColumn;
    }

    public void setShardingColumn(String shardingColumn) {
        this.shardingColumn = shardingColumn;
    }

    public Long getSize() {
        return this.size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String toString() {
        return "TableConfig{name='" + this.name + '\'' + ", shardingColumn='" + this.shardingColumn + '\'' + ", size=" + this.size + ", strategy='" + this.strategy + '\'' + ", format='" + this.format + '\'' + ", algorithm='" + this.algorithm + '\'' + '}';
    }
}