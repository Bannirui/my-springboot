package com.github.bannirui.msb.orm.configuration;

import com.github.bannirui.msb.ex.FrameworkException;
import com.github.bannirui.msb.orm.property.MasterDsProperties;
import com.github.bannirui.msb.orm.property.ShardingProperties;
import com.github.bannirui.msb.orm.shardingjdbc.ShardingDsInfo;
import com.github.bannirui.msb.orm.util.MyBatisConfigLoadUtil;
import com.github.bannirui.msb.orm.util.ResourceHelp;
import com.github.bannirui.msb.orm.util.ShardingJdbcUtil;
import com.github.bannirui.msb.plugin.Interceptor;
import com.github.bannirui.msb.plugin.PluginDecorator;
import com.github.pagehelper.PageInterceptor;
import com.zaxxer.hikari.HikariConfig;
import io.micrometer.common.util.StringUtils;
import org.apache.ibatis.session.Configuration;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.*;

public class ShardingJdbcConfiguration implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {
    private static Logger LOGGER = LoggerFactory.getLogger(ShardingJdbcConfiguration.class);
    protected ConfigurableEnvironment env;

    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanFactory) throws BeansException {
        ShardingProperties shardingProperties = this.initConfig("msb.sharding", "msb.sharding.datasources[%s].slave.hikari");
        ShardingJdbcUtil.initShardingFusing(shardingProperties);
        String dataSourceName = "shardingDataSource";
        String sessionFactoryName = "shardingSessionFactory";
        String mapperScannerConfigurerName = "shardingMapperScannerConfigurer";
        String transactionManagerName = "shardingTransactionManager";
        String transactionTemplateName = "shardingTransactionTemplate";
        try {
            this.registerDataSourceBeanDefinitionBuilder(dataSourceName, beanFactory, shardingProperties);
        } catch (Exception e) {
            throw FrameworkException.getInstance(e, "创建数据库连接池异常{0}", dataSourceName);
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
        Map hashMap = MyBatisConfigLoadUtil.loadConfig(this.env, "msb.sharding.mapper", HashMap.class);
        propertyValues.addPropertyValue("sqlSessionFactory", new RuntimeBeanReference(sqlSessionFactoryName));
        propertyValues.addPropertyValues(hashMap);
        mapperScannerBeanDefinition.setPropertyValues(propertyValues);
        beanFactory.registerBeanDefinition(mapperScannerConfigurerName, mapperScannerBeanDefinition);
    }

    private void registerSessionFactoryDefinitionBuilder(String sessionFactoryName, BeanDefinitionRegistry beanFactory, String dataSourceName) {
        GenericBeanDefinition sqlsessionBeanDefinition = new GenericBeanDefinition();
        sqlsessionBeanDefinition.setBeanClass(SqlSessionFactoryBean.class);
        MutablePropertyValues propertyValues = new MutablePropertyValues();
        Map<String, Object> sqlSessionConfigMap = MyBatisConfigLoadUtil.loadConfig(this.env, "msb.sharding.sqlsession", HashMap.class);
        Object mapperLocations = sqlSessionConfigMap.get("mapperLocations");
        if (StringUtils.isNotBlank((String) mapperLocations)) {
            Resource[] resources = ResourceHelp.resolveMapperLocations((String)mapperLocations);
            sqlSessionConfigMap.put("mapperLocations", resources);
        }
        propertyValues.addPropertyValues(sqlSessionConfigMap);
        sqlsessionBeanDefinition.setPropertyValues(propertyValues);
        Object configLocation = sqlSessionConfigMap.get("configLocation");
        if (Objects.nonNull(configLocation)) {
            propertyValues.addPropertyValue("configLocation", ResourceHelp.resolveMapperLocations(configLocation.toString())[0]);
        } else {
            Configuration configuration = MyBatisConfigLoadUtil.loadConfig(this.env, "msb.sharding.settings", Configuration.class);
            propertyValues.add("configuration", configuration);
        }
        propertyValues.add("dataSource", new RuntimeBeanReference(dataSourceName));
        List<Interceptor> mybatisInterceptors = new ArrayList<>();
        PageInterceptor pageInterceptor = new PageInterceptor();
        Properties properties = new Properties();
        properties.setProperty("autoRuntimeDialect", "true");
        pageInterceptor.setProperties(properties);
        PluginDecorator<Class> pageInterceptorPluginDecorator = new PluginDecorator(pageInterceptor.getClass(), -1);
        pageInterceptorPluginDecorator.setInstance(pageInterceptor);
        List<PluginDecorator<Class>> mybatisFilters = PluginConfigManager.getOrderedPluginClasses(Interceptor.class.getName(), true);
        PluginConfigManager.insertIntoOrderedList(mybatisFilters, true, pageInterceptorPluginDecorator);
        for (PluginDecorator<Class> pd : mybatisFilters) {
            try {
                Interceptor mybatisInterceptor;
                if (pd.getInstance() != null) {
                    mybatisInterceptor = (Interceptor)pd.getInstance();
                } else {
                    Class pluginClass = pd.getPlugin();
                    mybatisInterceptor = (Interceptor)pluginClass.newInstance();
                }
                mybatisInterceptors.add(mybatisInterceptor);
            } catch (InstantiationException | IllegalAccessException e) {
                LOGGER.error("load mybatis plugin error: ", e);
            }
        }
        propertyValues.add("plugins", mybatisInterceptors);
        beanFactory.registerBeanDefinition(sessionFactoryName, sqlsessionBeanDefinition);
    }

    protected void registerDataSourceBeanDefinitionBuilder(String dataSourceName, BeanDefinitionRegistry beanFactory, ShardingProperties shardingProperties) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        BeanDefinitionBuilder dataSourceDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(SimpleShardingDataSource.class);
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        Map<String, MasterSlaveRuleConfiguration> masterSlaveRuleConfigs = new HashMap<>();
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

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
    }

    protected ShardingProperties initConfig(String shardingPrefix, String dataSourceSlavePrefix) {
        ShardingProperties shardingProperties = Binder.get(this.env).bind(shardingPrefix, ShardingProperties.class).orElseThrow(() -> FrameworkException.getInstance("No sharding config value bound, prefix equals " + shardingPrefix, new Object[0]));
        List<MasterDsProperties> dsProperties = shardingProperties.getDataSources();
        int size = dsProperties.size();
        int i;
        for(i = 0; i < size; ++i) {
            if (Objects.nonNull(dsProperties.get(i).getSlave())) {
                HikariConfig master = dsProperties.get(i).getHikari();
                HikariConfig slave = new HikariConfig();
                master.copyStateTo(slave);
                Bindable<HikariConfig> hikariConfigBindable = Bindable.ofInstance(slave);
                slave = Binder.get(this.env).bind(String.format(dataSourceSlavePrefix, i), hikariConfigBindable).orElse(null);
                dsProperties.get(i).getSlave().setHikari(slave);
            }
            dsProperties.get(i).setName(ShardingJdbcUtil.generationCurrentDataBaseName((long)i));
        }
        i = shardingProperties.getDefaultDSIndex() != null ? shardingProperties.getDefaultDSIndex() : 0;
        shardingProperties.setDefaultDataSource(ShardingJdbcUtil.generationCurrentDataBaseName((long)i));
        return shardingProperties;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.env = (ConfigurableEnvironment)environment;
    }
}
