package com.github.bannirui.msb.orm.configuration;

public class ShardingConfigChangeEventListener implements ApplicationListener<DynamicConfigChangeSpringEvent>, ApplicationContextAware, EnvironmentAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShardingConfigChangeEventListener.class);
    @Value("${titans.datasource.dynamic.forceCloseWaitSeconds:300}")
    private int forceCloseWaitSeconds = 300;
    private Environment environment;
    private ApplicationContext context;

    public ShardingConfigChangeEventListener() {
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    public synchronized void onApplicationEvent(DynamicConfigChangeSpringEvent changeEvent) {
        ConfigChange configChange = changeEvent.getConfigChange();
        Set<String> changedKeys = configChange.getChangedConfigKeys();
        if (changedKeys.stream().anyMatch((changedKey) -> {
            return changedKey.startsWith("titans.sharding.fusingConfigs");
        })) {
            BindResult<ShardingProperties> bind = Binder.get(this.environment).bind("titans.sharding", ShardingProperties.class);
            if (bind.isBound()) {
                ShardingJdbcUtil.initShardingFusing((ShardingProperties)bind.get());
            }
        }

        Boolean enable = (Boolean)this.environment.getProperty("titans.datasource.dynamic.change.enabled", Boolean.class, false);
        if (enable) {
            ShardingConfigChange shardingConfigChange = this.resolveConfigForShardingJdbc(changeEvent);
            if (shardingConfigChange != null) {
                try {
                    DynamicShardingDataSource shardingDataSource = (DynamicShardingDataSource)this.context.getBean("shardingDataSource", DynamicShardingDataSource.class);
                    Map<String, MasterSlaveRuleConfiguration> masterSlaveRuleConfigs = shardingDataSource.getMasterSlaveRuleConfigs();
                    Map<String, DataSource> oldDataSourceMap = shardingDataSource.getDataSourceMap();
                    LOGGER.info("Sharding-dataSource Updating...");
                    ShardingConfigChangeEventListener.Result result = analyseUpdatedDataSources(shardingConfigChange, masterSlaveRuleConfigs, oldDataSourceMap);
                    int defaultIndex = shardingConfigChange.getDefaultDSIndex() != null ? shardingConfigChange.getDefaultDSIndex() : 0;
                    String defaultDSName = ShardingJdbcUtil.generationCurrentDataBaseName((long)defaultIndex);
                    shardingDataSource.updateDataSource(defaultDSName, result.getNewDataSourceMap(), shardingConfigChange.getTableConfigs(), masterSlaveRuleConfigs);
                    LOGGER.info("Sharding-dataSource Update completed.");
                    Iterator var12 = result.getReleaseDS().iterator();

                    while(var12.hasNext()) {
                        DataSource releaseD = (DataSource)var12.next();
                        DataSourceHelp.asyncRelease(new ReleaseDataSourceRunnable(releaseD, (long)this.forceCloseWaitSeconds));
                    }
                } catch (NoSuchBeanDefinitionException var14) {
                    LOGGER.error("没有找到配置名为[{shardingDataSource}]的数据源Bean信息,无法更新配置", var14);
                }

            }
        }
    }

    private ShardingConfigChange resolveConfigForShardingJdbc(DynamicConfigChangeSpringEvent changeEvent) {
        Map<Integer, MasterDsProperties> changedConfigMap = new HashMap();
        ConfigChange configChange = changeEvent.getConfigChange();
        Set<String> changedKeys = configChange.getChangedConfigKeys();
        Iterator var5 = changedKeys.iterator();

        while(true) {
            String key;
            do {
                if (!var5.hasNext()) {
                    boolean tableConfigChanged = changedKeys.stream().anyMatch((key_) -> {
                        return EnvironmentManager.SHARDING_TABLE_PREFIX_REGULAR.matcher(key_).matches() || EnvironmentManager.SHARDING_TABLE_OLD_PREFIX_REGULAR.matcher(key_).matches();
                    });
                    boolean defDsIndecChanged = changedKeys.contains("titans.sharding.defaultDSIndex") || changedKeys.contains("sharding.defaultDSIndex");
                    if (!tableConfigChanged && !defDsIndecChanged && changedConfigMap.size() <= 0) {
                        return null;
                    }

                    List<TableConfig> tableConfigs = ShardingJdbcUtil.loadShardingTableConfig(this.environment);
                    Integer defDSIndex = (Integer)this.environment.getProperty("titans.sharding.defaultDSIndex", Integer.class);
                    return new ShardingConfigChange(changedConfigMap, tableConfigs, defDSIndex);
                }

                key = (String)var5.next();
            } while(!EnvironmentManager.SHARDING_PREFIX_REGULAR.matcher(key).matches() && !EnvironmentManager.SHARDING_OLD_PREFIX_REGULAR.matcher(key).matches());

            int index = Integer.parseInt(key.substring(key.indexOf("[") + 1, key.indexOf("]")));
            if (!changedConfigMap.containsKey(index)) {
                MasterDsProperties dsProperties = ShardingJdbcUtil.loadShardingDbConfig(this.environment, index);
                dsProperties.setName(ShardingJdbcUtil.generationCurrentDataBaseName((long)index));
                changedConfigMap.put(index, dsProperties);
            }
        }
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
            this.newDataSourceMap = new HashMap(oldDataSourceMap);
        }

        public Map<String, DataSource> getNewDataSourceMap() {
            return this.newDataSourceMap;
        }

        public Set<DataSource> getReleaseDS() {
            return this.releaseDS;
        }

        private ShardingConfigChangeEventListener.Result invoke() {
            this.releaseDS = new HashSet();
            Map<Integer, MasterDsProperties> changedDataSources = this.shardingConfigChange.getChangedDataSources();
            Iterator var2 = changedDataSources.values().iterator();

            while(var2.hasNext()) {
                MasterDsProperties dsProperties = (MasterDsProperties)var2.next();
                DataSource removeDs = (DataSource)this.newDataSourceMap.remove(dsProperties.getName());
                if (removeDs != null) {
                    this.releaseDS.add(removeDs);
                }

                MasterSlaveRuleConfiguration masterSlaveRuleConfig = (MasterSlaveRuleConfiguration)this.masterSlaveRuleConfigs.remove(dsProperties.getName());
                if (masterSlaveRuleConfig != null) {
                    this.releaseDS.add(this.newDataSourceMap.remove(masterSlaveRuleConfig.getMasterDataSourceName()));
                    Collection<String> slaveDataSourceNames = masterSlaveRuleConfig.getSlaveDataSourceNames();
                    Iterator var7 = slaveDataSourceNames.iterator();

                    while(var7.hasNext()) {
                        String slaveDataSourceName = (String)var7.next();
                        this.releaseDS.add(this.newDataSourceMap.remove(slaveDataSourceName));
                    }
                }

                if (Objects.nonNull(dsProperties.getHikari()) && StringUtils.hasText(dsProperties.getHikari().getJdbcUrl())) {
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
