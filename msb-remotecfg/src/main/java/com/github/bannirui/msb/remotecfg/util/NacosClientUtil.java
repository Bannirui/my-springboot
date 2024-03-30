package com.github.bannirui.msb.remotecfg.util;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import java.util.Properties;

/**
 * nacos sdk.
 */
public class NacosClientUtil {

    /**
     * 获取配置中心配置项.
     */
    public static String getConfig(String serverAddr, String dataId, String group) {
        try {
            Properties properties = new Properties();
            properties.put(PropertyKeyConst.SERVER_ADDR, serverAddr);
            ConfigService configService = NacosFactory.createConfigService(properties);
            return configService.getConfig(dataId, group, 5000);
        } catch (NacosException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取配置中心配置项.
     */
    public static String getConfig(String serverAddr, String dataId) {
        return getConfig(serverAddr, dataId, "DEFAULT_GROUP");
    }
}
