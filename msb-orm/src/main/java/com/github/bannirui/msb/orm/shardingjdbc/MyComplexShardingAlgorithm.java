package com.github.bannirui.msb.orm.shardingjdbc;

public class MyComplexShardingAlgorithm implements MutiKeysShardingAlgorithm {
    private TableConfig tableConfig;
    private MultipleKeysDatabaseShardingAlgorithm multiKeysDatabaseShardingAlgorithm = new MyComplexShardingAlgorithm.MultiKeysDatabaseShardingAlgorithm();
    private MultipleKeysTableShardingAlgorithm multiKeysTableShardingAlgorithm = new MyComplexShardingAlgorithm.MultiKeysTableShardingAlgorithm();

    public MyComplexShardingAlgorithm() {
    }

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
        private MultiKeysTableShardingAlgorithm() {
        }

        public Collection<String> doSharding(Collection<String> availableTargetNames, Collection<ShardingValue<?>> shardingValues) {
            return Collections.singleton(ShardingJdbcUtil.getTableName(MyComplexShardingAlgorithm.this.tableConfig.getName(), MyComplexShardingAlgorithm.this.tableConfig.getSize(), MyComplexShardingAlgorithm.this.tableConfig));
        }
    }

    private class MultiKeysDatabaseShardingAlgorithm implements MultipleKeysDatabaseShardingAlgorithm {
        private MultiKeysDatabaseShardingAlgorithm() {
        }

        public Collection<String> doSharding(Collection<String> availableTargetNames, Collection<ShardingValue<?>> shardingValues) {
            return Collections.singletonList("ds0000");
        }
    }
}
