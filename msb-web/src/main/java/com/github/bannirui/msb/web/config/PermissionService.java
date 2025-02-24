package com.github.bannirui.msb.web.config;

import com.github.bannirui.msb.enums.ExceptionEnum;
import com.github.bannirui.msb.ex.ErrorCodeException;
import com.github.bannirui.msb.web.filter.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;

public class PermissionService {

    public boolean hasPermission(String key) {
        return (Boolean)this.getPermission(key, Boolean.class);
    }

    public boolean permissionValueEqualTo(String key, Integer value) {
        Integer v = this.getIntPermission(key);
        return v == null ? false : v.equals(value);
    }

    public boolean permissionValueEqualTo(String key, String value) {
        String v = this.getStringPermission(key);
        return v == null ? false : v.equals(value);
    }

    public boolean permissionValueGreaterThan(String key, Integer value) {
        Integer v = this.getIntPermission(key);
        if (v == null) {
            return false;
        } else {
            return v.compareTo(value) > 0;
        }
    }

    public boolean permissionValueGreaterThanOrEqualTo(String key, Integer value) {
        Integer v = this.getIntPermission(key);
        if (v == null) {
            return false;
        } else {
            return v.compareTo(value) >= 0;
        }
    }

    public boolean permissionValueLessThan(String key, Integer value) {
        Integer v = this.getIntPermission(key);
        if (v == null) {
            return false;
        } else {
            return v.compareTo(value) < 0;
        }
    }

    public boolean permissionValueLessThanOrEqualTo(String key, Integer value) {
        Integer v = this.getIntPermission(key);
        if (v == null) {
            return false;
        } else {
            return v.compareTo(value) <= 0;
        }
    }

    public boolean permissionValueStartsWith(String key, String value) {
        String v = this.getStringPermission(key);
        return v != null && !StringUtils.isEmpty(value) ? v.startsWith(value) : false;
    }

    public boolean permissionValueEndsWith(String key, String value) {
        String v = this.getStringPermission(key);
        return v != null && !StringUtils.isEmpty(value) ? v.endsWith(value) : false;
    }

    public boolean permissionValueContains(String key, String value) {
        Set<String> v = this.getDataPermission(key);
        return v != null && !StringUtils.isEmpty(value) ? v.contains(value) : false;
    }

    public Integer getIntPermission(String key) {
        return this.getPermission(key, Integer.class);
    }

    public String getStringPermission(String key) {
        return this.getPermission(key, String.class);
    }

    public Set<String> getDataPermission(String key) {
        return this.getPermission(key, Set.class);
    }

    public <T> T getPermission(String key, Class<T> t) {
        User user = (User)SecurityContextHolder.getContext().getAuthentication();
        if (user == null) {
            return null;
        } else if (t.isAssignableFrom(Boolean.class)) {
            return user.getPermissions() == null ? (T)Boolean.FALSE : (T)(Boolean)user.getPermissions().contains(key);
        } else if (t.isAssignableFrom(Integer.class)) {
            return user.getIntPermissions() == null ? null : (T) user.getIntPermissions().get(key);
        } else if (t.isAssignableFrom(String.class)) {
            return user.getStringPermissions() == null ? null : (T) user.getStringPermissions().get(key);
        } else if (t.isAssignableFrom(Set.class)) {
            return user.getDataPermissions() == null ? null : (T) user.getDataPermissions().get(key);
        } else {
            throw new ErrorCodeException(ExceptionEnum.PARAM_EXCEPTION, "权限获取", "未知的类型" + t + ",请选择[Boolean.class,String.class,Integer.class,Set.class]");
        }
    }
}
