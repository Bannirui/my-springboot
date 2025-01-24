package com.github.bannirui.msb.mq.sdk.message.statistic.kafka;

import java.util.List;

public class KafkaConsumerInfo {
    private String topic;
    private String clientIdName;
    private String consumerGroup;
    private String ip;
    private List<KafkaLagInfo> lags;
    private double incomingByteRate;
    private double incomingByteTotal;
    private double ioRatio;
    private double ioTimeNsAvg;
    private double ioWaitRatio;
    private double ioWaitTimeNsAvg;
    private double ioWaittimeTotal;
    private double iotimeTotal;
    private double networkIoRate;
    private double networkIoTotal;
    private double requestRate;
    private double requestSizeAvg;
    private double requestSizeMax;
    private double requestTotal;
    private double responseRate;
    private double responseTotal;
    private double selectRate;
    private double selectTotal;
    private double connectionCount;
    private double connectionCreationRate;
    private double connectionCreationTotal;

    public String getTopic() {
        return this.topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getClientIdName() {
        return this.clientIdName;
    }

    public void setClientIdName(String clientIdName) {
        this.clientIdName = clientIdName;
    }

    public String getConsumerGroup() {
        return this.consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public String getIp() {
        return this.ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public List<KafkaLagInfo> getLags() {
        return this.lags;
    }

    public void setLags(List<KafkaLagInfo> lags) {
        this.lags = lags;
    }

    public double getIncomingByteRate() {
        return this.incomingByteRate;
    }

    public void setIncomingByteRate(double incomingByteRate) {
        this.incomingByteRate = incomingByteRate;
    }

    public double getIncomingByteTotal() {
        return this.incomingByteTotal;
    }

    public void setIncomingByteTotal(double incomingByteTotal) {
        this.incomingByteTotal = incomingByteTotal;
    }

    public double getIoRatio() {
        return this.ioRatio;
    }

    public void setIoRatio(double ioRatio) {
        this.ioRatio = ioRatio;
    }

    public double getIoTimeNsAvg() {
        return this.ioTimeNsAvg;
    }

    public void setIoTimeNsAvg(double ioTimeNsAvg) {
        this.ioTimeNsAvg = ioTimeNsAvg;
    }

    public double getIoWaitRatio() {
        return this.ioWaitRatio;
    }

    public void setIoWaitRatio(double ioWaitRatio) {
        this.ioWaitRatio = ioWaitRatio;
    }

    public double getIoWaitTimeNsAvg() {
        return this.ioWaitTimeNsAvg;
    }

    public void setIoWaitTimeNsAvg(double ioWaitTimeNsAvg) {
        this.ioWaitTimeNsAvg = ioWaitTimeNsAvg;
    }

    public double getIoWaittimeTotal() {
        return this.ioWaittimeTotal;
    }

    public void setIoWaittimeTotal(double ioWaittimeTotal) {
        this.ioWaittimeTotal = ioWaittimeTotal;
    }

    public double getIotimeTotal() {
        return this.iotimeTotal;
    }

    public void setIotimeTotal(double iotimeTotal) {
        this.iotimeTotal = iotimeTotal;
    }

    public double getNetworkIoRate() {
        return this.networkIoRate;
    }

    public void setNetworkIoRate(double networkIoRate) {
        this.networkIoRate = networkIoRate;
    }

    public double getNetworkIoTotal() {
        return this.networkIoTotal;
    }

    public void setNetworkIoTotal(double networkIoTotal) {
        this.networkIoTotal = networkIoTotal;
    }

    public double getRequestRate() {
        return this.requestRate;
    }

    public void setRequestRate(double requestRate) {
        this.requestRate = requestRate;
    }

    public double getRequestSizeAvg() {
        return this.requestSizeAvg;
    }

    public void setRequestSizeAvg(double requestSizeAvg) {
        this.requestSizeAvg = requestSizeAvg;
    }

    public double getRequestSizeMax() {
        return this.requestSizeMax;
    }

    public void setRequestSizeMax(double requestSizeMax) {
        this.requestSizeMax = requestSizeMax;
    }

    public double getRequestTotal() {
        return this.requestTotal;
    }

    public void setRequestTotal(double requestTotal) {
        this.requestTotal = requestTotal;
    }

    public double getResponseRate() {
        return this.responseRate;
    }

    public void setResponseRate(double responseRate) {
        this.responseRate = responseRate;
    }

    public double getResponseTotal() {
        return this.responseTotal;
    }

    public void setResponseTotal(double responseTotal) {
        this.responseTotal = responseTotal;
    }

    public double getSelectRate() {
        return this.selectRate;
    }

    public void setSelectRate(double selectRate) {
        this.selectRate = selectRate;
    }

    public double getSelectTotal() {
        return this.selectTotal;
    }

    public void setSelectTotal(double selectTotal) {
        this.selectTotal = selectTotal;
    }

    public double getConnectionCount() {
        return this.connectionCount;
    }

    public void setConnectionCount(double connectionCount) {
        this.connectionCount = connectionCount;
    }

    public double getConnectionCreationRate() {
        return this.connectionCreationRate;
    }

    public void setConnectionCreationRate(double connectionCreationRate) {
        this.connectionCreationRate = connectionCreationRate;
    }

    public double getConnectionCreationTotal() {
        return this.connectionCreationTotal;
    }

    public void setConnectionCreationTotal(double connectionCreationTotal) {
        this.connectionCreationTotal = connectionCreationTotal;
    }
}
