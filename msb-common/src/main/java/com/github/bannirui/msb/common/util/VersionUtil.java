package com.github.bannirui.msb.common.util;

import java.util.Properties;

public class VersionUtil {

    /**
     * msg框架版本.
     */
    public static String getVersion() {
        String version = null;
        try {
            Properties properties = new Properties();
            properties.load(VersionUtil.class.getResourceAsStream("/META-INF/maven/com/github/bannirui/msb-common/pom.properties"));
            version = properties.getProperty("version");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return version;
    }
}
