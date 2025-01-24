package com.github.bannirui.msb.mq.sdk.message.statistic;

public class ClientInfo {
    private String mmsName;
    private String clientName;
    private String ip;

    public String getMmsName() {
        return this.mmsName;
    }

    public void setMmsName(String mmsName) {
        this.mmsName = mmsName;
    }

    public String getClientName() {
        return this.clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getIp() {
        return this.ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
