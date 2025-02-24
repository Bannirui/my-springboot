package com.github.bannirui.msb.web.entity;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class UpdateUserResult implements Serializable {
    private Long total;
    private List<Map<String, Object>> users;

    public Long getTotal() {
        return this.total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public List<Map<String, Object>> getUsers() {
        return this.users;
    }

    public void setUsers(List<Map<String, Object>> users) {
        this.users = users;
    }
}
