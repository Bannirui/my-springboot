package com.github.bannirui.msb.web.filter;

import com.alibaba.fastjson.JSONArray;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public class User implements Authentication {
    private static final long serialVersionUID = 1L;
    private Set<String> permissions;
    private Map<String, Integer> intPermissions;
    private Map<String, String> stringPermissions;
    private Map<String, Set<String>> dataPermissions;
    private Map<String, Map<String, Set<String>>> dataPermissionsWithScopeCtrlType;
    private Map<String, Object> properties;
    private boolean authenticated = false;
    private List<Menu> menus;
    private String accessToken;

    public User(Map<String, Object> userInfo, List<Menu> menus) {
        this.properties = userInfo;
        this.menus = menus;
        if (userInfo.get("permissions") != null) {
            Map<String, Object> permissionMap = (Map)userInfo.get("permissions");
            for (Map.Entry<String, Object> entry : permissionMap.entrySet()) {
                Map value = (Map)entry.getValue();
                if(Objects.isNull(value)) continue;
                Object val = value.get("value");
                if(Objects.isNull(val)) continue;
                if(val instanceof Boolean && !(Boolean)val) continue;
                if (val instanceof Boolean && (Boolean)val) {
                    this.addPermissions(entry.getKey());
                } else if (val instanceof Integer) {
                    this.addIntPermissions(entry.getKey(), (Integer)val);
                } else if (val instanceof String) {
                    this.addStringPermissions(entry.getKey(), (String)val);
                }
                HashMap<String, Set<String>> dataPermission = this.getNodeScopes(value);
                this.addDataPermissions(entry.getKey(), dataPermission);
            }
        }
        this.authenticated = true;
        this.dataPermissions = this.flatMapDataPermissionsWithScopeCtrlType();
    }

    public void updatePermissions(Map<String, Object> permissionMap) {
        Set<String> permissions = new HashSet<>();
        Map<String, Integer> intPermissions = new HashMap<>();
        Map<String, String> stringPermissions = new HashMap<>();
        Map<String, Map<String, Set<String>>> dataPermissionsWithScopeCtrlType = new HashMap<>();
        for (Map.Entry<String, Object> entry : permissionMap.entrySet()) {
            this.permissions = permissions;
            this.intPermissions = intPermissions;
            this.stringPermissions = stringPermissions;
            this.dataPermissions = this.flatMapDataPermissionsWithScopeCtrlType();
            Map value = (Map)entry.getValue();
            if(Objects.isNull(value)) continue;
            Object val = value.get("value");
            if(Objects.isNull(val)) continue;
            if(val instanceof Boolean && !(Boolean)val) continue;
            if (val instanceof Boolean && (Boolean)val) {
                permissions.add(entry.getKey());
            } else if (val instanceof Integer) {
                intPermissions.put(entry.getKey(), (Integer)val);
            } else if (val instanceof String) {
                stringPermissions.put(entry.getKey(), (String)val);
            }
            HashMap<String, Set<String>> dataPermission = this.getNodeScopes(value);
            dataPermissionsWithScopeCtrlType.put(entry.getKey(), dataPermission);
        }
    }

    private void addStringPermissions(String key, String v) {
        if (this.stringPermissions == null) {
            this.stringPermissions = new HashMap<>();
        }
        this.stringPermissions.put(key, v);
    }

    private void addIntPermissions(String key, Integer v) {
        if (this.intPermissions == null) {
            this.intPermissions = new HashMap<>();
        }
        this.intPermissions.put(key, v);
    }

    private void addDataPermissions(String key, HashMap<String, Set<String>> dataPermission) {
        if (dataPermission != null) {
            if (this.dataPermissionsWithScopeCtrlType == null) {
                this.dataPermissionsWithScopeCtrlType = new HashMap<>();
            }
            this.dataPermissionsWithScopeCtrlType.put(key, dataPermission);
        }

    }

    private HashMap<String, Set<String>> getNodeScopes(Map<String, Object> value) {
        Object type = value.get("scope_ctrl_type");
        if (type == null) {
            return null;
        }
        JSONArray jsonArray = (JSONArray)value.get(type + "_scopes");
        if (jsonArray != null && jsonArray.size() > 0) {
            Set<String> collect = jsonArray.stream().map(Object::toString).collect(Collectors.toSet());
            if (collect.size() > 0) {
                HashMap<String, Set<String>> ctrlTypeScopes = new HashMap<>();
                ctrlTypeScopes.put(type.toString(), collect);
                return ctrlTypeScopes;
            }
        }
        return null;
    }

    private Map<String, Set<String>> flatMapDataPermissionsWithScopeCtrlType() {
        if (this.dataPermissionsWithScopeCtrlType == null) {
            return null;
        }
        HashMap<String, Set<String>> dps = new HashMap<>();
        Set<String> permissions = this.dataPermissionsWithScopeCtrlType.keySet();
        for (String permission : permissions) {
            Set<String> collect = this.dataPermissionsWithScopeCtrlType.get(permission).values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
            dps.put(permission, collect);
        }
        return dps;
    }

    private void addPermissions(String key) {
        if (this.permissions == null) {
            this.permissions = new HashSet<>();
        }
        this.permissions.add(key);
    }

    public String getPermissionsType(String key) {
        if (this.properties.get("permissions") != null) {
            Map<String, Object> permissionMap = (Map)this.properties.get("permissions");
            if (permissionMap.get(key) != null) {
                return (String)((Map)permissionMap.get(key)).get("scope_ctrl_type");
            }
        }
        return null;
    }

    public Set<String> getPermissions() {
        return this.permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    public Map<String, Integer> getIntPermissions() {
        return this.intPermissions;
    }

    public void setIntPermissions(Map<String, Integer> intPermissions) {
        this.intPermissions = intPermissions;
    }

    public Map<String, String> getStringPermissions() {
        return this.stringPermissions;
    }

    public void setStringPermissions(Map<String, String> stringPermissions) {
        this.stringPermissions = stringPermissions;
    }

    public Map<String, Set<String>> getDataPermissions() {
        return this.dataPermissions;
    }

    public void setDataPermissions(Map<String, Set<String>> dataPermissions) {
        this.dataPermissions = dataPermissions;
    }

    public Map<String, Map<String, Set<String>>> dataPermissionsWithScopeCtrlType() {
        return this.dataPermissionsWithScopeCtrlType;
    }

    public String getAvatar() {
        return (String)this.getProperties("avatar");
    }

    public Integer getCertLevel() {
        return (Integer)this.getProperties("cert_level");
    }

    public String getDept_code() {
        return (String)this.getProperties("dept_code");
    }

    public String getDeptName() {
        return (String)this.getProperties("dept_name");
    }

    public String getFullname() {
        return (String)this.getProperties("fullname");
    }

    public String getMoodMessage() {
        return (String)this.getProperties("mood_message");
    }

    public String getNickname() {
        return (String)this.getProperties("nickname");
    }

    public String getNodeCode() {
        return (String)this.getProperties("node_code");
    }

    public String getNodeName() {
        return (String)this.getProperties("node_name");
    }

    public String getOpenid() {
        return (String)this.getProperties("openid");
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return null;
    }

    @Override
    public boolean isAuthenticated() {
        return this.authenticated;
    }

    @Override
    public void setAuthenticated(boolean authenticated) throws IllegalArgumentException {
        this.authenticated = authenticated;
    }

    @Override
    public String getName() {
        return (String)this.getProperties("nickname");
    }

    public Object getProperties(String key) {
        return this.properties == null ? null : this.properties.get(key);
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public List<Menu> getMenus() {
        return this.menus;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
