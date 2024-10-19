package com.github.bannirui.msb.common.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;
import java.lang.reflect.Type;
import java.security.Timestamp;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonUtil {
    private static final Logger log = LoggerFactory.getLogger(JsonUtil.class);
    private static final SerializerFeature[] FEATURES;
    private static SerializeConfig config;

    public JsonUtil() {
    }

    public static String toJSON(Object object) {
        return JSON.toJSONString(object, FEATURES);
    }

    public static byte[] toJsonBytes(Object object) {
        return JSON.toJSONBytes(object, FEATURES);
    }

    public static <T> T parse(String json, Class<T> clazz) {
        return JSON.parseObject(json, clazz);
    }

    public static <T> T parse(String json, Type type) {
        return JSON.parseObject(json, type, new Feature[0]);
    }

    public static <T> T parseNotThrowException(String json, Class<T> clazz) {
        T t = null;

        try {
            t = JSON.parseObject(json, clazz);
        } catch (Exception var4) {
            Exception e = var4;
            log.error(e.getMessage());
        }

        return t;
    }

    public static <T> List<T> parseArray(String json, Class<T> clazz) {
        return JSON.parseArray(json, clazz);
    }

    public static <T> List<T> parseArrayNotThrowException(String json, Class<T> clazz) {
        List<T> list = null;

        try {
            list = JSON.parseArray(json, clazz);
        } catch (Exception var4) {
            Exception e = var4;
            log.error(e.getMessage());
        }

        return list;
    }

    static {
        FEATURES = new SerializerFeature[]{SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNullListAsEmpty, SerializerFeature.WriteNullNumberAsZero, SerializerFeature.WriteNullBooleanAsFalse, SerializerFeature.WriteNullStringAsEmpty, SerializerFeature.DisableCircularReferenceDetect};
        config = new SerializeConfig();
        SimpleDateFormatSerializer df = new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss");
        config.put(Date.class, df);
        config.put(java.sql.Date.class, df);
        config.put(Timestamp.class, df);
    }
}
