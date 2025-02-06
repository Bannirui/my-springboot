package com.github.bannirui.msb.mq.sdk.metrics;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.github.bannirui.msb.mq.sdk.message.statistic.ProducerStatisticsInfo;
import com.github.bannirui.msb.mq.sdk.message.statistic.StatisticsInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class MmsProducerMetrics extends MmsMetrics {
    private static final Logger logger = LoggerFactory.getLogger(MmsProducerMetrics.class);
    private final Meter messageSuccessRate;
    private final Meter messageFailureRate;
    private final Timer sendCostRate;
    private Distribution msgBody;
    private Map<String, Timer> timeDelays;
    private static final String PRODUCDER_METRIC_GROUP = "MmsProducerMetrics";
    private Distribution distribution;

    public Distribution getDistribution() {
        return this.distribution;
    }

    public MmsProducerMetrics(String clientName, String mmsName) {
        super(clientName, mmsName);
        this.messageSuccessRate = MmsMetricsRegistry.REGISTRY.meter(
            MmsMetricsRegistry.buildName("MmsProducerMetrics", "messageSuccessRate", clientName, mmsName));
        this.messageFailureRate = MmsMetricsRegistry.REGISTRY.meter(
            MmsMetricsRegistry.buildName("MmsProducerMetrics", "messageFailureRate", clientName, mmsName));
        this.sendCostRate = MmsMetricsRegistry.REGISTRY.timer(MmsMetricsRegistry.buildName("MmsProducerMetrics", "sendCostRate", clientName, mmsName));
        this.msgBody = Distribution.newDistribution(MmsMetricsRegistry.buildName("MmsProducerMetrics", "msgBody", clientName, mmsName));
        this.distribution = Distribution.newDistribution(MmsMetricsRegistry.buildName("MmsProducerMetrics", "distribution", clientName, mmsName));
    }

    public Meter messageSuccessRate() {
        return this.messageSuccessRate;
    }

    public Meter messageFailureRate() {
        return this.messageFailureRate;
    }

    public Timer sendCostRate() {
        return this.sendCostRate;
    }

    public Distribution msgBody() {
        return this.msgBody;
    }

    public StatisticsInfo reportMessageStatistics() {
        ProducerStatisticsInfo info = new ProducerStatisticsInfo();
        Distribution old = this.distribution;
        Distribution oldMsgBody = this.msgBody;
        this.renewDistribution();
        info.setClientInfo(this.getClientInfo());
        info.getDistributions().add(this.transfer(old, "distribution"));
        info.getMeters().add(this.transfer(this.messageSuccessRate, "messageSuccessRate"));
        info.getMeters().add(this.transfer(this.messageFailureRate, "messageFailureRate"));
        info.getTimers().add(this.transfer(this.sendCostRate, "sendCostRate"));
        info.getDistributions().add(this.transfer(oldMsgBody, "msgBody"));
        return info;
    }

    private void renewDistribution() {
        this.distribution = Distribution.newDistribution(this.distribution.getName());
        this.msgBody = Distribution.newDistribution(this.msgBody.getName());
    }

    public String reportLogStatistics() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.getClientName() + "--" + this.getMmsName()).append(":\n");
        try {
            stringBuilder.append("SuccessMessagePerSec     ");
            this.processMeter(this.messageSuccessRate, stringBuilder);
            stringBuilder.append("ProducerSendRateAndTimeMs");
            this.processTimer(this.sendCostRate, stringBuilder);
            stringBuilder.append("FailureMessagePerSec     ");
            this.processMeter(this.messageFailureRate, stringBuilder);
            Distribution old = this.distribution;
            Distribution oldMsgBody = this.msgBody;
            this.renewDistribution();
            stringBuilder.append(old.output());
            stringBuilder.append(oldMsgBody.output());
        } catch (Exception e) {
            logger.error("output statistics error", e);
        }
        return stringBuilder.toString();
    }
}
