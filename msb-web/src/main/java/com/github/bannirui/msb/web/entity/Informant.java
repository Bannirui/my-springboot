package com.github.bannirui.msb.web.entity;

public class Informant implements Serializable {
    private static final long serialVersionUID = -3217506222284354585L;
    private String name;
    private String department;
    @JSONField(
        name = "user_id"
    )
    private String userId;
    private String mobile;

    public Informant() {
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return this.department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMobile() {
        return this.mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
}
