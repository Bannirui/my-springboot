package com.github.bannirui.msb.endpoint.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.bannirui.msb.endpoint.dump.ThreadDumpEndpoint;
import com.github.bannirui.msb.endpoint.health.Health;
import com.github.bannirui.msb.endpoint.health.HealthIndicator;
import com.github.bannirui.msb.endpoint.info.InfoProvider;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndpointManager {
    public static final Logger logger = LoggerFactory.getLogger(EndpointManager.class);
    private static final String HEALTH_URL = "/health";
    private static final String ENV_URL = "/env";
    private static final String THREADDUMP_URL = "/threaddump";
    private static final Map<String, HealthIndicator> HEALTH_INDICATOR_HASH_MAP = new HashMap<>();
    private static final Map<String, InfoProvider> INFO_PROVIDER_MAP = new HashMap<>();
    private static String authorization;
    private static final String LOCAL_HOST = "127.0.0.1";
    private static final ExecutorService POOL_EXECUTOR;

    static {
        POOL_EXECUTOR = new ThreadPoolExecutor(1, 2, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue(128), (new ThreadFactoryBuilder()).setNameFormat("endpoint-pool-%d").build(), new ThreadPoolExecutor.AbortPolicy());
    }

    private EndpointManager() {
        throw new IllegalStateException("Utility class");
    }

    public static void registerHealth(String key, HealthIndicator health) {
        HEALTH_INDICATOR_HASH_MAP.put(key, health);
    }

    public static void registerInfoProvider(String key, InfoProvider infoProvider) {
        INFO_PROVIDER_MAP.put(key, infoProvider);
    }

    public static void setAuthorization(String authorization) {
        EndpointManager.authorization = authorization;
    }

    public static String dispatcher(String clientAddress, String url, String auth) {
        if (auth != null && auth.contains("Basic ")) {
            String basic = new String(Base64.getDecoder().decode(auth.split(" ")[1]));
            if (!authorization.equals(basic)) {
                return "401";
            } else if (!"/health".equals(url)) {
                if ("127.0.0.1".equals(clientAddress)) {
                    if ("/env".equals(url)) {
                        if (INFO_PROVIDER_MAP.get("env") != null) {
                            return INFO_PROVIDER_MAP.get("env").info();
                        }
                        return "There has no Enviroment Info Provider!";
                    }
                    if ("/threaddump".equals(url)) {
                        ThreadDumpEndpoint.ThreadDumpDescriptor threadDump = (new ThreadDumpEndpoint()).threadDump();
                        return JSON.toJSONString(threadDump, SerializerFeature.PrettyFormat);
                    }
                }
                return "404";
            } else {
                Map<String, Object> map = new HashMap<>();
                map.put("status", "UP");
                Map<String, Future<Health>> futures = new HashMap<>();
                for (Map.Entry<String, HealthIndicator> entry : HEALTH_INDICATOR_HASH_MAP.entrySet()) {
                    try {
                        Future<Health> future = POOL_EXECUTOR.submit(() -> entry.getValue().health());
                        futures.put(entry.getKey(), future);
                    } catch (Exception e) {
                        logger.error("{} HealthIndicator", entry.getKey(), e);
                    }
                }
                for (Map.Entry<String, Future<Health>> entry : futures.entrySet()) {
                    try {
                        Health health = entry.getValue().get(30L, TimeUnit.SECONDS);
                        map.put(entry.getKey(), health);
                    } catch (Exception e) {
                        map.put(entry.getKey(), Health.build().down());
                        logger.error("{} HealthIndicator", entry.getKey(), e);
                    }
                }
                return JSON.toJSONString(map, SerializerFeature.PrettyFormat);
            }
        } else {
            return "401";
        }
    }

    public static Map<String, HealthIndicator> getHealthIndicatorHashMap() {
        return HEALTH_INDICATOR_HASH_MAP;
    }
}
