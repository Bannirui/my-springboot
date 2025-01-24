package com.github.bannirui.msb.mq.sdk.message.statistic;

public class TimerInfo {
    private String type;
    private double min;
    private double max;
    private double mean;
    private double stddev;
    private double median;
    private double percent75;
    private double percent90;
    private double percent95;
    private double percent98;
    private double percent99;
    private double percent999;

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getMin() {
        return this.min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return this.max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getMean() {
        return this.mean;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }

    public double getStddev() {
        return this.stddev;
    }

    public void setStddev(double stddev) {
        this.stddev = stddev;
    }

    public double getMedian() {
        return this.median;
    }

    public void setMedian(double median) {
        this.median = median;
    }

    public double getPercent75() {
        return this.percent75;
    }

    public void setPercent75(double percent75) {
        this.percent75 = percent75;
    }

    public double getPercent90() {
        return this.percent90;
    }

    public void setPercent90(double percent90) {
        this.percent90 = percent90;
    }

    public double getPercent95() {
        return this.percent95;
    }

    public void setPercent95(double percent95) {
        this.percent95 = percent95;
    }

    public double getPercent98() {
        return this.percent98;
    }

    public void setPercent98(double percent98) {
        this.percent98 = percent98;
    }

    public double getPercent99() {
        return this.percent99;
    }

    public void setPercent99(double percent99) {
        this.percent99 = percent99;
    }

    public double getPercent999() {
        return this.percent999;
    }

    public void setPercent999(double percent999) {
        this.percent999 = percent999;
    }
}
