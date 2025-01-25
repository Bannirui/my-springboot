package com.github.bannirui.msb.mq.sdk.utils;

import com.github.bannirui.msb.common.ex.FrameworkException;
import com.github.bannirui.msb.mq.sdk.common.MmsEnv;
import com.google.common.collect.Maps;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {
    private static final Logger LOGGER= LoggerFactory.getLogger(Utils.class);

    public static String getMmsVersion() {
        Utils object = new Utils();
        Package objPackage = object.getClass().getPackage();
        String version = objPackage.getImplementationVersion();
        if (!StringUtils.isEmpty(version)) {
            return version;
        }
        version = objPackage.getSpecificationVersion();
        return version;
    }

    /**
     * 字符串形式配置解析成{@link Properties}形式
     * @param source Properties标准格式的字符串 每对配置用换行符
     */
    public static Properties parseProperties(String source) throws IOException {
        Properties properties = new Properties();
        if(StringUtils.isEmpty(source)) {
            return properties;
        }
        try {
            properties.load(new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8)));
            return properties;
        } catch (IOException e) {
            throw FrameworkException.getInstance("str转Properties失败{0}", e);
        }
    }

    public static String buildPath(String... args) {
        return args.length < 1 ? "" : String.join("/", args);
    }

    public static String separatePath(String path, String... args) {
        return path.replace(String.join("/", args).concat("/"), "");
    }

    public static final String buildName(String name) {
        return MmsEnv.MMS_IP + "||" + name + "||" + MmsEnv.MMS_VERSION + "||" + LocalDateTime.now() + "||" + ThreadLocalRandom.current().nextInt(100000);
    }

    public static <T> String toString(List<T> lists) {
        if (CollectionUtils.isEmpty(lists)) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder("[ ");
        for (T list : lists) {
            stringBuilder.append(list.toString());
            stringBuilder.append(",");
        }
        return stringBuilder.substring(0, stringBuilder.length() - 1) + " ]";
    }

    public static <T, V> String toString(Map<T, V> maps) {
        if (MapUtils.isEmpty(maps)) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder("[ ");
        maps.forEach((key, value) -> {
            stringBuilder.append(key.toString());
            stringBuilder.append(":");
            stringBuilder.append(value.toString());
            stringBuilder.append(",");
        });
        return stringBuilder.substring(0, stringBuilder.length() - 1) + " ]";
    }

    public static String abbrev(TimeUnit unit) {
        switch(unit) {
            case NANOSECONDS:
                return "ns";
            case MICROSECONDS:
                return "us";
            case MILLISECONDS:
                return "ms";
            case SECONDS:
                return "s";
            case MINUTES:
                return "m";
            case HOURS:
                return "h";
            case DAYS:
                return "d";
            default:
                throw new IllegalArgumentException("Unrecognized TimeUnit: " + unit);
        }
    }

    public static void gracefullyShutdown(ExecutorService pool) {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(1L, TimeUnit.SECONDS)) {
                pool.shutdownNow();
                if (!pool.awaitTermination(1L, TimeUnit.SECONDS)) {
                    LOGGER.info("kafka consumer pool did not terminate");
                }
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static Map<Integer, Integer> getIndex(int totalCount, int defaultBatchCount) {
        return getIndex(totalCount, defaultBatchCount, 0);
    }

    public static Map<Integer, Integer> getIndex(int totalCount, int defaultBatchCount, int maxThreads) {
        int m = totalCount % defaultBatchCount;
        int pageCount;
        if (m > 0) {
            pageCount = totalCount / defaultBatchCount + 1;
        } else {
            pageCount = totalCount / defaultBatchCount;
        }
        int loopCount = pageCount - 1;
        int num = 0;
        if (maxThreads > 0 && pageCount > maxThreads) {
            loopCount = maxThreads;
            int remainder = totalCount - defaultBatchCount * maxThreads;
            if (remainder > maxThreads) {
                num = remainder / maxThreads;
                num = getAddNum(totalCount, defaultBatchCount, maxThreads, num);
            } else {
                num = 1;
            }
        }
        Map<Integer, Integer> maps = Maps.newTreeMap();
        for(int i = 1; i <= loopCount + 1; ++i) {
            int startBatch = (i - 1) * (defaultBatchCount + num);
            int endBatch = Math.min((defaultBatchCount + num) * i, totalCount);
            if (startBatch < endBatch) {
                maps.put(startBatch, endBatch);
            }
        }
        return maps;
    }

    private static int getAddNum(int totalCount, int defaultBatchCount, int loopCount, int num) {
        if ((loopCount + 1) * (defaultBatchCount + num) < totalCount) {
            ++num;
            return getAddNum(totalCount, defaultBatchCount, loopCount, num);
        } else {
            return num;
        }
    }

    public static <T> List<T> getSubList(List<T> list, int from, int maxNum) {
        return list != null && list.size() > from ? new ArrayList<>(list.subList(from, Math.min(maxNum, list.size()))) : new ArrayList<>();
    }
}
