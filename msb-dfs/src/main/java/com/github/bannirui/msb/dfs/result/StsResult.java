package com.github.bannirui.msb.dfs.result;

import com.alibaba.fastjson.annotation.JSONField;

public class StsResult {
    @JSONField(
        name = "security_token"
    )
    private String securityToken;
    private Boolean status;
    private String message;
    private String statusCode;

    public String getSecurityToken() {
        return this.securityToken;
    }

    public void setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
    }

    public Boolean getStatus() {
        return this.status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatusCode() {
        return this.statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public String toString() {
        return "StsResult{securityToken='" + this.securityToken + '\'' + ", status=" + this.status + ", message='" + this.message + '\'' + ", statusCode='" + this.statusCode + '\'' + '}';
    }
}
