package com.github.bannirui.msb.web.entity;

public class UpdateUserResult implements Serializable {
    private Long total;
    private List<Map<String, Object>> users;

    public UpdateUserResult() {
    }

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
