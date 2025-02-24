package com.github.bannirui.msb.web.session;

import com.github.bannirui.msb.env.MsbEnvironmentMgr;
import com.github.bannirui.msb.web.filter.User;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.security.core.context.SecurityContext;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * session缓存的内存实现
 */
public class MapSessionStorageImpl implements ISessionStorage {
    private Cache<String, Map<String, Object>> sessionMap = null;
    private Cache<String, Set<String>> openIdSessionIdMap = null;

    public MapSessionStorageImpl(ConfigurableEnvironment env) {
        Integer defaultMaxInactiveInterval = null;
        if (MsbEnvironmentMgr.getProperty(env, "sso.maxInactiveInterval") != null) {
            defaultMaxInactiveInterval = Integer.parseInt(MsbEnvironmentMgr.getProperty(env, "sso.maxInactiveInterval"));
        } else {
            defaultMaxInactiveInterval = 1800;
        }
        this.sessionMap = Caffeine.newBuilder().expireAfterAccess((long) defaultMaxInactiveInterval, TimeUnit.SECONDS).build();
        this.openIdSessionIdMap = Caffeine.newBuilder().expireAfterAccess((long) defaultMaxInactiveInterval, TimeUnit.SECONDS).build();
    }

    @Override
    public void remove(String id) {
        this.sessionMap.invalidate(id);
        String openId = this.getOpenId(id);
        if (openId != null) {
            this.openIdSessionIdMap.invalidate(openId);
        }
    }

    @Override
    public Map<String, Object> get(String id) {
        return this.sessionMap.getIfPresent(id);
    }

    @Override
    public void save(String id, Map<String, Object> sessionAttrs) {
        Map<String, Object> map = this.sessionMap.getIfPresent(id);
        if (map == null) {
            map = new HashMap<>();
        }
        map.putAll(sessionAttrs);
        String openId = this.getOpenId(id);
        if (openId != null) {
            Set<String> openIdSessionSet = this.openIdSessionIdMap.getIfPresent(openId);
            if (openIdSessionSet == null) {
                openIdSessionSet = new HashSet<>();
            }
            openIdSessionSet.add(id);
            this.openIdSessionIdMap.put(openId, openIdSessionSet);
        }
        this.sessionMap.put(id, map);
    }

    @Override
    public void ttl(String id, Integer maxInactiveInterval) {
        this.get(id);
    }

    @Override
    public void put(String id, String key, Object value) {
        Map<String, Object> map = this.sessionMap.getIfPresent(id);
        if (map == null) {
            map = new HashMap<>();
        }
        if ("SPRING_SECURITY_CONTEXT".equals(key) && value instanceof SecurityContext && ((SecurityContext) value).getAuthentication() instanceof User) {
            User user = (User) ((SecurityContext) value).getAuthentication();
            String openId = (String) user.getProperties("openid");
            if (openId != null) {
                Set<String> openIdSessionSet = this.openIdSessionIdMap.getIfPresent(openId);
                if (openIdSessionSet == null) {
                    openIdSessionSet = new HashSet<>();
                }
                openIdSessionSet.add(id);
                this.openIdSessionIdMap.put(openId, openIdSessionSet);
            }
        }
        map.put(key, value);
    }

    @Override
    public List<User> getUser(String openId) {
        Set<String> openIdSessionSet = this.openIdSessionIdMap.getIfPresent(openId);
        if (openIdSessionSet == null) {
            openIdSessionSet = new HashSet<>();
        }
        List<User> users = new ArrayList<>();
        for (String sessionId : openIdSessionSet) {
            Map<String, Object> map = this.sessionMap.getIfPresent(sessionId);
            if (Objects.isNull(map)) continue;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String k = entry.getKey();
                Object v = entry.getValue();
                if ("SPRING_SECURITY_CONTEXT".equals(k) && v instanceof SecurityContext && ((SecurityContext) v).getAuthentication() instanceof User user) {
                    users.add(user);
                }
            }
        }
        return users;
    }

    @Override
    public void updateUser(List<User> users) {
    }

    private String getOpenId(String sessionId) {
        Map<String, Object> map = this.sessionMap.getIfPresent(sessionId);
        if (Objects.isNull(map)) return null;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String k = entry.getKey();
            Object v = entry.getValue();
            if ("SPRING_SECURITY_CONTEXT".equals(k) && v instanceof SecurityContext && ((SecurityContext) v).getAuthentication() instanceof User user) {
                return (String) user.getProperties("openid");
            }
        }
        return null;
    }

    @Override
    public void remove(String id, String attributeName) {
        Map<String, Object> map = this.sessionMap.getIfPresent(id);
        if (map != null && map.containsKey(attributeName)) {
            map.remove(attributeName);
        }
    }

    @Override
    public void destroy() {
    }
}
