package com.github.bannirui.msb.env;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.github.bannirui.msb.constant.AppCfg;
import com.github.bannirui.msb.constant.EnvType;
import com.github.bannirui.msb.enums.ExceptionEnum;
import com.github.bannirui.msb.ex.ErrorCodeException;
import com.github.bannirui.msb.ex.FrameworkException;
import com.github.bannirui.msb.util.FileUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.ObjectUtils;

/**
 * 配置信息处理.
 * 用于协助Spring Environment.
 * 只限于对msb框架配置处理 业务应用配置信息通过{@link org.springframework.beans.factory.annotation.Value}或者{@link org.springframework.boot.context.properties.bind.Binder}处理.
 */
public class MsbEnvironmentMgr {

    public static final Pattern MYBATIS_CONFIGS_PREFIX_REGULAR;
    public static final Pattern SHARDING_PREFIX_REGULAR;
    public static final Pattern SHARDING_TABLE_PREFIX_REGULAR;
    private static final Logger logger = LoggerFactory.getLogger(MsbEnvironmentMgr.class);
    private static final String env_key = "env";
    private static final String net_env_key = "netEnv";
    private static String USER_HOME_PATH = System.getProperty("user.home");
    private static String MSB_FILE_PATH;

    private static String appName;
    /**
     * 为msb框架配置了远程配置 拉下来缓存
     */
    private static Map<String, String> apolloMap;
    /**
     * msb框架项目的配置 从本地配置文件中加载到了内存
     */
    private static Properties properties;

    static {
        MYBATIS_CONFIGS_PREFIX_REGULAR = Pattern.compile("^mybatis\\.configs\\[(\\d+)\\]\\.datasource\\..*\\S");
        SHARDING_PREFIX_REGULAR = Pattern.compile("^sharding\\.datasources\\[(\\d+)\\]\\..*\\S");
        SHARDING_TABLE_PREFIX_REGULAR = Pattern.compile("^sharding\\.tableConfigs\\[(\\d+)\\]\\..*\\S");
        MSB_FILE_PATH = USER_HOME_PATH + "/.msb/" + getEnv() + "-msb.json";
        apolloMap = new HashMap<>();
        init();
        initMsbConfig();
    }

    private MsbEnvironmentMgr() {
        throw new IllegalStateException("Utility class");
    }

    private static void init() {
        String osName = System.getProperty("os.name");
        if (osName != null && osName.toLowerCase().contains("linux")) {
            System.setProperty("java.security.egd", "file:/dev/./urandom");
        }
        System.setProperty("spring.banner.location", "classpath*:/msb.txt");
        System.setProperty("es.set.netty.runtime.available.processor", "false");
    }

    private static void initMsbConfig() {
        String netEnv = getNetEnv();
        String filePath =
            (Objects.nonNull(netEnv) && StringUtils.isNotBlank(netEnv)) ?
                "classpath:META-INF/msb/" + netEnv + "/env-" + getEnv() + ".properties" :
                "classpath:META-INF/msb/env-" + getEnv() + ".properties";
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            // 加载当前module的本地配置文件内容
            Resource resource = resolver.getResource(filePath);
            properties = new Properties();
            properties.load(resource.getInputStream());
        } catch (IOException e) {
            throw new ErrorCodeException(e, ExceptionEnum.INITIALIZATION_EXCEPTION, "资源文件");
        }
        if (!Objects.equals("false", System.getProperty("msb.apollo"))) {
            // msb框架本身的配置放在了apollo 从远程拉配置到本地
            String url = properties.getProperty("apollo.meta.url");
            String uri = properties.getProperty("apollo.msb.uri");
            if (StringUtils.isNotBlank(url) && StringUtils.isNotBlank(uri)) {
                try {
                    String ret = readApolloConfig(Collections.singletonList(url), uri);
                    if (StringUtils.isBlank(ret)) {
                        return;
                    }
                    // 从apollo远程拉下来的配置结果解析缓存起来
                    apolloMap = parseApolloStrRet(ret);
                    createOrModifyCacheFile(ret);
                } catch (Exception e) {
                    logger.error("读取msb apollo错误", e);
                    apolloMap = readMsbCacheConfig();
                }
            }
        }
    }

    /**
     * 配置优先级
     * <ul>
     *     <li>jvm环境变量</li>
     *     <li>远程配置</li>
     *     <li>本地配置</li>
     * </ul>
     */
    public static String getProperty(String key) {
        String ret = null;
        if (Objects.nonNull(ret = System.getProperty(key))) {
            return ret;
        }
        if (Objects.nonNull(ret = apolloMap.get(key))) {
            return ret;
        }
        return properties.getProperty(key);
    }

    public static String getProperty(ConfigurableEnvironment env, String key) {
        return env.getProperty(key);
    }

    public static void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    /**
     * -Denv=dev JVM启动参数指定.
     */
    public static String getEnv() {
        String env = System.getProperty(env_key);
        return Objects.isNull(env) ? EnvType.DEV : env;
    }

    public static String getNetEnv() {
        return System.getProperty(net_env_key);
    }

    /**
     * 获取应用名称.
     * 优先级为
     * <ul>
     *     <li>启动参数-Dapp.id</li>
     *     <li>应用配置文件/META-INF/app.properties</li>
     * </ul>
     */
    public static String getAppName() {
        if (Objects.isNull(appName)) {
            String appId = System.getProperty(AppCfg.APP_ID_KEY);
            if (Objects.nonNull(appId)) {
                appName = appId;
            } else {
                Properties props = new Properties();
                try {
                    props.load(MsbEnvironmentMgr.class.getResourceAsStream("/META-INF/app.properties"));
                } catch (Exception e) {
                    throw new ErrorCodeException(e, ExceptionEnum.FILE_EXCEPTION, "读取app.properties");
                }
                appName = props.getProperty(AppCfg.APP_ID_KEY);
            }
        }
        return appName;
    }

    /**
     * msb框架项目的配置加载到容器中
     * <ul>
     *     <li>项目有远程配置就把远程配置拉下来</li>
     *     <li>项目有本地配置就把本地配置解析出来</li>
     * </ul>
     */
    public static void addMsbConfig2PropertySource(ConfigurableEnvironment env) {
        addMsbApolloConfig2PropertySource(env);
        addMsbFileConfig2PropertySource(env);
    }

    /**
     * msb项目的远程配置加载到容器中.
     */
    private static void addMsbApolloConfig2PropertySource(ConfigurableEnvironment env) {
        OriginTrackedMapPropertySource ps = new OriginTrackedMapPropertySource("msbApolloConfig: [" + getEnv() + "]", apolloMap);
        env.getPropertySources().addLast(ps);
    }

    /**
     * msb项目的本地配置加载到容器中.
     */
    private static void addMsbFileConfig2PropertySource(ConfigurableEnvironment env) {
        OriginTrackedMapPropertySource ps = new OriginTrackedMapPropertySource("msbFileConfig: [" + getEnv() + "]", properties);
        env.getPropertySources().addLast(ps);
    }

    private static List<String> getApolloConfigNode(String apolloMetaUrl) throws IOException {
        String nodes = httpGet(apolloMetaUrl);
        if (StringUtils.isEmpty(nodes)) {
            logger.error("MSB can not find apollo nodes from url: {}, response string is null!", apolloMetaUrl);
            throw new FrameworkException(FrameworkException.ERR_DEF, "MSB can not find apollo nodes");
        } else {
            JSONArray jsonArr = JSONArray.parseArray(nodes);
            if (jsonArr.isEmpty()) {
                logger.error("MSB can not find apollo nodes from url: {}", apolloMetaUrl);
                throw new FrameworkException(FrameworkException.ERR_DEF, "MSB can not find apollo nodes");
            } else {
                List<String> configHomeUrls = new ArrayList<>();
                int sz = jsonArr.size();
                for (int i = 0; i < sz; i++) {
                    configHomeUrls.add(jsonArr.getJSONObject(i).getString("homepageUrl"));
                }
                return configHomeUrls;
            }
        }
    }

    /**
     * apollo配置中心中msb项目的内容.
     *
     * @param configNodesUrls apollo的服务域名
     * @param uri             配置文件的路径
     * @return {"appId":"msb","cluster":"default","namespaceName":"application","configurations":{"mysql.name":"mysql"},"releaseKey":"20241024131541-240add83aef7df27"}
     */
    private static String readApolloConfig(List<String> configNodesUrls, String uri) throws IOException {
        int limit = 1;
        return recursiveReadApolloConfig(configNodesUrls, uri, 0, 0, limit);
    }

    private static String recursiveReadApolloConfig(List<String> urls, String uri, int curPos, int curCnt, int cntLimit) {
        // base
        if (curCnt >= cntLimit) {
            return null;
        }
        try {
            return httpGet(urls.get(curPos) + uri);
        } catch (IOException e) {
            ++curPos;
            if (curPos >= urls.size()) {
                curPos = 0;
                ++curCnt;
            }
            return recursiveReadApolloConfig(urls, uri, curPos, curCnt, cntLimit);
        } catch (Exception e) {
            throw new FrameworkException(FrameworkException.ERR_DEF, "cannot read apollo config from url as " + urls.get(curPos));
        }
    }

    /**
     * 通过http的get请求获取apollo的配置信息.
     */
    private static String httpGet(String url) throws IOException {
        StringBuffer sb = new StringBuffer();
        URL readUrl = new URL(url);
        URLConnection conn = readUrl.openConnection();
        conn.setRequestProperty("accept", "*/*");
        conn.setRequestProperty("connection", "Keep-Alive");
        conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        conn.connect();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line = null;
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw e;
        }
        return sb.toString();
    }

    /**
     * msb项目本身的配置信息从apollo远程配置中心get请求下来解析出来
     *
     * @param ret {"appId":"msb","cluster":"default","namespaceName":"application","configurations":{"mysql.name":"mysql"},"releaseKey":"20241024131541-240add83aef7df27"}
     * @return {"mysql.name":"mysql"}
     */
    public static Map<String, String> parseApolloStrRet(String ret) {
        JSONObject jsonObject = JSONObject.parseObject(ret);
        return jsonObject.getObject("configurations", new TypeReference<Map<String, String>>() {
        });
    }

    public static Map<String, String> readMsbCacheConfig() {
        Map<String, String> apolloMap = new HashMap<>();
        File msbFile = new File(MSB_FILE_PATH);
        try {
            if (msbFile.exists() && FileUtil.canRead(msbFile)) {
                String ret = FileUtils.readFileToString(msbFile);
                if (StringUtils.isNotEmpty(ret)) {
                    apolloMap = parseApolloStrRet(ret.trim());
                } else {
                    logger.error("msb缓存配置读取失败");
                }
            }
        } catch (Exception e) {
            logger.error("msb缓存配置读取失败 err=", e);
        }
        return apolloMap;
    }

    /**
     * 将框架的远程配置同步到本地.
     */
    public static void createOrModifyCacheFile(String json) {
        File folderMsb = new File(USER_HOME_PATH + "/.msb");
        try {
            File file;
            File msbCfgFile;
            if (folderMsb.exists() && FileUtil.canRead(folderMsb) && FileUtil.canWrite(folderMsb)) {
                msbCfgFile = new File(MSB_FILE_PATH);
                if (msbCfgFile.exists()) {
                    // 更新本地文件
                    if (!ObjectUtils.isEmpty(json) && FileUtil.canWrite(msbCfgFile) && FileUtil.canRead(msbCfgFile)) {
                        FileUtil.deleteFile(msbCfgFile);
                        file = new File(MSB_FILE_PATH);
                        if (file.createNewFile()) {
                            FileUtils.writeStringToFile(file, json);
                        } else {
                            logger.error("createOrModifyCacheFile error,path:{}", MSB_FILE_PATH);
                        }
                    }
                } else {
                    // 新增本地文件
                    file = new File(MSB_FILE_PATH);
                    if (file.createNewFile()) {
                        FileUtils.writeStringToFile(file, json);
                    } else {
                        logger.error("createOrModifyCacheFile error,path:{}", MSB_FILE_PATH);
                    }
                }
            } else {
                msbCfgFile = new File(USER_HOME_PATH);
                if (FileUtil.canWrite(msbCfgFile)) {
                    folderMsb.mkdir();
                    file = new File(MSB_FILE_PATH);
                    if (file.createNewFile()) {
                        FileUtils.writeStringToFile(file, json);
                    } else {
                        logger.error("createOrModifyCacheFile error,path:{}", MSB_FILE_PATH);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("msb缓存配置文件创建或更新异常 错误信息={}", e.getMessage());
        }
    }

    public static Set<String> getAllKeys() {
        Set<String> ret = new HashSet<>(apolloMap.size());
        if (Objects.nonNull(properties)) {
            ret.addAll(properties.stringPropertyNames());
        }
        return ret;
    }
}
