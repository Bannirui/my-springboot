package com.github.bannirui.msb.common.compensate;

import java.io.Serializable;
import java.util.Set;

public class CsaRule implements Serializable {
    private String id;
    private String appId;
    private String resourceName;
    private String name;
    private boolean enabled = false;
    private int retryCount;
    private Set<String> includeExceptions;
    private Set<String> excludeExceptions;
    private Integer delayTime;

    public CsaRule() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public Set<String> getIncludeExceptions() {
        return includeExceptions;
    }

    public void setIncludeExceptions(Set<String> includeExceptions) {
        this.includeExceptions = includeExceptions;
    }

    public Set<String> getExcludeExceptions() {
        return excludeExceptions;
    }

    public void setExcludeExceptions(Set<String> excludeExceptions) {
        this.excludeExceptions = excludeExceptions;
    }

    public Integer getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(Integer delayTime) {
        this.delayTime = delayTime;
    }

    @Override
    public String toString() {
        return "CsaRule{" +
            "id='" + id + '\'' +
            ", appId='" + appId + '\'' +
            ", resourceName='" + resourceName + '\'' +
            ", name='" + name + '\'' +
            ", enabled=" + enabled +
            ", retryCount=" + retryCount +
            ", includeExceptions=" + includeExceptions +
            ", excludeExceptions=" + excludeExceptions +
            ", delayTime=" + delayTime +
            '}';
    }
}
