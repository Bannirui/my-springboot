package com.github.bannirui.msb.web.session;

import java.util.Map;
import java.util.Set;

public class MapSessionStorageImpl implements ISessionStorage {
    private Cache<String, Map<String, Object>> sessionMap = null;
    private Cache<String, Set<String>> openIdSessionIdMap = null;

    public MapSessionStorageImpl(ConfigurableEnvironment env) {
        Integer defaultMaxInactiveInterval = null;
        if (EnvironmentManager.getProperty(env, "sso.maxInactiveInterval") != null) {
            defaultMaxInactiveInterval = Integer.parseInt(EnvironmentManager.getProperty(env, "sso.maxInactiveInterval"));
        } else {
            defaultMaxInactiveInterval = 1800;
        }

        this.sessionMap = Caffeine.newBuilder().expireAfterAccess((long)defaultMaxInactiveInterval, TimeUnit.SECONDS).build();
        this.openIdSessionIdMap = Caffeine.newBuilder().expireAfterAccess((long)defaultMaxInactiveInterval, TimeUnit.SECONDS).build();
    }

    public void remove(String id) {
        this.sessionMap.invalidate(id);
        String openId = this.getOpenId(id);
        if (openId != null) {
            this.openIdSessionIdMap.invalidate(openId);
        }

    }

    public Map<String, Object> get(String id) {
        return (Map)this.sessionMap.getIfPresent(id);
    }

    public void save(String id, Map<String, Object> sessionAttrs) {
        Map<String, Object> map = (Map)this.sessionMap.getIfPresent(id);
        if (map == null) {
            map = new HashMap();
        }

        ((Map)map).putAll(sessionAttrs);
        String openId = this.getOpenId(id);
        if (openId != null) {
            Set<String> openIdSessionSet = (Set)this.openIdSessionIdMap.getIfPresent(openId);
            if (openIdSessionSet == null) {
                openIdSessionSet = new HashSet();
            }

            ((Set)openIdSessionSet).add(id);
            this.openIdSessionIdMap.put(openId, openIdSessionSet);
        }

        this.sessionMap.put(id, map);
    }

    public void ttl(String id, Integer maxInactiveInterval) {
        this.get(id);
    }

    public void put(String id, String key, Object value) {
        Map<String, Object> map = (Map)this.sessionMap.getIfPresent(id);
        if (map == null) {
            map = new HashMap();
        }

        if ("SPRING_SECURITY_CONTEXT".equals(key) && value instanceof SecurityContext && ((SecurityContext)value).getAuthentication() instanceof User) {
            User user = (User)((SecurityContext)value).getAuthentication();
            String openId = (String)user.getProperties("openid");
            if (openId != null) {
                Set<String> openIdSessionSet = (Set)this.openIdSessionIdMap.getIfPresent(openId);
                if (openIdSessionSet == null) {
                    openIdSessionSet = new HashSet();
                }

                ((Set)openIdSessionSet).add(id);
                this.openIdSessionIdMap.put(openId, openIdSessionSet);
            }
        }

        ((Map)map).put(key, value);
    }

    public List<User> getUser(String openId) {
        Set<String> openIdSessionSet = (Set)this.openIdSessionIdMap.getIfPresent(openId);
        if (openIdSessionSet == null) {
            openIdSessionSet = new HashSet();
        }

        List<User> users = new ArrayList();
        Iterator var4 = ((Set)openIdSessionSet).iterator();

        while(true) {
            Map map;
            do {
                if (!var4.hasNext()) {
                    return users;
                }

                String sessionId = (String)var4.next();
                map = (Map)this.sessionMap.getIfPresent(sessionId);
            } while(map == null);

            Iterator var7 = map.entrySet().iterator();

            while(var7.hasNext()) {
                Entry<String, Object> entry = (Entry)var7.next();
                String k = (String)entry.getKey();
                Object v = entry.getValue();
                if ("SPRING_SECURITY_CONTEXT".equals(k) && v instanceof SecurityContext && ((SecurityContext)v).getAuthentication() instanceof User) {
                    User user = (User)((SecurityContext)v).getAuthentication();
                    users.add(user);
                }
            }
        }
    }

    public void updateUser(List<User> users) {
    }

    private String getOpenId(String sessionId) {
        Map<String, Object> map = (Map)this.sessionMap.getIfPresent(sessionId);
        if (map != null) {
            Iterator var3 = map.entrySet().iterator();

            while(var3.hasNext()) {
                Entry<String, Object> entry = (Entry)var3.next();
                String k = (String)entry.getKey();
                Object v = entry.getValue();
                if ("SPRING_SECURITY_CONTEXT".equals(k) && v instanceof SecurityContext && ((SecurityContext)v).getAuthentication() instanceof User) {
                    User user = (User)((SecurityContext)v).getAuthentication();
                    String openid = (String)user.getProperties("openid");
                    return openid;
                }
            }
        }

        return null;
    }

    public void remove(String id, String attributeName) {
        Map<String, Object> map = (Map)this.sessionMap.getIfPresent(id);
        if (map != null && map.containsKey(attributeName)) {
            map.remove(attributeName);
        }

    }

    public void destroy() {
    }
}
