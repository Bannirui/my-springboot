package com.github.bannirui.msb.web.session;

import com.alibaba.fastjson.JSON;
import com.github.bannirui.msb.util.DigestUtil;
import com.github.bannirui.msb.web.entity.UpdateUserResult;
import com.github.bannirui.msb.web.filter.User;
import com.github.bannirui.msb.web.util.HttpUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SessionRepositoryImpl implements SessionRepository<SessionRepositoryImpl.RedisSession>, DisposableBean {
    private Logger logger = LoggerFactory.getLogger(SessionRepositoryImpl.class);
    private ISessionStorage sessionStorage;
    private Duration defaultMaxInactiveInterval;
    private Duration ssoSessionMaxLife;
    private ScheduledExecutorService scheduler = null;
    private long fromTimeStamp = System.currentTimeMillis();

    public SessionRepositoryImpl(Environment environment, ISessionStorage sessionStorage) {
        ConfigurableEnvironment env = (ConfigurableEnvironment)environment;
        this.defaultMaxInactiveInterval = Binder.get(env).bind("session.redis.session-timeout", Duration.class).orElseGet(() -> {
            String sessionTimeout = env.getProperty("sso.maxInactiveInterval");
            if (StringUtils.isNotBlank(sessionTimeout)) {
                this.defaultMaxInactiveInterval = Duration.ofSeconds(Integer.parseInt(sessionTimeout));
            }
            return this.defaultMaxInactiveInterval;
        });
        String ssoSessionMaxLife = env.getProperty("sso.sessionMaxLifeTime", "604800");
        if (StringUtils.isNotBlank(ssoSessionMaxLife)) {
            this.ssoSessionMaxLife = Duration.ofSeconds(Integer.parseInt(ssoSessionMaxLife));
        }
        this.sessionStorage = sessionStorage;
        boolean enablePermissionFetch = env.getProperty("sso.enablePermissionFetch", Boolean.TYPE, true);
        if (enablePermissionFetch) {
            try {
                this.schedulePermissionUpdate(env);
            } catch (Exception e) {
                this.logger.error("启动拉取权限异常", e);
            }
        }
    }

    private void schedulePermissionUpdate(ConfigurableEnvironment env) {
        String appId = env.getProperty("sso.appId");
        String appSecret = env.getProperty("sso.secret");
        String permHistoryUrl = env.getProperty("sso.permHistoryUrl");
        String permissionFetchUrl = env.getProperty("sso.permissionFetchUrl");
        Integer fetchSize = 1_000;
        Long fetchInterval;
        try {
            fetchInterval = env.getProperty("sso.fetchInterval", Long.class);
        } catch (Exception e) {
            fetchInterval = 10L;
        }
        try {
            fetchSize = env.getProperty("sso.fetchSize", Integer.class);
        } catch (Exception e) {
            fetchSize = 1_000;
        }
        this.scheduler = new ScheduledThreadPoolExecutor(1);
        Integer finalFetchSize = fetchSize;
        this.scheduler.scheduleAtFixedRate(() -> {
            Map<String, Object> params = new HashMap<>();
            params.put("appid", appId);
            params.put("pagesize", finalFetchSize);
            params.put("from_timestamp", this.fromTimeStamp);
            try {
                String str = HttpUtils.doGet(permHistoryUrl, params, 1000, 2000);
                UpdateUserResult result = JSON.parseObject(str, UpdateUserResult.class);
                if (result.getTotal() > 0L) {
                    List<Map<String, Object>> users = result.getUsers();
                    for (Map<String, Object> user : users) {
                        String openId = (String)user.get("openid");
                        this.fromTimeStamp = (Long)user.get("updated_at");
                        List<User> existUserList = this.sessionStorage.getUser(openId);
                        if(CollectionUtils.isEmpty(existUserList)) continue;
                        String url = permissionFetchUrl + openId;
                        long timestamp = System.currentTimeMillis();
                        Map<String, String> headerMap = new HashMap<>();
                        headerMap.put("X-App-Id", appId);
                        headerMap.put("X-Sign-Timestamp", "" + timestamp);
                        headerMap.put("X-Sign", DigestUtil.digest("openid=" + openId + timestamp, appSecret, "UTF-8"));
                        String permissionStr = HttpUtils.doGet(url, null, "UTF-8", headerMap, 1_000, 2_000);
                        Map<String, Object> permissionMap = (Map) JSON.parseObject(permissionStr, HashMap.class);
                        for (User existUser : existUserList) {
                            existUser.updatePermissions(permissionMap);
                        }
                        this.sessionStorage.updateUser(existUserList);
                    }
                } else {
                    this.fromTimeStamp = System.currentTimeMillis() - 1_000L;
                }
            } catch (IOException e) {
                this.logger.error("更新权限数据异常", e);
            } catch (Exception e) {
                this.logger.error("更新权限数据异常", e);
            }
        }, 0L, fetchInterval, TimeUnit.SECONDS);
    }

    @Override
    public SessionRepositoryImpl.RedisSession createSession() {
        SessionRepositoryImpl.RedisSession session = new SessionRepositoryImpl.RedisSession();
        if (this.defaultMaxInactiveInterval != null) {
            session.setMaxInactiveInterval(this.defaultMaxInactiveInterval);
        }
        if (this.ssoSessionMaxLife != null) {
            session.setMaxLife(this.ssoSessionMaxLife);
        }
        return session;
    }

    @Override
    public void save(SessionRepositoryImpl.RedisSession session) {
    }

    @Override
    public SessionRepositoryImpl.RedisSession findById(String id) {
        Map<String, Object> map = this.sessionStorage.get(id);
        SessionRepositoryImpl.RedisSession session;
        if (MapUtils.isEmpty(map)) {
            session = new SessionRepositoryImpl.RedisSession(id);
            if (this.defaultMaxInactiveInterval != null) {
                session.setMaxInactiveInterval(this.defaultMaxInactiveInterval);
            }
            if (this.ssoSessionMaxLife != null) {
                session.setMaxLife(this.ssoSessionMaxLife);
            }
            return session;
        } else {
            session = new SessionRepositoryImpl.RedisSession(id, map);
            if (this.defaultMaxInactiveInterval != null) {
                session.setMaxInactiveInterval(this.defaultMaxInactiveInterval);
            }
            if (this.ssoSessionMaxLife != null) {
                session.setMaxLife(this.ssoSessionMaxLife);
            }
            if (session.isExpired()) {
                this.deleteById(id);
                session = new SessionRepositoryImpl.RedisSession(id);
                if (this.defaultMaxInactiveInterval != null) {
                    session.setMaxInactiveInterval(this.defaultMaxInactiveInterval);
                }
                if (this.ssoSessionMaxLife != null) {
                    session.setMaxLife(this.ssoSessionMaxLife);
                }
            }
            return session;
        }
    }

    @Override
    public void deleteById(String id) {
        this.sessionStorage.remove(id);
    }

    @Override
    public void destroy() throws Exception {
        this.sessionStorage.destroy();
    }

    protected class RedisSession implements Session {
        public static final int DEFAULT_MAX_INACTIVE_INTERVAL_SECONDS = 1800;
        public static final int DEFAULT_MAX_LIFE_SECONDS = 604800;
        public static final String CREATION_TIME = "creationTime";
        private String id;
        private Map<String, Object> sessionAttrs;
        private Duration maxInactiveInterval;
        private Instant createTime;
        private Instant lastAccessedTime;
        private Duration maxLife;

        public RedisSession() {
            this(UUID.randomUUID().toString());
        }

        public RedisSession(String sessionId) {
            this(sessionId, null);
            SessionRepositoryImpl.this.sessionStorage.save(this.getId(), this.getSessionAttrs());
            SessionRepositoryImpl.this.sessionStorage.ttl(this.getId(), (int)this.maxInactiveInterval.getSeconds());
        }

        public RedisSession(String sessionId, Map<String, Object> attrs) {
            this.sessionAttrs = new HashMap<>();
            this.maxInactiveInterval = Duration.ofSeconds(1800L);
            this.createTime = Clock.systemDefaultZone().instant();
            this.lastAccessedTime = this.createTime;
            this.maxLife = Duration.ofSeconds(604800L);
            this.id = sessionId;
            if (attrs == null) {
                this.sessionAttrs.put("creationTime", this.createTime);
            } else {
                this.sessionAttrs = attrs;
            }
            if (this.sessionAttrs != null && this.sessionAttrs.get("creationTime") == null) {
                this.sessionAttrs.put("creationTime", this.createTime);
            }
        }

        private Map<String, Object> getSessionAttrs() {
            return this.sessionAttrs;
        }

        public boolean isNew() {
            return false;
        }

        @Override
        public Instant getCreationTime() {
            return (Instant)this.sessionAttrs.get("creationTime");
        }

        @Override
        public void setLastAccessedTime(Instant lastAccessedTime) {
            this.lastAccessedTime = lastAccessedTime;
            if (!this.isExpired()) {
                SessionRepositoryImpl.this.sessionStorage.ttl(this.id, (int)this.maxInactiveInterval.getSeconds());
            }
        }

        @Override
        public Instant getLastAccessedTime() {
            return this.lastAccessedTime;
        }

        @Override
        public void setMaxInactiveInterval(Duration interval) {
            this.maxInactiveInterval = interval;
        }

        @Override
        public Duration getMaxInactiveInterval() {
            return this.maxInactiveInterval;
        }

        public Duration getMaxLife() {
            return this.maxLife;
        }

        public void setMaxLife(Duration maxLife) {
            this.maxLife = maxLife;
        }

        @Override
        public boolean isExpired() {
            Instant now = Instant.now();
            try {
                return now.minus(this.maxLife).compareTo(this.getCreationTime()) > 0;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public String getId() {
            return this.id;
        }

        @Override
        public String changeSessionId() {
            return this.id;
        }

        @Override
        public <T> T getAttribute(String attributeName) {
            return (T) this.sessionAttrs.get(attributeName);
        }

        @Override
        public Set<String> getAttributeNames() {
            return this.sessionAttrs.keySet();
        }

        @Override
        public void setAttribute(String attributeName, Object attributeValue) {
            if (attributeValue == null) {
                this.removeAttribute(attributeName);
            } else {
                this.sessionAttrs.put(attributeName, attributeValue);
                SessionRepositoryImpl.this.sessionStorage.put(this.id, attributeName, attributeValue);
            }
        }

        @Override
        public void removeAttribute(String attributeName) {
            this.sessionAttrs.remove(attributeName);
            SessionRepositoryImpl.this.sessionStorage.remove(this.id, attributeName);
        }
    }
}
