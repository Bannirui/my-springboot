package com.github.bannirui.msb.mq.sdk.message.statistic;

public class DistributionInfo {
    private long lessThan1Ms;
    private long lessThan5Ms;
    private long lessThan10Ms;
    private long lessThan50Ms;
    private long lessThan100Ms;
    private long lessThan500Ms;
    private long lessThan1000Ms;
    private long moreThan1000Ms;
    private String type;

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getLessThan1Ms() {
        return this.lessThan1Ms;
    }

    public void setLessThan1Ms(long lessThan1Ms) {
        this.lessThan1Ms = lessThan1Ms;
    }

    public long getLessThan5Ms() {
        return this.lessThan5Ms;
    }

    public void setLessThan5Ms(long lessThan5Ms) {
        this.lessThan5Ms = lessThan5Ms;
    }

    public long getLessThan10Ms() {
        return this.lessThan10Ms;
    }

    public void setLessThan10Ms(long lessThan10Ms) {
        this.lessThan10Ms = lessThan10Ms;
    }

    public long getLessThan50Ms() {
        return this.lessThan50Ms;
    }

    public void setLessThan50Ms(long lessThan50Ms) {
        this.lessThan50Ms = lessThan50Ms;
    }

    public long getLessThan100Ms() {
        return this.lessThan100Ms;
    }

    public void setLessThan100Ms(long lessThan100Ms) {
        this.lessThan100Ms = lessThan100Ms;
    }

    public long getLessThan500Ms() {
        return this.lessThan500Ms;
    }

    public void setLessThan500Ms(long lessThan500Ms) {
        this.lessThan500Ms = lessThan500Ms;
    }

    public long getLessThan1000Ms() {
        return this.lessThan1000Ms;
    }

    public void setLessThan1000Ms(long lessThan1000Ms) {
        this.lessThan1000Ms = lessThan1000Ms;
    }

    public long getMoreThan1000Ms() {
        return this.moreThan1000Ms;
    }

    public void setMoreThan1000Ms(long moreThan1000Ms) {
        this.moreThan1000Ms = moreThan1000Ms;
    }
}
