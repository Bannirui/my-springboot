package com.github.bannirui.msb.orm.util;

import com.github.bannirui.msb.properties.bind.PropertyBinder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

public class MyBatisConfigLoadUtil {
    /**
     * 加载单数据源配置
     * <ul>
     *     <li>数据源名称 mybatis.config.datasource.name</li>
     *     <li>mybatis.config.datasource.hikari.jdbcUrl</li>
     *     <li>mybatis.config.datasource.hikari.username</li>
     *     <li>mybatis.config.datasource.hikari.password</li>
     *     <li>mybatis.config.datasource.hikari.driverClassName</li>
     * </ul>
     */
    public static <T> T loadSingleConfig(Environment env, Class<T> target) {
        PropertyBinder propertyBinder = new PropertyBinder((ConfigurableEnvironment) env);
        BindResult<String> configName = propertyBinder.bind("mybatis.config.datasource.name", String.class);
        if (Objects.isNull(configName)) {
            return null;
        }
        BindResult<T> bindResult = Binder.get(env).bind("mybatis.config.datasource", target);
        return bindResult.orElse( null);
    }

    public static <T> T loadConfigByIndex(Environment env, int index, Class<T> target) {
        String configName = env.getProperty(String.format("titans.mybatis.configs[%s].datasource.name", index));
        if (StringUtils.isEmpty(configName)) {
            return null;
        } else {
            BindResult<T> bindResult = Binder.get(env).bind(String.format("titans.mybatis.configs[%s].datasource", index), target);
            return bindResult.orElse(null);
        }
    }

    public static <T> List<T> loadMultipleConfig(Environment env, Class<T> target) {
        Set<String> configNames = new HashSet<>();
        List<T> configs = new ArrayList<>();
        int index = 0;
        while(true) {
            String configName = env.getProperty(String.format("titans.mybatis.configs[%s].datasource.name", index));
            if (StringUtils.isEmpty(configName)) {
                return configs;
            }
            if (configNames.add(configName)) {
                T targetConfig = loadConfigByIndex(env, index, target);
                configs.add(targetConfig);
            }
            ++index;
        }
    }

    public static <T> T loadConfigs(Environment env, String prefix, int index, Class<T> target) {
        BindResult<T> bindResult = Binder.get(env).bind(String.format(prefix, index), target);
        return bindResult.orElse(null);
    }

    public static <T> T loadConfig(Environment env, String prefix, Class<T> target) {
        BindResult<T> bindResult = Binder.get(env).bind(String.format(prefix), target);
        return bindResult.orElse(null);
    }
}
