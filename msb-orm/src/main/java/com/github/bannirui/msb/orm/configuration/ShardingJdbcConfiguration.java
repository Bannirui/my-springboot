package com.github.bannirui.msb.orm.configuration;

public class ShardingJdbcConfiguration implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {
    private static Logger LOGGER = LoggerFactory.getLogger(ShardingJdbcConfiguration.class);
    protected ConfigurableEnvironment env;

    public ShardingJdbcConfiguration() {
    }

    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanFactory) throws BeansException {
        ShardingProperties shardingProperties = this.initConfig("titans.sharding", "titans.sharding.datasources[%s].slave.hikari");
        ShardingJdbcUtil.initShardingFusing(shardingProperties);
        String dataSourceName = "shardingDataSource";
        String sessionFactoryName = "shardingSessionFactory";
        String mapperScannerConfigurerName = "shardingMapperScannerConfigurer";
        String transactionManagerName = "shardingTransactionManager";
        String transactionTemplateName = "shardingTransactionTemplate";

        try {
            this.registerDataSourceBeanDefinitionBuilder(dataSourceName, beanFactory, shardingProperties);
        } catch (Exception var9) {
            throw FrameworkException.getInstance(var9, "创建数据库连接池异常{0}", new Object[]{dataSourceName});
        }

        this.registerSessionFactoryDefinitionBuilder(sessionFactoryName, beanFactory, dataSourceName);
        this.registerMapperScannerDefinitionBuilder(mapperScannerConfigurerName, beanFactory, sessionFactoryName);
        this.registerTransactionManagerDefinitionBuilder(transactionManagerName, beanFactory, dataSourceName);
        this.registerTransactionTemplateDefinitionBuilder(transactionTemplateName, beanFactory, transactionManagerName);
    }

    private void registerTransactionManagerDefinitionBuilder(String transactionManagerName, BeanDefinitionRegistry beanFactory, String dataSourceName) {
        BeanDefinitionBuilder transactionManagerDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(DataSourceTransactionManager.class);
        transactionManagerDefinitionBuilder.addPropertyReference("dataSource", dataSourceName);
        beanFactory.registerBeanDefinition(transactionManagerName, transactionManagerDefinitionBuilder.getRawBeanDefinition());
    }

    private void registerMapperScannerDefinitionBuilder(String mapperScannerConfigurerName, BeanDefinitionRegistry beanFactory, String sqlSessionFactoryName) {
        GenericBeanDefinition mapperScannerBeanDefinition = new GenericBeanDefinition();
        mapperScannerBeanDefinition.setBeanClass(MapperScannerConfigurer.class);
        MutablePropertyValues propertyValues = new MutablePropertyValues();
        HashMap hashMap = (HashMap)MyBatisConfigLoadUtil.loadConfig(this.env, "titans.sharding.mapper", HashMap.class);
        propertyValues.addPropertyValue("sqlSessionFactory", new RuntimeBeanReference(sqlSessionFactoryName));
        propertyValues.addPropertyValues(hashMap);
        mapperScannerBeanDefinition.setPropertyValues(propertyValues);
        beanFactory.registerBeanDefinition(mapperScannerConfigurerName, mapperScannerBeanDefinition);
    }

    private void registerSessionFactoryDefinitionBuilder(String sessionFactoryName, BeanDefinitionRegistry beanFactory, String dataSourceName) {
        GenericBeanDefinition sqlsessionBeanDefinition = new GenericBeanDefinition();
        sqlsessionBeanDefinition.setBeanClass(SqlSessionFactoryBean.class);
        MutablePropertyValues propertyValues = new MutablePropertyValues();
        HashMap sqlSessionConfigMap = (HashMap)MyBatisConfigLoadUtil.loadConfig(this.env, "titans.sharding.sqlsession", HashMap.class);
        Object mapperLocations = sqlSessionConfigMap.get("mapperLocations");
        if (StringUtils.hasText((String)mapperLocations)) {
            Resource[] resources = ResourceHelp.resolveMapperLocations((String)mapperLocations);
            sqlSessionConfigMap.put("mapperLocations", resources);
        }

        propertyValues.addPropertyValues(sqlSessionConfigMap);
        sqlsessionBeanDefinition.setPropertyValues(propertyValues);
        Object configLocation = sqlSessionConfigMap.get("configLocation");
        if (Objects.nonNull(configLocation)) {
            propertyValues.addPropertyValue("configLocation", ResourceHelp.resolveMapperLocations(configLocation.toString())[0]);
        } else {
            Configuration configuration = (Configuration)MyBatisConfigLoadUtil.loadConfig(this.env, "titans.sharding.settings", Configuration.class);
            propertyValues.add("configuration", configuration);
        }

        propertyValues.add("dataSource", new RuntimeBeanReference(dataSourceName));
        List<Interceptor> mybatisInterceptors = new ArrayList();
        PageInterceptor pageInterceptor = new PageInterceptor();
        Properties properties = new Properties();
        properties.setProperty("autoRuntimeDialect", "true");
        pageInterceptor.setProperties(properties);
        PluginDecorator<Class> pageInterceptorPluginDecorator = new PluginDecorator(pageInterceptor.getClass(), -1);
        pageInterceptorPluginDecorator.setInstance(pageInterceptor);
        List<PluginDecorator<Class>> mybatisFilters = PluginConfigManager.getOrderedPluginClasses(Interceptor.class.getName(), true);
        PluginConfigManager.insertIntoOrderedList(mybatisFilters, true, pageInterceptorPluginDecorator);
        Iterator var14 = mybatisFilters.iterator();

        while(var14.hasNext()) {
            PluginDecorator pd = (PluginDecorator)var14.next();

            try {
                Interceptor mybatisInterceptor;
                if (pd.getInstance() != null) {
                    mybatisInterceptor = (Interceptor)pd.getInstance();
                } else {
                    Class pluginClass = pd.getPlugin();
                    mybatisInterceptor = (Interceptor)pluginClass.newInstance();
                }

                mybatisInterceptors.add(mybatisInterceptor);
            } catch (InstantiationException | IllegalAccessException var18) {
                LOGGER.error("load mybatis plugin error: ", var18);
            }
        }

        propertyValues.add("plugins", mybatisInterceptors);
        beanFactory.registerBeanDefinition(sessionFactoryName, sqlsessionBeanDefinition);
    }

    protected void registerDataSourceBeanDefinitionBuilder(String dataSourceName, BeanDefinitionRegistry beanFactory, ShardingProperties shardingProperties) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        BeanDefinitionBuilder dataSourceDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(SimpleShardingDataSource.class);
        Map<String, DataSource> dataSourceMap = new HashMap();
        Map<String, MasterSlaveRuleConfiguration> masterSlaveRuleConfigs = new HashMap();

        ShardingDsInfo shardingDsInfo;
        for(Iterator var7 = shardingProperties.getDataSources().iterator(); var7.hasNext(); dataSourceMap.putAll(shardingDsInfo.getDataSourceMap())) {
            MasterDsProperties config = (MasterDsProperties)var7.next();
            shardingDsInfo = ShardingJdbcUtil.createShardingDataSource(config);
            if (shardingDsInfo.hasSlave()) {
                masterSlaveRuleConfigs.put(config.getName(), shardingDsInfo.getMasterSlaveRuleConfig());
            }
        }

        dataSourceDefinitionBuilder.addPropertyValue("dataSourceMap", dataSourceMap);
        dataSourceDefinitionBuilder.addPropertyValue("defaultDataSource", shardingProperties.getDefaultDataSource());
        dataSourceDefinitionBuilder.addPropertyValue("tableConfigs", shardingProperties.getTableConfigs());
        dataSourceDefinitionBuilder.addPropertyValue("masterSlaveRuleConfigs", masterSlaveRuleConfigs);
        dataSourceDefinitionBuilder.addPropertyValue("useOptimizedTableRule", shardingProperties.isUseOptimizedTableRule());
        dataSourceDefinitionBuilder.setDestroyMethodName("shutdown");
        dataSourceDefinitionBuilder.setInitMethodName("init");
        beanFactory.registerBeanDefinition(dataSourceName, dataSourceDefinitionBuilder.getRawBeanDefinition());
    }

    private void registerTransactionTemplateDefinitionBuilder(String transactionTemplateName, BeanDefinitionRegistry beanFactory, String transactionManagerName) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(TransactionTemplate.class);
        beanDefinitionBuilder.addPropertyReference("transactionManager", transactionManagerName);
        beanFactory.registerBeanDefinition(transactionTemplateName, beanDefinitionBuilder.getRawBeanDefinition());
    }

    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
    }

    protected ShardingProperties initConfig(String shardingPrefix, String dataSourceSlavePrefix) {
        ShardingProperties shardingProperties = (ShardingProperties)Binder.get(this.env).bind(shardingPrefix, ShardingProperties.class).orElseThrow(() -> {
            return FrameworkException.getInstance("No sharding config value bound, prefix equals " + shardingPrefix, new Object[0]);
        });
        List<MasterDsProperties> dsProperties = shardingProperties.getDataSources();
        int size = dsProperties.size();

        int i;
        for(i = 0; i < size; ++i) {
            if (Objects.nonNull(((MasterDsProperties)dsProperties.get(i)).getSlave())) {
                HikariConfig master = ((MasterDsProperties)dsProperties.get(i)).getHikari();
                HikariConfig slave = new HikariConfig();
                master.copyStateTo(slave);
                Bindable<HikariConfig> hikariConfigBindable = Bindable.ofInstance(slave);
                slave = (HikariConfig)Binder.get(this.env).bind(String.format(dataSourceSlavePrefix, i), hikariConfigBindable).orElse((Object)null);
                ((MasterDsProperties)dsProperties.get(i)).getSlave().setHikari(slave);
            }

            ((MasterDsProperties)dsProperties.get(i)).setName(ShardingJdbcUtil.generationCurrentDataBaseName((long)i));
        }

        i = shardingProperties.getDefaultDSIndex() != null ? shardingProperties.getDefaultDSIndex() : 0;
        shardingProperties.setDefaultDataSource(ShardingJdbcUtil.generationCurrentDataBaseName((long)i));
        return shardingProperties;
    }

    public void setEnvironment(Environment environment) {
        this.env = (ConfigurableEnvironment)environment;
    }
}
