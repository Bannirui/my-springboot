package com.github.bannirui.msb.orm.property;

public class FusingConfig {
    public static final String RULES_GROUP_ID = "SHARDING_DEGRADE_RULE_GROUP_ID";
    public static final String EXCEPTIONBLACKS = "QueryTimeoutException, SQLTimeoutException, MySQLTimeoutException";
    private int grade = 2;
    private int timeWindow = 2;
    private Double count = 20.0D;
    private int rtSlowRequestAmount = 5;
    private int minRequestAmount = 5;
    private boolean enable = true;

    public FusingConfig() {
    }

    public int getGrade() {
        return this.grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public int getTimeWindow() {
        return this.timeWindow;
    }

    public void setTimeWindow(int timeWindow) {
        this.timeWindow = timeWindow;
    }

    public Double getCount() {
        return this.count;
    }

    public void setCount(Double count) {
        this.count = count;
    }

    public int getRtSlowRequestAmount() {
        return this.rtSlowRequestAmount;
    }

    public void setRtSlowRequestAmount(int rtSlowRequestAmount) {
        this.rtSlowRequestAmount = rtSlowRequestAmount;
    }

    public int getMinRequestAmount() {
        return this.minRequestAmount;
    }

    public void setMinRequestAmount(int minRequestAmount) {
        this.minRequestAmount = minRequestAmount;
    }

    public boolean isEnable() {
        return this.enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String toString() {
        return "FusingConfig{grade=" + this.grade + ", timeWindow=" + this.timeWindow + ", count=" + this.count + ", rtSlowRequestAmount=" + this.rtSlowRequestAmount + ", minRequestAmount=" + this.minRequestAmount + '}';
    }
}
