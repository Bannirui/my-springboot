package com.github.bannirui.msb.mq.sdk.metrics;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.github.bannirui.msb.mq.sdk.message.statistic.ConsumerStatisticsInfo;
import com.github.bannirui.msb.mq.sdk.message.statistic.StatisticsInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MmsConsumerMetrics extends MmsMetrics {
    private static final Logger logger = LoggerFactory.getLogger(MmsConsumerMetrics.class);
    private final Meter consumeSuccessRate;
    private final Meter consumeFailureRate;
    private final Timer userCostTimeMs;
    private static final String CONSUMER_METRIC_GROUP = "MmsConsumerMetrics";

    public MmsConsumerMetrics(String clientName, String mmsName) {
        super(clientName, mmsName);
        this.consumeSuccessRate = MmsMetricsRegistry.REGISTRY.meter(
            MmsMetricsRegistry.buildName("MmsConsumerMetrics", "messageSuccessRate", clientName, mmsName));
        this.consumeFailureRate = MmsMetricsRegistry.REGISTRY.meter(
            MmsMetricsRegistry.buildName("MmsConsumerMetrics", "consumeFailureRate", clientName, mmsName));
        this.userCostTimeMs = MmsMetricsRegistry.REGISTRY.timer(
            MmsMetricsRegistry.buildName("MmsConsumerMetrics", "userCostTimeMs", clientName, mmsName));
    }

    public Meter consumeSuccessRate() {
        return this.consumeSuccessRate;
    }

    public Meter consumeFailureRate() {
        return this.consumeFailureRate;
    }

    public Timer userCostTimeMs() {
        return this.userCostTimeMs;
    }

    public StatisticsInfo reportMessageStatistics() {
        ConsumerStatisticsInfo info = new ConsumerStatisticsInfo();
        info.setClientInfo(this.getClientInfo());
        info.getMeters().add(this.transfer(this.consumeSuccessRate, "consumeSuccessRate"));
        info.getMeters().add(this.transfer(this.consumeFailureRate, "consumeFailureRate"));
        info.getTimers().add(this.transfer(this.userCostTimeMs, "userCostTimeMs"));
        return info;
    }

    public String reportLogStatistics() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.getClientName() + "--" + this.getMmsName()).append(":\n");
        stringBuilder.append("SuccessMessagePerSec     ");
        try {
            this.processMeter(this.consumeSuccessRate, stringBuilder);
            stringBuilder.append("FailureMessagePerSec");
            this.processMeter(this.consumeFailureRate, stringBuilder);
            stringBuilder.append("userCostTimeMs");
            this.processTimer(this.userCostTimeMs, stringBuilder);
        } catch (Exception e) {
            logger.error("output statistics error", e);
        }
        return stringBuilder.toString();
    }
}
