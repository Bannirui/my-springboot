package com.github.bannirui.msb.orm.shardingjdbc;

public interface IShardingAlgorithm {
    void setTableConfig(TableConfig tableConfig);

    SingleKeyDatabaseShardingAlgorithm<?> getDataBaseShardingAlgorithm();

    SingleKeyTableShardingAlgorithm<?> getTableShardingAlgorithm();

    default List<String> getTableNameList(String tableName, Long tableSize, String tableNameFormat) {
        return ShardingJdbcUtil.generationTableNames(tableName, tableSize, tableNameFormat);
    }
}
