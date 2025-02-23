package com.github.bannirui.msb.web.filter;

import com.alibaba.fastjson.annotation.JSONField;
import java.io.Serializable;
import java.util.List;

public class Menu implements Serializable {
    private String id;
    private String code;
    @JSONField(
        name = "created_at"
    )
    private String createdAt;
    @JSONField(
        name = "updated_at"
    )
    private String updatedAt;
    @JSONField(
        name = "parent_id"
    )
    private String parentId;
    @JSONField(
        name = "menu_icon"
    )
    private String menuIcon;
    @JSONField(
        name = "menu_name"
    )
    private String menuName;
    @JSONField(
        name = "menu_url"
    )
    private String menuUrl;
    @JSONField(
        name = "access_permissions"
    )
    private String accessPermissions;
    @JSONField(
        name = "access_permission_arr"
    )
    private String[] accessPermissionArr;
    private List<Menu> children;

    public void setId(final String id) {
        this.id = id;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public void setCreatedAt(final String createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(final String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setParentId(final String parentId) {
        this.parentId = parentId;
    }

    public void setMenuIcon(final String menuIcon) {
        this.menuIcon = menuIcon;
    }

    public void setMenuName(final String menuName) {
        this.menuName = menuName;
    }

    public void setMenuUrl(final String menuUrl) {
        this.menuUrl = menuUrl;
    }

    public void setAccessPermissions(final String accessPermissions) {
        this.accessPermissions = accessPermissions;
    }

    public void setAccessPermissionArr(final String[] accessPermissionArr) {
        this.accessPermissionArr = accessPermissionArr;
    }

    public void setChildren(final List<Menu> children) {
        this.children = children;
    }

    public String getId() {
        return this.id;
    }

    public String getCode() {
        return this.code;
    }

    public String getCreatedAt() {
        return this.createdAt;
    }

    public String getUpdatedAt() {
        return this.updatedAt;
    }

    public String getParentId() {
        return this.parentId;
    }

    public String getMenuIcon() {
        return this.menuIcon;
    }

    public String getMenuName() {
        return this.menuName;
    }

    public String getMenuUrl() {
        return this.menuUrl;
    }

    public String getAccessPermissions() {
        return this.accessPermissions;
    }

    public String[] getAccessPermissionArr() {
        return this.accessPermissionArr;
    }

    public List<Menu> getChildren() {
        return this.children;
    }
}
