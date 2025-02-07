package com.github.bannirui.msb.orm.shardingjdbc;

import com.github.bannirui.msb.orm.configuration.MasterSlaveRuleConfiguration;
import com.github.bannirui.msb.orm.property.TableConfig;

import javax.sql.DataSource;
import java.io.Closeable;
import java.util.List;
import java.util.Map;

public interface DynamicShardingDataSource extends Closeable {
    Map<String, DataSource> getDataSourceMap();

    Map<String, MasterSlaveRuleConfiguration> getMasterSlaveRuleConfigs();

    void updateDataSource(String defaultDSName, Map<String, DataSource> newDataSourceMap, List<TableConfig> tableConfigs, Map<String, MasterSlaveRuleConfiguration> masterSlaveRuleConfigs);
}
