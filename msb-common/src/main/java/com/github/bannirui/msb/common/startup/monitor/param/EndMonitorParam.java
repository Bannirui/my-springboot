package com.github.bannirui.msb.common.startup.monitor.param;

public class EndMonitorParam {
    private Integer id;
    private long endTime;

    public EndMonitorParam() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public long getEndTime() {
        return this.endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}