package com.github.bannirui.msb.orm.configuration;

import com.github.bannirui.msb.env.MsbEnvironmentMgr;
import com.github.bannirui.msb.event.DynamicConfigChangeSpringEvent;
import com.github.bannirui.msb.orm.property.MasterDsProperties;
import com.github.bannirui.msb.orm.util.MyBatisConfigLoadUtil;
import com.github.bannirui.msb.properties.ConfigChange;
import com.zaxxer.hikari.HikariConfig;
import org.apache.commons.lang3.StringUtils;
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

import java.util.*;

public class DataSourceChangeEventListener implements ApplicationListener<DynamicConfigChangeSpringEvent>, ApplicationContextAware, EnvironmentAware {
    private static Logger log = LoggerFactory.getLogger(DataSourceChangeEventListener.class);
    @Value("${msb.datasource.dynamic.forceCloseWaitSeconds:300}")
    private int forceCloseWaitSeconds = 300;
    private ApplicationContext context;
    private Environment env;

    @Override
    public void setEnvironment(Environment environment) {
        this.env = environment;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    @Override
    public synchronized void onApplicationEvent(DynamicConfigChangeSpringEvent configChangeEvent) {
        Boolean enable = this.env.getProperty("msb.datasource.dynamic.change.enabled", Boolean.class, false);
        if(!enable) return;
        List<MasterDsProperties> properties = this.resolveConfigForMyBatis(configChangeEvent);
        properties.forEach(newConfig -> {
            String name = newConfig.getName();
            try {
                DynamicDataSource dataSource = this.context.getBean(name + "DataSource", DynamicDataSource.class);
                HikariConfig hikariConfig = newConfig.getHikari();
                dataSource.updateDataSource(hikariConfig, this.forceCloseWaitSeconds);
            } catch (NoSuchBeanDefinitionException e) {
                log.error("没有找到配置名为[{}]的数据源Bean信息,无法更新配置", name, e);
            }
        });
    }

    private List<MasterDsProperties> resolveConfigForMyBatis(DynamicConfigChangeSpringEvent changeEvent) {
        ConfigChange configChange = changeEvent.getConfigChange();
        Set<String> changedKeys = configChange.getChangedConfigKeys();
        List<MasterDsProperties> properties = new ArrayList<>();
        String PREFIX = "msb.mybatis.config.datasource.";
        boolean matches = changedKeys.stream().anyMatch((keyx) -> keyx.startsWith(PREFIX) || keyx.startsWith("mybatis.config."));
        if (matches) {
            if (StringUtils.isNotBlank(this.env.getProperty("msb.mybatis.config.datasource.name"))) {
                MasterDsProperties dsProperties = MyBatisConfigLoadUtil.loadSingleConfig(this.env, MasterDsProperties.class);
                properties.add(dsProperties);
            } else {
                log.error("warn:未找到数据源Spring Bean名称,数据源无法更新,确认msb.mybatis.config.datasource.name是否配置");
            }
        }
        Set<Integer> updateConfigs = new HashSet<>();
        Iterator var8 = changedKeys.iterator();
        while(true) {
            String key;
            do {
                if (!var8.hasNext()) {
                    return properties;
                }
                key = (String)var8.next();
            } while(!MsbEnvironmentMgr.MYBATIS_CONFIGS_PREFIX_REGULAR.matcher(key).matches() && !MsbEnvironmentMgr.MYBATIS_OLD_CONFIGS_PREFIX_REGULAR.matcher(key).matches());
            Integer index = Integer.valueOf(key.substring(key.indexOf("[") + 1, key.indexOf("]")));
            if (updateConfigs.add(index)) {
                String nameKey = String.format("msb.mybatis.configs[%s].datasource.name", index);
                String configName = this.env.getProperty(nameKey);
                if (StringUtils.isNotBlank(configName)) {
                    MasterDsProperties dsProperties = MyBatisConfigLoadUtil.loadConfigByIndex(this.env, index, MasterDsProperties.class);
                    properties.add(dsProperties);
                } else {
                    log.error("warn:未找到数据源Spring Bean名称,数据源无法更新,确认{}是否配置", nameKey);
                }
            }
        }
    }
}
