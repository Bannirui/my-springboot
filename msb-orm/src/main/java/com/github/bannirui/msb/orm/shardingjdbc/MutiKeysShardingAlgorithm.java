package com.github.bannirui.msb.orm.shardingjdbc;

import com.github.bannirui.msb.orm.property.TableConfig;

public interface MutiKeysShardingAlgorithm {
    void setTableConfig(TableConfig tableConfig);

    MultipleKeysDatabaseShardingAlgorithm getMultipleKeysDatabaseShardingAlgorithm();

    MultipleKeysTableShardingAlgorithm getMultipleKeysTableShardingAlgorithm();
}
