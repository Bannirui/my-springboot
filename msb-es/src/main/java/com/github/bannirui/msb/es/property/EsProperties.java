package com.github.bannirui.msb.es.property;

public class EsProperties {
    private String clusterNodes;
    private String userName;
    private String userPass;
    private int connectTimeOutSeconds;
    private int socketTimeOutSeconds;
    private int maxConnectTotal;
    private int maxConnectPerRoute;
    private String proxyHost;
    private String connectionKeepAliveStrategyClass;

    public String getConnectionKeepAliveStrategyClass() {
        return this.connectionKeepAliveStrategyClass;
    }

    public void setConnectionKeepAliveStrategyClass(String connectionKeepAliveStrategyClass) {
        this.connectionKeepAliveStrategyClass = connectionKeepAliveStrategyClass;
    }

    public String getProxyHost() {
        return this.proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPass() {
        return this.userPass;
    }

    public void setUserPass(String userPass) {
        this.userPass = userPass;
    }

    public int getConnectTimeOutSeconds() {
        return this.connectTimeOutSeconds;
    }

    public void setConnectTimeOutSeconds(int connectTimeOutSeconds) {
        this.connectTimeOutSeconds = connectTimeOutSeconds;
    }

    public int getSocketTimeOutSeconds() {
        return this.socketTimeOutSeconds;
    }

    public void setSocketTimeOutSeconds(int socketTimeOutSeconds) {
        this.socketTimeOutSeconds = socketTimeOutSeconds;
    }

    public int getMaxConnectTotal() {
        return this.maxConnectTotal;
    }

    public void setMaxConnectTotal(int maxConnectTotal) {
        this.maxConnectTotal = maxConnectTotal;
    }

    public int getMaxConnectPerRoute() {
        return this.maxConnectPerRoute;
    }

    public void setMaxConnectPerRoute(int maxConnectPerRoute) {
        this.maxConnectPerRoute = maxConnectPerRoute;
    }

    public String getClusterNodes() {
        return this.clusterNodes;
    }

    public void setClusterNodes(String clusterNodes) {
        this.clusterNodes = clusterNodes;
    }
}
