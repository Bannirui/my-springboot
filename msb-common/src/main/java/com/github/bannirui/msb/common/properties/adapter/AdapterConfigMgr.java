package com.github.bannirui.msb.common.properties.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.LoggerFactory;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

/**
 * 为项目配置预留配置方式.
 * classpath:/application-adapter.properties
 * 自定义框架为了统一配置风格 会在各个项目框架或者中间件中起到中间层作用
 * 比如
 * <ul>
 *     <li>a.name的配置统一为msb.a.name</li>
 *     <li>b.name的配置统一为msb.b.name</li>
 * </ul>
 * 因此 当项目配置都加载到Spring中后把映射关系也作为配置加载到Spring容器中
 * 此时映射嵌套是为了将来使用 所以放到Spring之前检查下配置是否已经存在于Spring中
 * 比如msb.name=${a.name}而a.name并没有在Spring中 那么msb.name=${a.name}也就没有必要加载到Spring中了
 */
public abstract class AdapterConfigMgr {

    /**
     * 配置文件路径 其中配置了属性嵌套映射
     */
    private static final String FILE_PATH = "classpath:/application-adapter.properties";
    private static Pattern ARRAY_PATTERN = Pattern.compile("\\[([^]]+)");
    public static final String ARRAY_ADAPTER_SOURCE_NAME = "msb:AdapterPropertySource";


    public AdapterConfigMgr() {
    }

    /**
     * 加载到Spring容器中
     */
    public static void loadAdapterPropertySource(ConfigurableEnvironment env) {
        /**
         * 比如msb.name=${name}
         * key=msb.name
         * val=${name}
         */
        Map<String, Object> adapterSource = new HashMap<>();
        /**
         * 比如msb.name=${name}
         * key=name
         * val=msb.name
         */
        Map<String, String> old2NewMap = new HashMap<>();
        DefaultResourceLoader defaultResourceLoader = new DefaultResourceLoader();
        Resource resource = defaultResourceLoader.getResource(FILE_PATH);
        Properties properties = null;
        try {
            properties = PropertiesLoaderUtils.loadProperties(resource);
            properties.forEach((newKey, oldKeyPlaceHolder) -> {
                String oldKey =
                    ((String) oldKeyPlaceHolder).substring(((String) oldKeyPlaceHolder).indexOf("${") + 2, ((String) oldKeyPlaceHolder).indexOf('}'));
                if (extractIndex(oldKey).size() > 0) {
                    old2NewMap.put(oldKey, (String) newKey);
                } else if (env.getProperty(oldKey) != null) {
                    adapterSource.put((String) newKey, oldKeyPlaceHolder);
                }
            });
        } catch (Exception e) {
            LoggerFactory.getLogger(AdapterConfigMgr.class).error("could not find file: {}", FILE_PATH);
        }
        List<? extends EnumerablePropertySource<?>> sources = (List) env.getPropertySources().stream().filter((propertySource) -> {
            return propertySource instanceof EnumerablePropertySource;
        }).map((item) -> {
            return (EnumerablePropertySource) item;
        }).collect(Collectors.toList());
        Map<String, EnumerablePropertySource<?>> firstArraySourceAdapterRecord = new HashMap<>();
        for (EnumerablePropertySource<?> source : sources) {
            String[] propertyNames = source.getPropertyNames();
            for (String propertyName : propertyNames) {
                List<String> indexs = extractIndex(propertyName);
                if (indexs.size() > 0) {
                    String oldKeyTemplate = ARRAY_PATTERN.matcher(propertyName).replaceAll("[%s");
                    String newKeyTemplate = (String) old2NewMap.get(oldKeyTemplate);
                    if (Objects.nonNull(newKeyTemplate)) {
                        String prefix = oldKeyTemplate.substring(0, oldKeyTemplate.indexOf("[") + 1);
                        EnumerablePropertySource<?> firstArraySource = firstArraySourceAdapterRecord.computeIfAbsent(prefix, (k) -> source);
                        if (Objects.equals(firstArraySource.getName(), source.getName())) {
                            String newKey = String.format(newKeyTemplate, indexs.toArray());
                            adapterSource.putIfAbsent(newKey, "${" + propertyName + "}");
                        }
                    }
                }
            }
        }
        if (adapterSource.size() > 0) {
            env.getPropertySources().addLast(new OriginTrackedMapPropertySource(ARRAY_ADAPTER_SOURCE_NAME, adapterSource));
        }
    }

    public static List<String> extractIndex(String msg) {
        List<String> list = new ArrayList<>(1);
        Matcher m = ARRAY_PATTERN.matcher(msg);
        while (m.find()) {
            list.add(m.group(1));
        }
        return list;
    }
}
