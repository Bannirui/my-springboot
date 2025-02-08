package com.github.bannirui.msb.orm.shardingjdbc;

import com.dangdang.ddframe.rdb.sharding.api.strategy.database.MultipleKeysDatabaseShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.MultipleKeysTableShardingAlgorithm;
import com.github.bannirui.msb.orm.property.TableConfig;

public interface MutiKeysShardingAlgorithm {
    void setTableConfig(TableConfig tableConfig);

    MultipleKeysDatabaseShardingAlgorithm getMultipleKeysDatabaseShardingAlgorithm();

    MultipleKeysTableShardingAlgorithm getMultipleKeysTableShardingAlgorithm();
}
