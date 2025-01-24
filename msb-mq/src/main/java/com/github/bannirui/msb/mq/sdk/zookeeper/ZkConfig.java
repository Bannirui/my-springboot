package com.github.bannirui.msb.mq.sdk.zookeeper;

import com.github.bannirui.msb.mq.sdk.common.MmsException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZkConfig {
    private static final Logger LOGGER= LoggerFactory.getLogger(ZkConfig.class);
    private static final String CONFIG_PATH = "/mms.properties";
    private static final Properties PROPERTIES;

    static {
        PROPERTIES = new Properties();
    }

    public static String getZkAddress(String env) {
        load();
        String zkAddress = PROPERTIES.getProperty("mms.zk." + env.toLowerCase());
        return null == zkAddress ? PROPERTIES.getProperty("mms.zk.test") : zkAddress;
    }

    private static void load() {
        if (PROPERTIES.isEmpty()) {
            synchronized(PROPERTIES) {
                if (PROPERTIES.isEmpty()) {
                    try {
                        try (InputStream inputStream = ZkConfig.class.getResourceAsStream("/mms.properties")) {
                            PROPERTIES.load(inputStream);
                        } catch (Throwable e) {
                            throw e;
                        }
                    } catch (IOException e) {
                        LOGGER.error("cannot load mms.properties", e);
                        throw MmsException.NO_MMS_PROFILE_EXCEPTION;
                    }
                }
            }
        }
    }
}
