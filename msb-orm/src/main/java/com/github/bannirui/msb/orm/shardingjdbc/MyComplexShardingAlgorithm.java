package com.github.bannirui.msb.orm.shardingjdbc;

import com.github.bannirui.msb.orm.property.TableConfig;
import com.github.bannirui.msb.orm.util.ShardingJdbcUtil;
import org.apache.shardingsphere.api.sharding.ShardingValue;

import java.util.Collection;
import java.util.Collections;

public class MyComplexShardingAlgorithm implements MutiKeysShardingAlgorithm {
    private TableConfig tableConfig;
    private MultipleKeysDatabaseShardingAlgorithm multiKeysDatabaseShardingAlgorithm = new MyComplexShardingAlgorithm.MultiKeysDatabaseShardingAlgorithm();
    private MultipleKeysTableShardingAlgorithm multiKeysTableShardingAlgorithm = new MyComplexShardingAlgorithm.MultiKeysTableShardingAlgorithm();

    public void setTableConfig(TableConfig tableConfig) {
        this.tableConfig = tableConfig;
    }

    public MultipleKeysDatabaseShardingAlgorithm getMultipleKeysDatabaseShardingAlgorithm() {
        return this.multiKeysDatabaseShardingAlgorithm;
    }

    public MultipleKeysTableShardingAlgorithm getMultipleKeysTableShardingAlgorithm() {
        return this.multiKeysTableShardingAlgorithm;
    }

    private class MultiKeysTableShardingAlgorithm implements MultipleKeysTableShardingAlgorithm {
        public Collection<String> doSharding(Collection<String> availableTargetNames, Collection<ShardingValue<?>> shardingValues) {
            return Collections.singleton(ShardingJdbcUtil.getTableName(MyComplexShardingAlgorithm.this.tableConfig.getName(), MyComplexShardingAlgorithm.this.tableConfig.getSize(), MyComplexShardingAlgorithm.this.tableConfig));
        }
    }

    private class MultiKeysDatabaseShardingAlgorithm implements MultipleKeysDatabaseShardingAlgorithm {
        public Collection<String> doSharding(Collection<String> availableTargetNames, Collection<ShardingValue<?>> shardingValues) {
            return Collections.singletonList("ds0000");
        }
    }
}
