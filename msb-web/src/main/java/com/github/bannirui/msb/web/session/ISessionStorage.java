package com.github.bannirui.msb.web.session;

import com.github.bannirui.msb.web.filter.User;
import java.util.List;
import java.util.Map;

public interface ISessionStorage {
    void remove(String id);

    Map<String, Object> get(String id);

    void save(String id, Map<String, Object> sessionAttrs);

    void ttl(String id, Integer maxInactiveInterval);

    void put(String id, String key, Object value);

    List<User> getUser(String openId);

    void updateUser(List<User> users);

    void remove(String id, String attributeName);

    void destroy();
}
