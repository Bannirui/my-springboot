package com.github.bannirui.msb.orm.shardingjdbc;

public interface MutiKeysShardingAlgorithm {
    void setTableConfig(TableConfig tableConfig);

    MultipleKeysDatabaseShardingAlgorithm getMultipleKeysDatabaseShardingAlgorithm();

    MultipleKeysTableShardingAlgorithm getMultipleKeysTableShardingAlgorithm();
}
