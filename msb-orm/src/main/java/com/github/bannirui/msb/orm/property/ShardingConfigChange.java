package com.github.bannirui.msb.orm.property;

public class ShardingConfigChange {
    private Map<Integer, MasterDsProperties> changedDataSources;
    private Integer defaultDSIndex;
    private List<TableConfig> tableConfigs;

    public ShardingConfigChange(Map<Integer, MasterDsProperties> changedDataSources, List<TableConfig> tableConfigs, Integer defaultDSIndex) {
        this.changedDataSources = changedDataSources;
        this.tableConfigs = tableConfigs;
        this.defaultDSIndex = defaultDSIndex;
    }

    public Map<Integer, MasterDsProperties> getChangedDataSources() {
        return this.changedDataSources;
    }

    public List<TableConfig> getTableConfigs() {
        return this.tableConfigs;
    }

    public Integer getDefaultDSIndex() {
        return this.defaultDSIndex;
    }
}
