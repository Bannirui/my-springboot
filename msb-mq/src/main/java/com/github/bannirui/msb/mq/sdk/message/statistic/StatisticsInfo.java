package com.github.bannirui.msb.mq.sdk.message.statistic;

public abstract class StatisticsInfo {
    private ClientInfo clientInfo;
    private Long timestamp = System.currentTimeMillis();

    public ClientInfo getClientInfo() {
        return this.clientInfo;
    }

    public void setClientInfo(ClientInfo clientInfo) {
        this.clientInfo = clientInfo;
    }

    public Long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
