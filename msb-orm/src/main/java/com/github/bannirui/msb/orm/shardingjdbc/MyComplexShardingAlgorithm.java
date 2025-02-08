package com.github.bannirui.msb.orm.shardingjdbc;

import com.dangdang.ddframe.rdb.sharding.api.ShardingValue;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.MultipleKeysDatabaseShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.MultipleKeysTableShardingAlgorithm;
import com.github.bannirui.msb.orm.property.TableConfig;
import com.github.bannirui.msb.orm.util.ShardingJdbcUtil;
import java.util.Collection;
import java.util.Collections;

public class MyComplexShardingAlgorithm implements MutiKeysShardingAlgorithm {
    private TableConfig tableConfig;
    private MultipleKeysDatabaseShardingAlgorithm multiKeysDatabaseShardingAlgorithm = new MyComplexShardingAlgorithm.MultiKeysDatabaseShardingAlgorithm();
    private MultipleKeysTableShardingAlgorithm multiKeysTableShardingAlgorithm = new MyComplexShardingAlgorithm.MultiKeysTableShardingAlgorithm();

    @Override
    public void setTableConfig(TableConfig tableConfig) {
        this.tableConfig = tableConfig;
    }

    @Override
    public MultipleKeysDatabaseShardingAlgorithm getMultipleKeysDatabaseShardingAlgorithm() {
        return this.multiKeysDatabaseShardingAlgorithm;
    }

    @Override
    public MultipleKeysTableShardingAlgorithm getMultipleKeysTableShardingAlgorithm() {
        return this.multiKeysTableShardingAlgorithm;
    }

    private class MultiKeysTableShardingAlgorithm implements MultipleKeysTableShardingAlgorithm {
        @Override
        public Collection<String> doSharding(Collection<String> availableTargetNames, Collection<com.dangdang.ddframe.rdb.sharding.api.ShardingValue<?>> shardingValues) {
            return Collections.singleton(ShardingJdbcUtil.getTableName(MyComplexShardingAlgorithm.this.tableConfig.getName(), MyComplexShardingAlgorithm.this.tableConfig.getSize(), MyComplexShardingAlgorithm.this.tableConfig));
        }
    }

    private class MultiKeysDatabaseShardingAlgorithm implements MultipleKeysDatabaseShardingAlgorithm {
        @Override
        public Collection<String> doSharding(Collection<String> availableTargetNames, Collection<ShardingValue<?>> shardingValues) {
            return Collections.singletonList("ds0000");
        }
    }
}
