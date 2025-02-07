package com.github.bannirui.msb.orm.configuration;

import com.github.bannirui.msb.orm.property.MasterDsProperties;
import com.github.bannirui.msb.orm.util.DBPasswordDecoder;
import com.github.bannirui.msb.orm.util.DataSourceHelp;
import com.github.bannirui.msb.orm.util.MyBatisConfigLoadUtil;
import com.github.bannirui.msb.orm.util.ResourceHelp;
import com.github.bannirui.msb.plugin.Interceptor;
import com.github.bannirui.msb.plugin.PluginDecorator;
import com.github.pagehelper.PageInterceptor;
import com.zaxxer.hikari.HikariConfig;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;

public class MyBatisConfiguration implements BeanDefinitionRegistryPostProcessor, EnvironmentAware, Ordered {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyBatisConfiguration.class);
    public static final int ORDER = 50;
    private static final String MAPPER_LOCATIONS = "mapperLocations";
    protected ConfigurableEnvironment env;

    public void setEnvironment(Environment environment) {
        this.env = (ConfigurableEnvironment)environment;
    }

    public int getOrder() {
        return 50;
    }

    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanFactory) throws BeansException {
        if (this.env.getProperty("titans.mybatis.config.datasource.name") != null) {
            MasterDsProperties dsConfig = MyBatisConfigLoadUtil.loadSingleConfig(this.env, MasterDsProperties.class);
            String dataSourceBeanName = DataSourceHelp.generateDataSourceBeanName(dsConfig.getName());
            if (dataSourceBeanName == null) {
                LOGGER.error("dataSource name must not be null!");
                return;
            }
            this.registerSingleDataSourceBeanDefinition(dataSourceBeanName, dsConfig, beanFactory);
            this.registerBean(dsConfig, -1, beanFactory, dataSourceBeanName);
        }
        if (this.env.getProperty(String.format("titans.mybatis.configs[%s].datasource.name", 0)) != null) {
            List<MasterDsProperties> dsConfigs = MyBatisConfigLoadUtil.loadMultipleConfig(this.env, MasterDsProperties.class);
            for(int i = 0; i < dsConfigs.size(); ++i) {
                MasterDsProperties dsConfig = dsConfigs.get(i);
                String dataSourceBeanName = DataSourceHelp.generateDataSourceBeanName(dsConfig.getName());
                if (dataSourceBeanName == null) {
                    LOGGER.error("dataSource name must not be null!");
                    return;
                }
                this.registerMultiDataSourceBeanDefinition(dataSourceBeanName, dsConfig, beanFactory, i);
                this.registerBean(dsConfig, i, beanFactory, dataSourceBeanName);
            }
        }
    }

    private void registerBean(MasterDsProperties dsConfig, int myBatisConfigIndex, BeanDefinitionRegistry beanFactory, String refDataSourceName) {
        String sessionFactoryName = dsConfig.getName() + "SessionFactory";
        String mapperScannerConfigurerName = dsConfig.getName() + "MapperScannerConfigurer";
        String transactionManagerName = dsConfig.getName() + "TransactionManager";
        String transactionTemplateName = dsConfig.getName() + "TransactionTemplate";
        HashMap sqlSessionConfigMap;
        HashMap mapperConfigMap;
        Configuration configuration;
        if (myBatisConfigIndex < 0) {
            sqlSessionConfigMap = MyBatisConfigLoadUtil.loadConfig(this.env, "titans.mybatis.config.sqlsession", HashMap.class);
            mapperConfigMap = MyBatisConfigLoadUtil.loadConfig(this.env, "titans.mybatis.config.mapper", HashMap.class);
            configuration = MyBatisConfigLoadUtil.loadConfig(this.env, "titans.mybatis.config.settings", Configuration.class);
        } else {
            sqlSessionConfigMap = MyBatisConfigLoadUtil.loadConfigs(this.env, "titans.mybatis.configs[%s].sqlsession", myBatisConfigIndex, HashMap.class);
            mapperConfigMap = MyBatisConfigLoadUtil.loadConfigs(this.env, "titans.mybatis.configs[%s].mapper", myBatisConfigIndex, HashMap.class);
            configuration = MyBatisConfigLoadUtil.loadConfigs(this.env, "titans.mybatis.configs[%s].settings", myBatisConfigIndex, Configuration.class);
        }
        this.convertMybatisConfig(sqlSessionConfigMap);
        this.registerSessionFactoryDefinitionBuilder(sessionFactoryName, beanFactory, sqlSessionConfigMap, configuration, refDataSourceName);
        this.registerMapperScannerDefinitionBuilder(mapperScannerConfigurerName, beanFactory, dsConfig, mapperConfigMap, sessionFactoryName);
        this.registerTransactionManagerDefinitionBuilder(transactionManagerName, beanFactory, refDataSourceName);
        this.registerTransactionTemplateDefinitionBuilder(transactionTemplateName, beanFactory, transactionManagerName);
    }

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

    private void registerMapperScannerDefinitionBuilder(String mapperScannerConfigurerName, BeanDefinitionRegistry beanFactory, MasterDsProperties dsConfig, HashMap mapperConfigMap, String sqlSessionFactoryName) {
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

    private void registerSessionFactoryDefinitionBuilder(String sessionFactoryName, BeanDefinitionRegistry beanFactory, HashMap sqlSessionConfigMap, Configuration configuration, String dataSourceName) {
        GenericBeanDefinition sqlsessionBeanDefinition = new GenericBeanDefinition();
        sqlsessionBeanDefinition.setBeanClass(SqlSessionFactoryBean.class);
        MutablePropertyValues propertyValues = new MutablePropertyValues();
        propertyValues.addPropertyValue("vfs", SpringBootVFS.class);
        propertyValues.addPropertyValues(sqlSessionConfigMap);
        sqlsessionBeanDefinition.setPropertyValues(propertyValues);
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

    private void registerTransactionTemplateDefinitionBuilder(String transactionTemplateName, BeanDefinitionRegistry beanFactory, String transactionManagerName) {
        BeanDefinitionBuilder transactionTemplateDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(TransactionTemplate.class);
        transactionTemplateDefinitionBuilder.addPropertyReference("transactionManager", transactionManagerName);
        beanFactory.registerBeanDefinition(transactionTemplateName, transactionTemplateDefinitionBuilder.getRawBeanDefinition());
    }

    private void convertMybatisConfig(HashMap sqlSessionConfigMap) {
        String mapperLocations = (String)sqlSessionConfigMap.get("mapperLocations");
        if (StringUtils.isNotBlank(mapperLocations)) {
            sqlSessionConfigMap.put("mapperLocations", ResourceHelp.resolveMapperLocations(mapperLocations));
        }
    }

    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
    }
}
