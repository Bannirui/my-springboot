package com.github.bannirui.msb.mq.sdk.zookeeper;

import com.github.bannirui.mms.common.MmsException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZkConfig {
    private static final Logger logger = LoggerFactory.getLogger(ZkConfig.class);
    private static final String CONFIG_PATH = "/mms.properties";
    private static final Properties properties;

    static {
        properties = new Properties();
    }

    public static String getZkAddress(String env) {
        load();
        String zkAddress = properties.getProperty("mms.zk." + env.toLowerCase());
        return null == zkAddress ? properties.getProperty("mms.zk.test") : zkAddress;
    }

    private static void load() {
        if (properties.isEmpty()) {
            synchronized(properties) {
                if (properties.isEmpty()) {
                    try {
                        try (InputStream inputStream = ZkConfig.class.getResourceAsStream("/mms.properties")) {
                            properties.load(inputStream);
                        } catch (Throwable e) {
                            throw e;
                        }
                    } catch (IOException e) {
                        logger.error("cannot load mms.properties", e);
                        throw MmsException.NO_MMS_PROFILE_EXCEPTION;
                    }
                }
            }
        }
    }
}
