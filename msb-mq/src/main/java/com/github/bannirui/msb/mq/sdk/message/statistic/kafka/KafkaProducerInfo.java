package com.github.bannirui.msb.mq.sdk.message.statistic.kafka;

public class KafkaProducerInfo {
    private String ip;
    private String topic;
    private String clientIdName;
    private double sendRate;
    private double retryRate;
    private double errorRate;
    private double sendLatency;
    private double size;
    private double batchSizeAvg;
    private double batchSizeMax;
    private double batchSplitRate;
    private double batchSplitTotal;
    private double bufferAvailableBytes;
    private double bufferTotalBytes;
    private double bufferpoolWaitRatio;
    private double bufferpoolWaitTimeTotal;
    private double produceThrottleTimeAvg;
    private double produceThrottleTimeMax;
    private double ioRatio;
    private double ioTimeNsAvg;
    private double ioWaittimeTotal;
    private double iotimeTotal;
    private double networkIoRate;
    private double networkIoTotal;

    public String getIp() {
        return this.ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

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

    public double getSendRate() {
        return this.sendRate;
    }

    public void setSendRate(double sendRate) {
        this.sendRate = sendRate;
    }

    public double getRetryRate() {
        return this.retryRate;
    }

    public void setRetryRate(double retryRate) {
        this.retryRate = retryRate;
    }

    public double getErrorRate() {
        return this.errorRate;
    }

    public void setErrorRate(double errorRate) {
        this.errorRate = errorRate;
    }

    public double getSendLatency() {
        return this.sendLatency;
    }

    public void setSendLatency(double sendLatency) {
        this.sendLatency = sendLatency;
    }

    public double getSize() {
        return this.size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public double getBatchSizeAvg() {
        return this.batchSizeAvg;
    }

    public void setBatchSizeAvg(double batchSizeAvg) {
        this.batchSizeAvg = batchSizeAvg;
    }

    public double getBatchSizeMax() {
        return this.batchSizeMax;
    }

    public void setBatchSizeMax(double batchSizeMax) {
        this.batchSizeMax = batchSizeMax;
    }

    public double getBatchSplitRate() {
        return this.batchSplitRate;
    }

    public void setBatchSplitRate(double batchSplitRate) {
        this.batchSplitRate = batchSplitRate;
    }

    public double getBatchSplitTotal() {
        return this.batchSplitTotal;
    }

    public void setBatchSplitTotal(double batchSplitTotal) {
        this.batchSplitTotal = batchSplitTotal;
    }

    public double getBufferAvailableBytes() {
        return this.bufferAvailableBytes;
    }

    public void setBufferAvailableBytes(double bufferAvailableBytes) {
        this.bufferAvailableBytes = bufferAvailableBytes;
    }

    public double getBufferTotalBytes() {
        return this.bufferTotalBytes;
    }

    public void setBufferTotalBytes(double bufferTotalBytes) {
        this.bufferTotalBytes = bufferTotalBytes;
    }

    public double getBufferpoolWaitRatio() {
        return this.bufferpoolWaitRatio;
    }

    public void setBufferpoolWaitRatio(double bufferpoolWaitRatio) {
        this.bufferpoolWaitRatio = bufferpoolWaitRatio;
    }

    public double getBufferpoolWaitTimeTotal() {
        return this.bufferpoolWaitTimeTotal;
    }

    public void setBufferpoolWaitTimeTotal(double bufferpoolWaitTimeTotal) {
        this.bufferpoolWaitTimeTotal = bufferpoolWaitTimeTotal;
    }

    public double getProduceThrottleTimeAvg() {
        return this.produceThrottleTimeAvg;
    }

    public void setProduceThrottleTimeAvg(double produceThrottleTimeAvg) {
        this.produceThrottleTimeAvg = produceThrottleTimeAvg;
    }

    public double getProduceThrottleTimeMax() {
        return this.produceThrottleTimeMax;
    }

    public void setProduceThrottleTimeMax(double produceThrottleTimeMax) {
        this.produceThrottleTimeMax = produceThrottleTimeMax;
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
}
