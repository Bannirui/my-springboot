package com.github.bannirui.msb.orm.configuration;

import com.github.bannirui.msb.event.DynamicConfigChangeSpringEvent;
import com.github.bannirui.msb.orm.property.MasterDsProperties;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

public class DataSourceChangeEventListener implements ApplicationListener<DynamicConfigChangeSpringEvent>, ApplicationContextAware, EnvironmentAware {
    private static Logger log = LoggerFactory.getLogger(DataSourceChangeEventListener.class);
    @Value("${titans.datasource.dynamic.forceCloseWaitSeconds:300}")
    private int forceCloseWaitSeconds = 300;
    private ApplicationContext context;
    private Environment env;

    public void setEnvironment(Environment environment) {
        this.env = environment;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    public synchronized void onApplicationEvent(DynamicConfigChangeSpringEvent configChangeEvent) {
        Boolean enable = this.env.getProperty("titans.datasource.dynamic.change.enabled", Boolean.class, false);
        if (enable) {
            List<MasterDsProperties> properties = this.resolveConfigForMyBatis(configChangeEvent);
            Iterator var4 = properties.iterator();
            while(var4.hasNext()) {
                MasterDsProperties newConfig = (MasterDsProperties)var4.next();
                String name = newConfig.getName();
                try {
                    DynamicDataSource dataSource = this.context.getBean(name + "DataSource", DynamicDataSource.class);
                    HikariConfig hikariConfig = newConfig.getHikari();
                    dataSource.updateDataSource(hikariConfig, (long)this.forceCloseWaitSeconds);
                } catch (NoSuchBeanDefinitionException e) {
                    log.error("没有找到配置名为[{" + name + "}]的数据源Bean信息,无法更新配置", e);
                }
            }
        }
    }

    private List<MasterDsProperties> resolveConfigForMyBatis(DynamicConfigChangeSpringEvent changeEvent) {
        ConfigChange configChange = changeEvent.getConfigChange();
        Set<String> changedKeys = configChange.getChangedConfigKeys();
        List<MasterDsProperties> properties = new ArrayList();
        String PREFIX = "titans.mybatis.config.datasource.";
        boolean matches = changedKeys.stream().anyMatch((keyx) -> {
            return keyx.startsWith(PREFIX) || keyx.startsWith("mybatis.config.");
        });
        if (matches) {
            if (StringUtils.hasText(this.env.getProperty("titans.mybatis.config.datasource.name"))) {
                MasterDsProperties dsProperties = (MasterDsProperties)MyBatisConfigLoadUtil.loadSingleConfig(this.env, MasterDsProperties.class);
                properties.add(dsProperties);
            } else {
                log.error("warn:未找到数据源Spring Bean名称,数据源无法更新,确认{}是否配置", "titans.mybatis.config.datasource.name");
            }
        }

        Set<Integer> updateConfigs = new HashSet();
        Iterator var8 = changedKeys.iterator();

        while(true) {
            String key;
            do {
                if (!var8.hasNext()) {
                    return properties;
                }

                key = (String)var8.next();
            } while(!EnvironmentManager.MYBATIS_CONFIGS_PREFIX_REGULAR.matcher(key).matches() && !EnvironmentManager.MYBATIS_OLD_CONFIGS_PREFIX_REGULAR.matcher(key).matches());

            Integer index = Integer.valueOf(key.substring(key.indexOf("[") + 1, key.indexOf("]")));
            if (updateConfigs.add(index)) {
                String nameKey = String.format("titans.mybatis.configs[%s].datasource.name", index);
                String configName = this.env.getProperty(nameKey);
                if (StringUtils.hasText(configName)) {
                    MasterDsProperties dsProperties = (MasterDsProperties)MyBatisConfigLoadUtil.loadConfigByIndex(this.env, index, MasterDsProperties.class);
                    properties.add(dsProperties);
                } else {
                    log.error("warn:未找到数据源Spring Bean名称,数据源无法更新,确认{}是否配置", nameKey);
                }
            }
        }
    }
}
