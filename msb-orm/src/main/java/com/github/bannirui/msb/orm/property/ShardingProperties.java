package com.github.bannirui.msb.orm.property;

import java.util.List;

public class ShardingProperties {
    private Integer defaultDSIndex;
    private String defaultDataSource;
    private List<TableConfig> tableConfigs;
    private List<MasterDsProperties> datasources;
    private boolean enableFusing = false;
    private List<FusingConfig> fusingConfigs;
    private String exceptionBlacks;
    private boolean useOptimizedTableRule;

    public Integer getDefaultDSIndex() {
        return this.defaultDSIndex;
    }

    public void setDefaultDSIndex(Integer defaultDSIndex) {
        this.defaultDSIndex = defaultDSIndex;
    }

    public String getDefaultDataSource() {
        return this.defaultDataSource;
    }

    public void setDefaultDataSource(String defaultDataSource) {
        this.defaultDataSource = defaultDataSource;
    }

    public List<TableConfig> getTableConfigs() {
        return this.tableConfigs;
    }

    public void setTableConfigs(List<TableConfig> tableConfigs) {
        this.tableConfigs = tableConfigs;
    }

    public List<MasterDsProperties> getDataSources() {
        return this.datasources;
    }

    public void setDataSources(List<MasterDsProperties> dataSources) {
        this.datasources = dataSources;
    }

    public List<FusingConfig> getFusingConfigs() {
        return this.fusingConfigs;
    }

    public void setFusingConfigs(List<FusingConfig> fusingConfigs) {
        this.fusingConfigs = fusingConfigs;
    }

    public String getExceptionBlacks() {
        return this.exceptionBlacks;
    }

    public void setExceptionBlacks(String exceptionBlacks) {
        this.exceptionBlacks = exceptionBlacks;
    }

    public boolean isEnableFusing() {
        return this.enableFusing;
    }

    public void setEnableFusing(boolean enableFusing) {
        this.enableFusing = enableFusing;
    }

    public boolean isUseOptimizedTableRule() {
        return this.useOptimizedTableRule;
    }

    public void setUseOptimizedTableRule(boolean useOptimizedTableRule) {
        this.useOptimizedTableRule = useOptimizedTableRule;
    }
}
