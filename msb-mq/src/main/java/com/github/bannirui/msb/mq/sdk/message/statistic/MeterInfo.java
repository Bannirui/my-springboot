package com.github.bannirui.msb.mq.sdk.message.statistic;

public class MeterInfo {
    private long count;
    private double mean;
    private double min1Rate;
    private double min5Rate;
    private double min15Rate;
    private String type;

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getCount() {
        return this.count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public double getMean() {
        return this.mean;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }

    public double getMin1Rate() {
        return this.min1Rate;
    }

    public void setMin1Rate(double min1Rate) {
        this.min1Rate = min1Rate;
    }

    public double getMin5Rate() {
        return this.min5Rate;
    }

    public void setMin5Rate(double min5Rate) {
        this.min5Rate = min5Rate;
    }

    public double getMin15Rate() {
        return this.min15Rate;
    }

    public void setMin15Rate(double min15Rate) {
        this.min15Rate = min15Rate;
    }
}
