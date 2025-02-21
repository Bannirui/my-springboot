package com.github.bannirui.msb.http.config;

public class HttpConfigProperties {
    private String basePackage;
    private Integer maxConnectionSize = 200;
    private Integer maxPerRouteSize = 200;
    private Long maxIdleSecond = 10L;

    public Integer getMaxConnectionSize() {
        return this.maxConnectionSize;
    }

    public void setMaxConnectionSize(Integer maxConnectionSize) {
        this.maxConnectionSize = maxConnectionSize;
    }

    public Integer getMaxPerRouteSize() {
        return this.maxPerRouteSize;
    }

    public void setMaxPerRouteSize(Integer maxPerRouteSize) {
        this.maxPerRouteSize = maxPerRouteSize;
    }

    public Long getMaxIdleSecond() {
        return this.maxIdleSecond;
    }

    public void setMaxIdleSecond(Long maxIdleSecond) {
        this.maxIdleSecond = maxIdleSecond;
    }

    public String getBasePackage() {
        return this.basePackage;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }
}
