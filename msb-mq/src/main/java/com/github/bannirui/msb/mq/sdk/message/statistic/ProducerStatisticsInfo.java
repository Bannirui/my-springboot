package com.github.bannirui.msb.mq.sdk.message.statistic;

import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProducerStatisticsInfo extends StatisticsInfo {
    List<MeterInfo> meters = new ArrayList<>();
    List<TimerInfo> timers = new ArrayList<>();
    List<DistributionInfo> distributions = new ArrayList();
    Map<String, TimerInfo> timeDelays = Maps.newHashMap();

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

    public List<DistributionInfo> getDistributions() {
        return this.distributions;
    }

    public void setDistributions(List<DistributionInfo> distributions) {
        this.distributions = distributions;
    }
}
