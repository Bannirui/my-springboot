package com.github.bannirui.msb.orm.shardingjdbc;

import com.dangdang.ddframe.rdb.sharding.api.ShardingDataSourceFactory;
import com.dangdang.ddframe.rdb.sharding.api.rule.DataSourceRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.DatabaseShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingDataSource;
import com.github.bannirui.msb.ex.FrameworkException;
import com.github.bannirui.msb.orm.configuration.MasterSlaveRuleConfiguration;
import com.github.bannirui.msb.orm.property.TableConfig;
import com.github.bannirui.msb.orm.squence.AbstractLifecycle;
import com.github.bannirui.msb.orm.util.DataSourceHelp;
import com.github.bannirui.msb.orm.util.ShardingJdbcUtil;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

public class SimpleShardingDataSource extends AbstractLifecycle implements DynamicShardingDataSource, DataSource {
    private Map<String, DataSource> dataSourceMap;
    private String defaultDataSource;
    private List<TableConfig> tableConfigs;
    private Map<String, MasterSlaveRuleConfiguration> masterSlaveRuleConfigs;
    private volatile DataSource dataSource;
    private boolean useOptimizedTableRule;

    @Override
    public void doInit() {
        DataSourceRule dataSourceRule = new DataSourceRule(this.dataSourceMap, this.defaultDataSource);
        if (this.tableConfigs == null) {
            this.tableConfigs = new ArrayList<>();
        }
        List<TableRule> tableRuleList = new ArrayList<>(this.tableConfigs.size());
        for (TableConfig config : this.tableConfigs) {
            IShardingAlgorithm simpleShardingAlgorithm = null;
            MutiKeysShardingAlgorithm multiKeysShardingAlgorithm = null;
            if (config.getAlgorithm() == null) {
                simpleShardingAlgorithm = new SimpleShardingAlgorithm();
            } else {
                Class multiKeysShardingAlgorithmClass;
                if (config.getShardingColumn().contains(",")) {
                    try {
                        multiKeysShardingAlgorithmClass = ClassUtils.forName(config.getAlgorithm(), MutiKeysShardingAlgorithm.class.getClassLoader());
                        multiKeysShardingAlgorithm = (MutiKeysShardingAlgorithm)multiKeysShardingAlgorithmClass.newInstance();
                    } catch (Exception e) {
                        throw FrameworkException.getInstance(e, "加载ComplexKeysShardingAlgorithmClass异常{0}", config.getAlgorithm());
                    }
                } else {
                    try {
                        multiKeysShardingAlgorithmClass = ClassUtils.forName(config.getAlgorithm(), IShardingAlgorithm.class.getClassLoader());
                        simpleShardingAlgorithm = (IShardingAlgorithm)multiKeysShardingAlgorithmClass.newInstance();
                    } catch (Exception e) {
                        throw FrameworkException.getInstance(e, "加载ShardingAlgorithmClass异常{0}", config.getAlgorithm());
                    }
                }
            }
            List<String> tableNames = ShardingJdbcUtil.getTableNameList(config.getName(), config.getSize(), config.getFormat());
            TableRule.TableRuleBuilder tableRuleBuilder = TableRule.builder(config.getName())
                .actualTables(tableNames)
                .dataSourceRule(dataSourceRule)
                .databaseShardingStrategy(this.getDatabaseShardingStrategy(config, simpleShardingAlgorithm, multiKeysShardingAlgorithm))
                .tableShardingStrategy(this.getTableShardingStrategy(config, simpleShardingAlgorithm, multiKeysShardingAlgorithm));
            if (this.useOptimizedTableRule) {
                // TODO: 2025/2/7
                // tableRuleBuilder.useOptimize(true);
            }
            TableRule tableRule = tableRuleBuilder.build();
            tableRuleList.add(tableRule);
        }
        ShardingRule shardingRule = ShardingRule.builder().dataSourceRule(dataSourceRule).tableRules(tableRuleList).build();
        this.dataSource = ShardingDataSourceFactory.createDataSource(shardingRule);
    }

    private DatabaseShardingStrategy getDatabaseShardingStrategy(TableConfig config, IShardingAlgorithm simpleShardingAlgorithm, MutiKeysShardingAlgorithm complexKeysShardingAlgorithm) {
        if (simpleShardingAlgorithm != null) {
            simpleShardingAlgorithm.setTableConfig(config);
            return new DatabaseShardingStrategy(config.getShardingColumn(), simpleShardingAlgorithm.getDataBaseShardingAlgorithm());
        } else if (complexKeysShardingAlgorithm != null) {
            complexKeysShardingAlgorithm.setTableConfig(config);
            Collection<String> shardingColumns = (Collection<String>) CollectionUtils.arrayToList(config.getShardingColumn().split(","));
            return new DatabaseShardingStrategy(shardingColumns, complexKeysShardingAlgorithm.getMultipleKeysDatabaseShardingAlgorithm());
        } else {
            throw new FrameworkException("Sharding-JDBC", "加载库分片策略异常");
        }
    }

    private TableShardingStrategy getTableShardingStrategy(TableConfig config, IShardingAlgorithm simpleShardingAlgorithm, MutiKeysShardingAlgorithm complexKeysShardingAlgorithm) {
        if (simpleShardingAlgorithm != null) {
            simpleShardingAlgorithm.setTableConfig(config);
            return new TableShardingStrategy(config.getShardingColumn(), simpleShardingAlgorithm.getTableShardingAlgorithm());
        } else if (complexKeysShardingAlgorithm != null) {
            complexKeysShardingAlgorithm.setTableConfig(config);
            Collection<String> shardingColumns = (Collection<String>) CollectionUtils.arrayToList(config.getShardingColumn().split(","));
            return new TableShardingStrategy(shardingColumns, complexKeysShardingAlgorithm.getMultipleKeysTableShardingAlgorithm());
        } else {
            throw new FrameworkException("Sharding-JDBC", "加载表分片策略异常");
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return this.dataSource.getConnection(username, password);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return this.dataSource.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return this.dataSource.isWrapperFor(iface);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return this.dataSource.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        this.dataSource.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        this.dataSource.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return this.dataSource.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return this.dataSource.getParentLogger();
    }

    public void shutdown() {
        if (this.dataSource instanceof ShardingDataSource) {
            ((ShardingDataSource)this.dataSource).shutdown();
        }
    }

    public List<TableConfig> getTableConfigs() {
        return this.tableConfigs;
    }

    public void setTableConfigs(List<TableConfig> tableConfigs) {
        this.tableConfigs = tableConfigs;
    }

    @Override
    public Map<String, DataSource> getDataSourceMap() {
        return this.dataSourceMap;
    }

    public void setMasterSlaveRuleConfigs(Map<String, MasterSlaveRuleConfiguration> masterSlaveRuleConfigs) {
        this.masterSlaveRuleConfigs = masterSlaveRuleConfigs;
    }

    public void setUseOptimizedTableRule(boolean useOptimizedTableRule) {
        this.useOptimizedTableRule = useOptimizedTableRule;
    }

    @Override
    public Map<String, MasterSlaveRuleConfiguration> getMasterSlaveRuleConfigs() {
        return this.masterSlaveRuleConfigs;
    }

    @Override
    public void updateDataSource(String defaultDSName, Map<String, DataSource> newDataSourceMap, List<TableConfig> tableConfigs, Map<String, MasterSlaveRuleConfiguration> masterSlaveRuleConfigs) {
        this.setDefaultDataSource(defaultDSName);
        this.setDataSourceMap(newDataSourceMap);
        this.setTableConfigs(tableConfigs);
        this.setMasterSlaveRuleConfigs(masterSlaveRuleConfigs);
        this.doInit();
    }

    public void setDataSourceMap(Map<String, DataSource> dataSourceMap) {
        this.dataSourceMap = dataSourceMap;
    }

    public String getDefaultDataSource() {
        return this.defaultDataSource;
    }

    public void setDefaultDataSource(String defaultDataSource) {
        this.defaultDataSource = defaultDataSource;
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }

    @Override
    public void destroy() {
        super.destroy();
        DataSourceHelp.shutdown();
    }

    @Override
    public void close() throws IOException {
        try {
            this.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
