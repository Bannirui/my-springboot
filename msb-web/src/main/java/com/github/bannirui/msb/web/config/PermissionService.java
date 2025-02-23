package com.github.bannirui.msb.web.config;

public class PermissionService {
    public PermissionService() {
    }

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
        return v != null && !StringUtil.isEmpty(value) ? v.startsWith(value) : false;
    }

    public boolean permissionValueEndsWith(String key, String value) {
        String v = this.getStringPermission(key);
        return v != null && !StringUtil.isEmpty(value) ? v.endsWith(value) : false;
    }

    public boolean permissionValueContains(String key, String value) {
        Set<String> v = this.getDataPermission(key);
        return v != null && !StringUtil.isEmpty(value) ? v.contains(value) : false;
    }

    public Integer getIntPermission(String key) {
        return (Integer)this.getPermission(key, Integer.class);
    }

    public String getStringPermission(String key) {
        return (String)this.getPermission(key, String.class);
    }

    public Set<String> getDataPermission(String key) {
        return (Set)this.getPermission(key, Set.class);
    }

    public <T> T getPermission(String key, Class<T> t) {
        User user = (User)SecurityContextHolder.getContext().getAuthentication();
        if (user == null) {
            return null;
        } else if (t.isAssignableFrom(Boolean.class)) {
            return user.getPermissions() == null ? false : user.getPermissions().contains(key);
        } else if (t.isAssignableFrom(Integer.class)) {
            return user.getIntPermissions() == null ? null : user.getIntPermissions().get(key);
        } else if (t.isAssignableFrom(String.class)) {
            return user.getStringPermissions() == null ? null : user.getStringPermissions().get(key);
        } else if (t.isAssignableFrom(Set.class)) {
            return user.getDataPermissions() == null ? null : user.getDataPermissions().get(key);
        } else {
            throw new ErrorCodeException(ExceptionEnum.PARAMETER_EXCEPTION, new Object[]{"权限获取", "未知的类型" + t + ",请选择[Boolean.class,String.class,Integer.class,Set.class]"});
        }
    }
}
