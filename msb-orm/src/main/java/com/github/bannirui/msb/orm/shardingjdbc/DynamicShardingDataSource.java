package com.github.bannirui.msb.orm.shardingjdbc;

public interface DynamicShardingDataSource extends Closeable {
    Map<String, DataSource> getDataSourceMap();

    Map<String, MasterSlaveRuleConfiguration> getMasterSlaveRuleConfigs();

    void updateDataSource(String defaultDSName, Map<String, DataSource> newDataSourceMap, List<TableConfig> tableConfigs, Map<String, MasterSlaveRuleConfiguration> masterSlaveRuleConfigs);
}
