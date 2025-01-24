package com.github.bannirui.msb.mq.sdk.message.statistic;

public class PingInfo {
    private long startTime = System.currentTimeMillis();
    private String source;
    private String target;
    private int succeedSend;
    private int timeoutSend;
    private int failSendOut;
    private Double dataLost;
    private Double min;
    private Double max;
    private Double avg;
    private Double stdev;

    public int getSucceedSend() {
        return this.succeedSend;
    }

    public void setSucceedSend(int succeedSend) {
        this.succeedSend = succeedSend;
    }

    public int getTimeoutSend() {
        return this.timeoutSend;
    }

    public void setTimeoutSend(int timeoutSend) {
        this.timeoutSend = timeoutSend;
    }

    public int getFailSendOut() {
        return this.failSendOut;
    }

    public void setFailSendOut(int failSendOut) {
        this.failSendOut = failSendOut;
    }

    public Double getDataLost() {
        return this.dataLost;
    }

    public void setDataLost(Double dataLost) {
        this.dataLost = dataLost;
    }

    public Double getMin() {
        return this.min;
    }

    public void setMin(Double min) {
        this.min = min;
    }

    public Double getMax() {
        return this.max;
    }

    public void setMax(Double max) {
        this.max = max;
    }

    public Double getAvg() {
        return this.avg;
    }

    public void setAvg(Double avg) {
        this.avg = avg;
    }

    public Double getStdev() {
        return this.stdev;
    }

    public void setStdev(Double stdev) {
        this.stdev = stdev;
    }

    public void succeedCount() {
        ++this.succeedSend;
    }

    public void timeoutCount() {
        ++this.timeoutSend;
    }

    public void failSendCount() {
        ++this.failSendOut;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getSource() {
        return this.source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return this.target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}
