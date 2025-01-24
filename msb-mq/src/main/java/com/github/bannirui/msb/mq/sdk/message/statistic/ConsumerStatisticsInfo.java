package com.github.bannirui.msb.mq.sdk.message.statistic;

import java.util.ArrayList;
import java.util.List;

public class ConsumerStatisticsInfo extends StatisticsInfo {
    private List<MeterInfo> meters = new ArrayList<>();
    private List<TimerInfo> timers = new ArrayList<>();

    public List<MeterInfo> getMeters() {
        return this.meters;
    }

    public void setMeters(List<MeterInfo> meters) {
        this.meters = meters;
    }

    public List<TimerInfo> getTimers() {
        return this.timers;
    }

    public void setTimers(List<TimerInfo> timers) {
        this.timers = timers;
    }
}
