package com.github.bannirui.msb.orm.util;

import com.alibaba.fastjson.JSON;
import com.dangdang.ddframe.rdb.sharding.jdbc.MasterSlaveDataSource;
import com.github.bannirui.msb.ex.FrameworkException;
import com.github.bannirui.msb.orm.configuration.MasterSlaveRuleConfiguration;
import com.github.bannirui.msb.orm.property.MasterDsProperties;
import com.github.bannirui.msb.orm.property.ShardingProperties;
import com.github.bannirui.msb.orm.property.SlaveDsProperties;
import com.github.bannirui.msb.orm.property.TableConfig;
import com.github.bannirui.msb.orm.shardingjdbc.ShardingDsInfo;
import com.google.common.base.Preconditions;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.sql.DataSource;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

public class ShardingJdbcUtil {
    public static final String TABLE_FORMAT = "_%04d";
    public static final String DATA_SOURCE_NAME_FORMAT = "ds%04d";
    public static final String SLAVE = "-slave";

    public static String generationCurrentDataBaseName(Long index) {
        return String.format("ds%04d", index);
    }

    public static List<String> generationTableNames(String tableName, Long tableSize, String format) {
        if (format == null) {
            format = "_%04d";
        }
        List<String> tableNames = new ArrayList<>();
        for(int i = 0; (long)i < tableSize; ++i) {
            tableNames.add(tableName + String.format(format, i));
        }
        return tableNames;
    }

    public static String generationCurrentTableName(String tableName, Long tableNumber, String format) {
        if (format == null) {
            format = "_%04d";
        }
        return tableName + String.format(format, tableNumber);
    }

    public static MasterDsProperties loadShardingDbConfig(Environment env, int index) {
        return Binder.get(env).bind(String.format("sharding.datasources[%s]", index), MasterDsProperties.class).orElse(new MasterDsProperties());
    }

    public static List<TableConfig> loadShardingTableConfig(Environment env) {
        return Binder.get(env).bind("sharding.table-configs", Bindable.listOf(TableConfig.class)).orElse(new ArrayList<>());
    }

    public static List<HikariDataSource> createDataSources(MasterDsProperties config) {
        HikariConfig masterHikariConfig = config.getHikari();
        masterHikariConfig.setPoolName(DataSourceHelp.generatePoolName(masterHikariConfig.getPoolName()));
        masterHikariConfig.setPassword(DBPasswordDecoder.decode(masterHikariConfig.getPassword()));
        List<HikariDataSource> dataSources = new ArrayList<>(2);
        dataSources.add(new HikariDataSource(masterHikariConfig));
        HikariConfig slaveConfig = completeSlaveConfig(masterHikariConfig, config.getSlave());
        if (Objects.nonNull(slaveConfig)) {
            dataSources.add(new HikariDataSource(slaveConfig));
        }
        return dataSources;
    }

    private static HikariConfig completeSlaveConfig(HikariConfig hikariConfig, SlaveDsProperties slave) {
        if (slave != null) {
            HikariConfig slaveConfig = slave.getHikari();
            if (slaveConfig.getPassword() != null) {
                slaveConfig.setPassword(DBPasswordDecoder.decode(slaveConfig.getPassword()));
            }
            if (slaveConfig.getDriverClassName() == null) {
                slaveConfig.setDriverClassName(hikariConfig.getDriverClassName());
            }
            slaveConfig.setPoolName(hikariConfig.getPoolName() + "-slave");
            return slaveConfig;
        } else {
            return null;
        }
    }

    public static ShardingDsInfo createShardingDataSource(MasterDsProperties config) {
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        List<HikariDataSource> dataSourceList = createDataSources(config);
        HikariDataSource masterDs = dataSourceList.get(0);
        if (dataSourceList.size() <= 1) {
            dataSourceMap.put(config.getName(), masterDs);
            return new ShardingDsInfo(dataSourceMap);
        } else {
            List<String> slaveDsNames = new ArrayList<>();
            List<DataSource> slaves = new ArrayList<>();
            for(int i = 1; i < dataSourceList.size(); ++i) {
                HikariDataSource slave = dataSourceList.get(i);
                slaveDsNames.add(slave.getPoolName());
                slaves.add(slave);
            }
            MasterSlaveRuleConfiguration masterSlaveRuleConfig = new MasterSlaveRuleConfiguration(config.getName(), masterDs.getPoolName(), slaveDsNames);
            MasterSlaveDataSource masterSlaveDataSource = new MasterSlaveDataSource("", masterDs, slaves);
            dataSourceMap.put(config.getName(), masterSlaveDataSource);
            return new ShardingDsInfo(dataSourceMap, masterSlaveRuleConfig);
        }
    }

    public static String getDbName(Object obj, Integer dbSize, TableConfig tableConfig) {
        if (dbSize == 1) {
            return generationCurrentDataBaseName(0L);
        } else {
            Long value = null;
            if (tableConfig.getStrategy() != null && !tableConfig.getStrategy().equalsIgnoreCase("mod")) {
                if (!tableConfig.getStrategy().equalsIgnoreCase("hash")) {
                    throw FrameworkException.getInstance("sharding jdbc 策略设置非法:{}", tableConfig.getStrategy());
                }
                int hashcode = obj.hashCode();
                if (hashcode < 0) {
                    hashcode = Math.abs(hashcode);
                }
                value = (long)hashcode;
            } else {
                value = Math.abs(Long.parseLong(obj.toString()));
            }
            Long tableSize = tableConfig.getSize();
            long size = (tableSize - tableSize / (long)dbSize) / (long)(dbSize - 1);
            size = tableSize % size == 0L ? size : size + 1L;
            Long ds = value % tableSize / size;
            return generationCurrentDataBaseName(ds);
        }
    }

    public static String getTableName(String logicTableName, Object number, TableConfig tableConfig) {
        Long tableSize = tableConfig.getSize();
        Long value = null;
        if (tableConfig.getStrategy() != null && !tableConfig.getStrategy().equalsIgnoreCase("mod")) {
            if (!tableConfig.getStrategy().equalsIgnoreCase("hash")) {
                throw FrameworkException.getInstance("sharding jdbc 策略设置非法:{}", JSON.toJSONString(tableConfig.getStrategy()));
            }
            int hashcode = number.hashCode();
            if (hashcode < 0) {
                hashcode = Math.abs(hashcode);
            }
            value = (long)hashcode;
        } else {
            value = Math.abs(Long.parseLong(number.toString()));
        }
        Long moduloValue = value % tableSize;
        return generationCurrentTableName(logicTableName, moduloValue, tableConfig.getFormat());
    }

    public static String getActualDataNodes(long size, TableConfig config) {
        Preconditions.checkArgument(config.getName() != null && config.getSize() >= 0L, "tableConfig 配置非法:", new Object[]{config});
        return config.getFormat() != null ? "ds000${0.." + (size - 1L) + "}." + config.getName() + config.getFormat() + "${0.." + (config.getSize() - 1L) + "}" : "ds000${0.." + (size - 1L) + "}." + config.getName() + "_%04d" + "${0.." + (config.getSize() - 1L) + "}";
    }

    public static List<String> getDbNameList(Integer dbSize) {
        List<String> dbNames = new ArrayList<>();
        for(int i = 0; i < dbSize; ++i) {
            dbNames.add(String.format("ds%04d", i));
        }
        return dbNames;
    }

    public static List<String> getTableNameList(String tableName, Long tableSize, String tableNameFormat) {
        return generationTableNames(tableName, tableSize, tableNameFormat);
    }

    public static void initShardingFusing(ShardingProperties shardingProperties) {
        // TODO: 2025/2/8
    //     if (shardingProperties.isEnableFusing()) {
    //         Preconditions.checkArgument(CollectionUtils.isNotEmpty(shardingProperties.getFusingConfigs()), "您开启了sharding分库熔断功能，尚未配置规则。");
    //         List<ShardingDegradeRule> shardingDegradeRules = new ArrayList<>();
    //         List<FusingConfig> fusingConfigs = shardingProperties.getFusingConfigs();
    //         List<String> dbNames = getDbNameList(shardingProperties.getDataSources().size());
    //         Iterator var4 = dbNames.iterator();
    //         while(var4.hasNext()) {
    //             String resourceKey = (String)var4.next();
    //             Iterator var6 = fusingConfigs.iterator();
    //             while(var6.hasNext()) {
    //                 FusingConfig fusingConfig = (FusingConfig)var6.next();
    //                 if (fusingConfig.isEnable()) {
    //                     ShardingDegradeRule rule = new ShardingDegradeRule("SHARDING_DEGRADE_RULE_GROUP_ID", resourceKey);
    //                     rule.setGrade(fusingConfig.getGrade());
    //                     rule.setCount(fusingConfig.getCount());
    //                     rule.setTimeWindow(fusingConfig.getTimeWindow());
    //                     rule.setMinRequestAmount(fusingConfig.getMinRequestAmount());
    //                     rule.setRtSlowRequestAmount(fusingConfig.getRtSlowRequestAmount());
    //                     shardingDegradeRules.add(rule);
    //                 }
    //             }
    //         }
    //         ShardingFusingUtil.initLoadRules("SHARDING_DEGRADE_RULE_GROUP_ID", shardingDegradeRules);
    //         ShardingFusingUtil.setExceptionBlacks(shardingProperties.getExceptionBlacks() == null ? "QueryTimeoutException, SQLTimeoutException, MySQLTimeoutException" : shardingProperties.getExceptionBlacks());
    //     }
    }
}
