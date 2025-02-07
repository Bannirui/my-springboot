package com.github.bannirui.msb.orm.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.util.Collection;

public class MasterSlaveRuleConfiguration {
    private final String name;
    private final String masterDataSourceName;
    private final Collection<String> slaveDataSourceNames;

    public MasterSlaveRuleConfiguration(final String name, final String masterDataSourceName, final Collection<String> slaveDataSourceNames) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "Name is required.");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(masterDataSourceName), "MasterDataSourceName is required.");
        Preconditions.checkArgument(null != slaveDataSourceNames && !slaveDataSourceNames.isEmpty(), "SlaveDataSourceNames is required.");
        this.name = name;
        this.masterDataSourceName = masterDataSourceName;
        this.slaveDataSourceNames = slaveDataSourceNames;
    }

    public String getName() {
        return this.name;
    }

    public String getMasterDataSourceName() {
        return this.masterDataSourceName;
    }

    public Collection<String> getSlaveDataSourceNames() {
        return this.slaveDataSourceNames;
    }
}
