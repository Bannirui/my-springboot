package com.github.bannirui.msb.common.env;

import com.github.bannirui.msb.common.enums.ExceptionEnum;
import com.github.bannirui.msb.common.ex.ErrorCodeException;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * 配置信息处理.
 * 用于协助Spring Environment.
 */
public class EnvironmentMgr {

    private static String appName;
    private static Map<String, String> apolloMap;
    private static Properties properties;

    private static void init() {
        String osName = System.getProperty("os.name");
        if(osName !=null && osName.toLowerCase().contains("linux")) {
            System.setProperty("java.security.egd", "file:/dev/./urandom");
        }
        System.setProperty("spring.banner.location", "classpath*:/msb.txt");
        System.setProperty("es.set.netty.runtime.available.processor", "false");
    }

    private static void initMsbConfig() {
        String netEnv = getNetEnv();
        String filePath = StringUtils.hasText(netEnv) ? "classpath:META-INF/msb/" + netEnv + "/env-" + getEnv() + ".properties" : "classpath:META-INF/msb/env-" + getEnv() + ".properties";
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource resource = resolver.getResource(filePath);
            properties = new Properties();
            properties.load(resource.getInputStream());
        } catch (IOException e) {
            throw new ErrorCodeException(e, ExceptionEnum.INITIALIZATION_EXCEPTION, new Object[] {"资源文件"});
        }
        try {
            String url = properties.getProperty("apollo.meta.url");
            String uri = properties.getProperty("apollo.msb.uri");
            String ret = readApolloConfig(new ArrayList<String>(){{add(url);}}, uri);
            apolloMap = parseApolloStrRet(ret);
            createOrModifyCacheFile(ret);
        } catch (Exception e) {
            logger.error("读取msb apollo错误", e);
            apolloMap = readMsbCacheConfig();
        }
    }

    public static String getProperty(ConfigurableEnvironment env, String key) {
        return env.getProperty(key);
    }

    public static String getEnv() {
        String env = System.getProperty("env");
        return Objects.isNull(env) ? "dev" : env;
    }

    public static String getNetEnv() {
        return System.getProperty("netEnv");
    }

    public static String getAppName() {
        if(Objects.isNull(appName)) {
            String appId = System.getProperty("app.id");
            if(Objects.nonNull(appId)) {
                appName = appId;
            } else {
                Properties props = new Properties();
                try {
                    props.load(EnvironmentMgr.class.getResourceAsStream("/META-INF/app.properties"));
                } catch (Exception e) {
                    throw new ErrorCodeException(e, ExceptionEnum.FILE_EXCEPTION,new Object[]{"读取app.properties"});
                }
                appName = props.getProperty("app.id");
            }
        }
        return appName;
    }

    public static void addMsbConfig2PropertySource(ConfigurableEnvironment env) {
        addMsbApolloConfig2PropertySource(env);
        addMsbFileConfig2PropertySource(env);
    }

    private static void addMsbApolloConfig2PropertySource(ConfigurableEnvironment env) {
        OriginTrackedMapPropertySource ps = new OriginTrackedMapPropertySource("msbApolloConfig: ["+getEnv()+"]", apolloMap);
        env.getPropertySources().addLast(ps);
    }

    private static void addMsbFileConfig2PropertySource(ConfigurableEnvironment env) {
        OriginTrackedMapPropertySource ps=new OriginTrackedMapPropertySource("msbFileConfig: ["+getEnv()+"]",properties);
        env.getPropertySources().addLast(ps);
    }

    private static String readApolloConfig(List<String> configNodesUrls, String uri) throws IOException {
        int limit=3;
        return recursiveReadApolloConfig(configNodesUrls, uri,0,0,limit);
    }

    private static String recursiveReadApolloConfig(List<String> urls, String uri, int curPos, int curCnt, int cntLimit) {
        return null;
    }
}
