package com.github.bannirui.msb.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.springframework.core.io.ClassPathResource;

public class VersionUtil {

    /**
     * msb框架版本.
     */
    public static String getVersion() {
        String classpath = String.valueOf(VersionUtil.class.getResource("VersionUtil.class"));
        if (classpath.startsWith("jar:")) {
            return VersionUtil.getVersionFromJar();
        } else if (classpath.startsWith("file:")) {
            return VersionUtil.getVersionFromFile();
        } else {
            return null;
        }
    }

    private static String getVersionFromJar() {
        try {
            InputStream is = new ClassPathResource("/META-INF/maven/com.github.bannirui/msb-common/pom.properties").getInputStream();
            Properties properties = new Properties();
            properties.load(is);
            return properties.getProperty("version", null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getVersionFromFile() {
        try {
            File f = new ClassPathResource("/META-INF").getFile();
            String path = f.getParentFile().getParentFile().getAbsolutePath();
            File of = new File(path + File.separator + "maven-archiver" + File.separator + "pom.properties");
            if(of.exists()) {
                Properties properties = new Properties();
                InputStream is = new FileInputStream(of);
                properties.load(is);
                return properties.getProperty("version", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
