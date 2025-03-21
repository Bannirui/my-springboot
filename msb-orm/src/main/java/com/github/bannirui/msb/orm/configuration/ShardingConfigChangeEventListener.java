package com.github.bannirui.msb.orm.configuration;

import com.github.bannirui.msb.env.MsbEnvironmentMgr;
import com.github.bannirui.msb.event.DynamicConfigChangeSpringEvent;
import com.github.bannirui.msb.orm.property.MasterDsProperties;
import com.github.bannirui.msb.orm.property.ShardingConfigChange;
import com.github.bannirui.msb.orm.property.ShardingProperties;
import com.github.bannirui.msb.orm.property.TableConfig;
import com.github.bannirui.msb.orm.shardingjdbc.DynamicShardingDataSource;
import com.github.bannirui.msb.orm.shardingjdbc.ShardingDsInfo;
import com.github.bannirui.msb.orm.util.DataSourceHelp;
import com.github.bannirui.msb.orm.util.ReleaseDataSourceRunnable;
import com.github.bannirui.msb.orm.util.ShardingJdbcUtil;
import com.github.bannirui.msb.properties.ConfigChange;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.sql.DataSource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

public class ShardingConfigChangeEventListener implements ApplicationListener<DynamicConfigChangeSpringEvent>, ApplicationContextAware, EnvironmentAware {
    private static final Logger logger = LoggerFactory.getLogger(ShardingConfigChangeEventListener.class);
    @Value("${msb.datasource.dynamic.forceCloseWaitSeconds:300}")
    private int forceCloseWaitSeconds = 300;
    private Environment environment;
    private ApplicationContext context;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    @Override
    public synchronized void onApplicationEvent(DynamicConfigChangeSpringEvent changeEvent) {
        ConfigChange configChange = changeEvent.getConfigChange();
        Set<String> changedKeys = configChange.getChangedConfigKeys();
        if (changedKeys.stream().anyMatch((changedKey) -> changedKey.startsWith("sharding.fusingConfigs"))) {
            BindResult<ShardingProperties> bind = Binder.get(this.environment).bind("sharding", ShardingProperties.class);
            if (bind.isBound()) {
                ShardingJdbcUtil.initShardingFusing(bind.get());
            }
        }
        Boolean enable = this.environment.getProperty("datasource.dynamic.change.enabled", Boolean.class, false);
        if(!enable) return;
        ShardingConfigChange shardingConfigChange = this.resolveConfigForShardingJdbc(changeEvent);
        if(Objects.isNull(shardingConfigChange)) return;
        try {
            DynamicShardingDataSource shardingDataSource = this.context.getBean("shardingDataSource", DynamicShardingDataSource.class);
            Map<String, MasterSlaveRuleConfiguration> masterSlaveRuleConfigs = shardingDataSource.getMasterSlaveRuleConfigs();
            Map<String, DataSource> oldDataSourceMap = shardingDataSource.getDataSourceMap();
            logger.info("Sharding-dataSource Updating...");
            ShardingConfigChangeEventListener.Result result = analyseUpdatedDataSources(shardingConfigChange, masterSlaveRuleConfigs, oldDataSourceMap);
            int defaultIndex = shardingConfigChange.getDefaultDSIndex() != null ? shardingConfigChange.getDefaultDSIndex() : 0;
            String defaultDSName = ShardingJdbcUtil.generationCurrentDataBaseName((long)defaultIndex);
            shardingDataSource.updateDataSource(defaultDSName, result.getNewDataSourceMap(), shardingConfigChange.getTableConfigs(), masterSlaveRuleConfigs);
            logger.info("Sharding-dataSource Update completed.");
            Set<DataSource> dsList = result.getReleaseDS();
            if(CollectionUtils.isNotEmpty(dsList)) {
                dsList.forEach((ds) -> DataSourceHelp.asyncRelease(new ReleaseDataSourceRunnable(ds, this.forceCloseWaitSeconds)));
            }
        } catch (NoSuchBeanDefinitionException e) {
            logger.error("没有找到配置名为[{shardingDataSource}]的数据源Bean信息,无法更新配置", e);
        }
    }

    private ShardingConfigChange resolveConfigForShardingJdbc(DynamicConfigChangeSpringEvent changeEvent) {
        Map<Integer, MasterDsProperties> changedConfigMap = new HashMap<>();
        ConfigChange configChange = changeEvent.getConfigChange();
        Set<String> changedKeys = configChange.getChangedConfigKeys();
        for (String key : changedKeys) {
            if (!MsbEnvironmentMgr.SHARDING_PREFIX_REGULAR.matcher(key).matches()) continue;
            int index = Integer.parseInt(key.substring(key.indexOf("[") + 1, key.indexOf("]")));
            if (!changedConfigMap.containsKey(index)) {
                MasterDsProperties dsProperties = ShardingJdbcUtil.loadShardingDbConfig(this.environment, index);
                dsProperties.setName(ShardingJdbcUtil.generationCurrentDataBaseName((long)index));
                changedConfigMap.put(index, dsProperties);
            }
        }
        boolean tableConfigChanged = changedKeys.stream().anyMatch((key_) -> MsbEnvironmentMgr.SHARDING_TABLE_PREFIX_REGULAR.matcher(key_).matches());
        boolean defDsIndecChanged = changedKeys.contains("sharding.defaultDSIndex");
        if (!tableConfigChanged && !defDsIndecChanged && MapUtils.isEmpty(changedConfigMap)) {
            return null;
        }
        List<TableConfig> tableConfigs = ShardingJdbcUtil.loadShardingTableConfig(this.environment);
        Integer defDSIndex = this.environment.getProperty("sharding.defaultDSIndex", Integer.class);
        return new ShardingConfigChange(changedConfigMap, tableConfigs, defDSIndex);
    }

    public static ShardingConfigChangeEventListener.Result analyseUpdatedDataSources(ShardingConfigChange shardingConfigChange, Map<String, MasterSlaveRuleConfiguration> masterSlaveRuleConfigs, Map<String, DataSource> oldDataSourceMap) {
        return (new ShardingConfigChangeEventListener.Result(shardingConfigChange, masterSlaveRuleConfigs, oldDataSourceMap)).invoke();
    }

    public static class Result {
        private ShardingConfigChange shardingConfigChange;
        private Map<String, MasterSlaveRuleConfiguration> masterSlaveRuleConfigs;
        private Map<String, DataSource> newDataSourceMap;
        private Set<DataSource> releaseDS;

        Result(ShardingConfigChange shardingConfigChange, Map<String, MasterSlaveRuleConfiguration> masterSlaveRuleConfigs, Map<String, DataSource> oldDataSourceMap) {
            this.shardingConfigChange = shardingConfigChange;
            this.masterSlaveRuleConfigs = masterSlaveRuleConfigs;
            this.newDataSourceMap = new HashMap<>(oldDataSourceMap);
        }

        public Map<String, DataSource> getNewDataSourceMap() {
            return this.newDataSourceMap;
        }

        public Set<DataSource> getReleaseDS() {
            return this.releaseDS;
        }

        private ShardingConfigChangeEventListener.Result invoke() {
            this.releaseDS = new HashSet<>();
            Map<Integer, MasterDsProperties> changedDataSources = this.shardingConfigChange.getChangedDataSources();
            for (MasterDsProperties dsProperties : changedDataSources.values()) {
                DataSource removeDs = this.newDataSourceMap.remove(dsProperties.getName());
                if (removeDs != null) {
                    this.releaseDS.add(removeDs);
                }
                MasterSlaveRuleConfiguration masterSlaveRuleConfig = this.masterSlaveRuleConfigs.remove(dsProperties.getName());
                if (masterSlaveRuleConfig != null) {
                    this.releaseDS.add(this.newDataSourceMap.remove(masterSlaveRuleConfig.getMasterDataSourceName()));
                    Collection<String> slaveDataSourceNames = masterSlaveRuleConfig.getSlaveDataSourceNames();
                    slaveDataSourceNames.forEach(name -> this.releaseDS.add(this.newDataSourceMap.remove(name)));
                }
                if (Objects.nonNull(dsProperties.getHikari()) && StringUtils.isNotBlank(dsProperties.getHikari().getJdbcUrl())) {
                    ShardingDsInfo shardingDsInfo = ShardingJdbcUtil.createShardingDataSource(dsProperties);
                    if (shardingDsInfo.hasSlave()) {
                        this.masterSlaveRuleConfigs.put(dsProperties.getName(), shardingDsInfo.getMasterSlaveRuleConfig());
                    }
                    this.newDataSourceMap.putAll(shardingDsInfo.getDataSourceMap());
                }
            }
            return this;
        }
    }
}
