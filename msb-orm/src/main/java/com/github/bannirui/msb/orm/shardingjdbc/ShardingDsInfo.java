package com.github.bannirui.msb.orm.shardingjdbc;

public class ShardingDsInfo {
    private Map<String, DataSource> dataSourceMap;
    private MasterSlaveRuleConfiguration masterSlaveRuleConfig;

    public ShardingDsInfo(Map<String, DataSource> dataSourceMap) {
        this.dataSourceMap = dataSourceMap;
    }

    public ShardingDsInfo(Map<String, DataSource> dataSourceMap, MasterSlaveRuleConfiguration masterSlaveRuleConfig) {
        this.dataSourceMap = dataSourceMap;
        this.masterSlaveRuleConfig = masterSlaveRuleConfig;
    }

    public Map<String, DataSource> getDataSourceMap() {
        return this.dataSourceMap;
    }

    public MasterSlaveRuleConfiguration getMasterSlaveRuleConfig() {
        return this.masterSlaveRuleConfig;
    }

    public boolean hasSlave() {
        return this.masterSlaveRuleConfig != null;
    }
}
