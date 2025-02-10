package com.github.bannirui.msb.orm.configuration;

import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.github.bannirui.msb.orm.property.MasterDsProperties;
import com.github.bannirui.msb.orm.util.DBPasswordDecoder;
import com.github.bannirui.msb.orm.util.DataSourceHelp;
import com.github.bannirui.msb.orm.util.MyBatisConfigLoadUtil;
import com.github.bannirui.msb.orm.util.ResourceHelp;
import com.github.bannirui.msb.plugin.PluginConfigManager;
import com.github.bannirui.msb.plugin.PluginDecorator;
import com.github.pagehelper.PageInterceptor;
import com.zaxxer.hikari.HikariConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.Configuration;
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
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;

public class MyBatisPlusConfiguration implements BeanDefinitionRegistryPostProcessor, EnvironmentAware, Ordered {
    private static final Logger logger = LoggerFactory.getLogger(MyBatisPlusConfiguration.class);
    private static final int order = 50;
    public static final String mapper_locations = "mapperLocations";
    protected ConfigurableEnvironment env;

    @Override
    public void setEnvironment(Environment environment) {
        this.env = (ConfigurableEnvironment)environment;
    }

    @Override
    public int getOrder() {
        return MyBatisPlusConfiguration.order;
    }

    /**
     * 注册Mybatis-plus依赖的BeanDefinition
     * <ul>
     *     <li>DataSource</li>
     *     <li>SessionFactory</li>
     *     <li>MapperScanner</li>
     *     <li>TransactionManager</li>
     *     <li>TransactionTemplate</li>
     * </ul>
     */
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanFactory) throws BeansException {
        // 单数据源
        if (Objects.nonNull(this.env.getProperty("mybatis.config.datasource.name"))) {
            MasterDsProperties dsConfig = MyBatisConfigLoadUtil.loadSingleConfig(this.env, MasterDsProperties.class);
            String dataSourceBeanName = DataSourceHelp.generateDataSourceBeanName(dsConfig.getName());
            this.registerSingleDataSourceBeanDefinition(dataSourceBeanName, dsConfig, beanFactory);
            this.registerBeanDefinition(dsConfig, -1, beanFactory, dataSourceBeanName);
        }
        // 多数据源
        if (this.env.getProperty(String.format("mybatis.configs[%s].datasource.name", 0)) != null) {
            List<MasterDsProperties> dsConfigs = MyBatisConfigLoadUtil.loadMultipleConfig(this.env, MasterDsProperties.class);
            for(int i = 0, sz=dsConfigs.size(); i < sz; ++i) {
                MasterDsProperties dsConfig = dsConfigs.get(i);
                String dataSourceBeanName = DataSourceHelp.generateDataSourceBeanName(dsConfig.getName());
                this.registerMultiDataSourceBeanDefinition(dataSourceBeanName, dsConfig, beanFactory, i);
                this.registerBeanDefinition(dsConfig, i, beanFactory, dataSourceBeanName);
            }
        }
    }

    /**
     * 注册BeanDefinition
     * <ul>
     *     <li>SessionFactory</li>
     *     <li>MapperScanner</li>
     *     <li>TransactionManager</li>
     *     <li>TransactionTemplate</li>
     * </ul>
     * @param myBatisConfigIndex 负数标识只有一个数据源 非负数标识数据源索引(0-based)
     */
    private void registerBeanDefinition(MasterDsProperties dsConfig, int myBatisConfigIndex, BeanDefinitionRegistry beanFactory, String refDataSourceName) {
        String sessionFactoryName = dsConfig.getName() + "SessionFactory";
        String mapperScannerConfigureName = dsConfig.getName() + "MapperScannerConfigure";
        String transactionManagerName = dsConfig.getName() + "TransactionManager";
        String transactionTemplateName = dsConfig.getName() + "TransactionTemplate";
        Map<String, Object> sqlSessionConfigMap;
        Map<String, Object> mapperConfigMap;
        Configuration configuration;
        if (myBatisConfigIndex < 0) {
            sqlSessionConfigMap = MyBatisConfigLoadUtil.loadConfig(this.env, "mybatis.config.sqlsession", HashMap.class);
            mapperConfigMap = MyBatisConfigLoadUtil.loadConfig(this.env, "mybatis.config.mapper", HashMap.class);
            configuration = MyBatisConfigLoadUtil.loadConfig(this.env, "mybatis.config.settings", Configuration.class);
        } else {
            sqlSessionConfigMap = MyBatisConfigLoadUtil.loadConfigs(this.env, "mybatis.configs[%s].sqlsession", myBatisConfigIndex, HashMap.class);
            mapperConfigMap = MyBatisConfigLoadUtil.loadConfigs(this.env, "mybatis.configs[%s].mapper", myBatisConfigIndex, HashMap.class);
            configuration = MyBatisConfigLoadUtil.loadConfigs(this.env, "mybatis.configs[%s].settings", myBatisConfigIndex, Configuration.class);
        }
        this.convertMybatisConfig(sqlSessionConfigMap);
        this.registerSessionFactoryDefinitionBuilder(sessionFactoryName, beanFactory, sqlSessionConfigMap, configuration, refDataSourceName);
        this.registerMapperScannerDefinitionBuilder(mapperScannerConfigureName, beanFactory, dsConfig, mapperConfigMap, sessionFactoryName);
        this.registerTransactionManagerDefinitionBuilder(transactionManagerName, beanFactory, refDataSourceName);
        this.registerTransactionTemplateDefinitionBuilder(transactionTemplateName, beanFactory, transactionManagerName);
    }

    /**
     * 注册数据源
     * @param dataSourceName beanName
     */
    protected void registerSingleDataSourceBeanDefinition(String dataSourceName, MasterDsProperties dsConfig, BeanDefinitionRegistry beanFactory) {
        HikariConfig hikariConfig = dsConfig.getHikari();
        hikariConfig.setPassword(DBPasswordDecoder.decode(hikariConfig.getPassword()));
        BeanDefinitionBuilder dataSourceDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(DynamicDataSource.class);
        dataSourceDefinitionBuilder.addConstructorArgValue(hikariConfig);
        beanFactory.registerBeanDefinition(dataSourceName, dataSourceDefinitionBuilder.getRawBeanDefinition());
    }

    protected void registerMultiDataSourceBeanDefinition(String dataSourceName, MasterDsProperties dsConfig, BeanDefinitionRegistry beanFactory, int index) {
        this.registerSingleDataSourceBeanDefinition(dataSourceName, dsConfig, beanFactory);
    }

    private void registerTransactionManagerDefinitionBuilder(String transactionManagerName, BeanDefinitionRegistry beanFactory, String dataSourceName) {
        BeanDefinitionBuilder transactionManagerDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(DataSourceTransactionManager.class);
        transactionManagerDefinitionBuilder.addPropertyReference("dataSource", dataSourceName);
        beanFactory.registerBeanDefinition(transactionManagerName, transactionManagerDefinitionBuilder.getRawBeanDefinition());
    }

    private void registerMapperScannerDefinitionBuilder(String mapperScannerConfigurerName, BeanDefinitionRegistry beanFactory, MasterDsProperties dsConfig, Map<String, Object> mapperConfigMap, String sqlSessionFactoryName) {
        GenericBeanDefinition mapperScannerBeanDefinition = new GenericBeanDefinition();
        mapperScannerBeanDefinition.setBeanClass(MapperScannerConfigurer.class);
        MutablePropertyValues propertyValues = new MutablePropertyValues();
        propertyValues.addPropertyValue("sqlSessionFactory", new RuntimeBeanReference(sqlSessionFactoryName));
        propertyValues.addPropertyValues(mapperConfigMap);
        if (dsConfig.getHikari().getJdbcUrl().indexOf("oracle") > 0) {
            Properties properties = new Properties();
            properties.setProperty("ORDER", "BEFORE");
            propertyValues.addPropertyValue("properties", properties);
        }
        mapperScannerBeanDefinition.setPropertyValues(propertyValues);
        beanFactory.registerBeanDefinition(mapperScannerConfigurerName, mapperScannerBeanDefinition);
    }

    /**
     * 注册SqlSessionFactory
     */
    private void registerSessionFactoryDefinitionBuilder(String sessionFactoryName, BeanDefinitionRegistry beanFactory, Map<String, Object> sqlSessionConfigMap, Configuration configuration, String dataSourceName) {
        GenericBeanDefinition sqlsessionBeanDefinition = new GenericBeanDefinition();
        sqlsessionBeanDefinition.setBeanClass(MybatisSqlSessionFactoryBean.class);
        MutablePropertyValues propertyValues = new MutablePropertyValues();
        sqlsessionBeanDefinition.setPropertyValues(propertyValues);
        propertyValues.addPropertyValue("vfs", SpringBootVFS.class);
        propertyValues.addPropertyValues(sqlSessionConfigMap);
        Object configLocation = sqlSessionConfigMap.get("configLocation");
        if (Objects.nonNull(configLocation)) {
            propertyValues.addPropertyValue("configLocation", ResourceHelp.resolveMapperLocations(configLocation.toString())[0]);
        } else {
            propertyValues.add("configuration", configuration);
        }
        propertyValues.add("dataSource", new RuntimeBeanReference(dataSourceName));
        List<Interceptor> mybatisInterceptors = new ArrayList<>();
        PageInterceptor pageInterceptor = new PageInterceptor();
        Properties properties = new Properties();
        properties.setProperty("autoRuntimeDialect", "true");
        pageInterceptor.setProperties(properties);
        PluginDecorator<Class<?>> pageInterceptorPluginDecorator = new PluginDecorator(pageInterceptor.getClass(), -1);
        pageInterceptorPluginDecorator.setInstance(pageInterceptor);
        // classpath:msb/plugin/org.apache.ibatis.plugin.Interceptor
        List<PluginDecorator<Class<?>>> mybatisFilters = PluginConfigManager.getOrderedPluginClasses(Interceptor.class.getName(), true);
        PluginConfigManager.insertIntoOrderedList(mybatisFilters, true, pageInterceptorPluginDecorator);
        for (PluginDecorator<Class<?>> pd : mybatisFilters) {
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
                logger.error("load mybatis plugin error: ", e);
            }
        }
        propertyValues.add("plugins", mybatisInterceptors);
        beanFactory.registerBeanDefinition(sessionFactoryName, sqlsessionBeanDefinition);
    }

    private void registerTransactionTemplateDefinitionBuilder(String transactionTemplateName, BeanDefinitionRegistry beanFactory, String transactionManagerName) {
        BeanDefinitionBuilder transactionTemplateDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(TransactionTemplate.class);
        transactionTemplateDefinitionBuilder.addPropertyReference("transactionManager", transactionManagerName);
        beanFactory.registerBeanDefinition(transactionTemplateName, transactionTemplateDefinitionBuilder.getRawBeanDefinition());
    }

    private void convertMybatisConfig(Map<String, Object> sqlSessionConfigMap) {
        String mapperLocations = (String)sqlSessionConfigMap.get(MyBatisPlusConfiguration.mapper_locations);
        if (StringUtils.isNotBlank(mapperLocations)) {
            sqlSessionConfigMap.put(MyBatisPlusConfiguration.mapper_locations, ResourceHelp.resolveMapperLocations(mapperLocations));
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
    }
}
