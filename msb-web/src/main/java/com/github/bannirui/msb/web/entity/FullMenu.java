package com.github.bannirui.msb.web.entity;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.util.List;

public class FullMenu implements Serializable {
    private static final long serialVersionUID = -3516317789336372120L;
    private Boolean display;
    private Long rank;
    @JSONField(
        name = "module_id"
    )
    private Long moduleId;
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
    private List<FullMenu> children;

    public Boolean getDisplay() {
        return this.display;
    }

    public void setDisplay(Boolean display) {
        this.display = display;
    }

    public Long getRank() {
        return this.rank;
    }

    public void setRank(Long rank) {
        this.rank = rank;
    }

    public Long getModuleId() {
        return this.moduleId;
    }

    public void setModuleId(Long moduleId) {
        this.moduleId = moduleId;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getParentId() {
        return this.parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getMenuIcon() {
        return this.menuIcon;
    }

    public void setMenuIcon(String menuIcon) {
        this.menuIcon = menuIcon;
    }

    public String getMenuName() {
        return this.menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public String getMenuUrl() {
        return this.menuUrl;
    }

    public void setMenuUrl(String menuUrl) {
        this.menuUrl = menuUrl;
    }

    public String getAccessPermissions() {
        return this.accessPermissions;
    }

    public void setAccessPermissions(String accessPermissions) {
        this.accessPermissions = accessPermissions;
    }

    public String[] getAccessPermissionArr() {
        return this.accessPermissionArr;
    }

    public void setAccessPermissionArr(String[] accessPermissionArr) {
        this.accessPermissionArr = accessPermissionArr;
    }

    public List<FullMenu> getChildren() {
        return this.children;
    }

    public void setChildren(List<FullMenu> children) {
        this.children = children;
    }
}
