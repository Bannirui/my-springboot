package com.github.bannirui.msb.orm.shardingjdbc;

import com.dangdang.ddframe.rdb.sharding.api.strategy.database.SingleKeyDatabaseShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.SingleKeyTableShardingAlgorithm;
import com.github.bannirui.msb.orm.property.TableConfig;
import com.github.bannirui.msb.orm.util.ShardingJdbcUtil;
import java.util.List;

public interface IShardingAlgorithm {
    void setTableConfig(TableConfig tableConfig);

    SingleKeyDatabaseShardingAlgorithm<?> getDataBaseShardingAlgorithm();

    SingleKeyTableShardingAlgorithm<?> getTableShardingAlgorithm();

    default List<String> getTableNameList(String tableName, Long tableSize, String tableNameFormat) {
        return ShardingJdbcUtil.generationTableNames(tableName, tableSize, tableNameFormat);
    }
}
