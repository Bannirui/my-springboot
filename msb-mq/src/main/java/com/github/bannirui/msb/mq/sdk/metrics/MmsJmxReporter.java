package com.github.bannirui.msb.mq.sdk.metrics;

import com.github.bannirui.msb.mq.sdk.common.MmsThreadFactory;
import com.github.bannirui.msb.mq.sdk.producer.MmsProducerProxy;
import com.github.bannirui.msb.mq.sdk.producer.ProducerFactory;
import com.github.bannirui.msb.mq.sdk.utils.Utils;
import com.yammer.metrics.core.*;
import com.yammer.metrics.stats.Snapshot;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MmsJmxReporter implements Runnable {
    // 线程池
    private final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1, new MmsThreadFactory("MmsConnectionManager-JmxReporter"));
    volatile boolean running = false;

    public void start(long period, TimeUnit unit) {
        this.running = true;
        this.executor.scheduleWithFixedDelay(this, period, period, unit);
    }

    public void shutdown() {
        this.running = false;
        Utils.gracefullyShutdown(this.executor);
    }

    public void run() {
        if (this.running) {
            ProducerFactory.getProducers().forEach(MmsProducerProxy::statistics);
            // todo
            // ConsumerFactory.getConsumers().forEach(MmsConsumerProxy::statistics);
        }
    }

    public void processMeter(Metered meter, StringBuilder context) throws Exception {
        String unit = Utils.abbrev(meter.rateUnit());
        context.append(String.format("             count = %d\n", meter.count()));
        context.append(String.format("         mean rate = %.2f %s/%s\n", meter.meanRate(), meter.eventType(), unit));
        context.append(String.format("     1-minute rate = %.2f %s/%s\n", meter.oneMinuteRate(), meter.eventType(), unit));
        context.append(String.format("     5-minute rate = %.2f %s/%s\n", meter.fiveMinuteRate(), meter.eventType(), unit));
        context.append(String.format("    15-minute rate = %.2f %s/%s\n", meter.fifteenMinuteRate(), meter.eventType(), unit));
    }

    public void processCounter(Counter counter, StringBuilder context) throws Exception {
        context.append(String.format("    count = %d\n", counter.count()));
    }

    public void processHistogram(Histogram histogram, StringBuilder context) throws Exception {
        Snapshot snapshot = histogram.getSnapshot();
        context.append(String.format("               min = %.2f\n", histogram.min()));
        context.append(String.format("               max = %.2f\n", histogram.max()));
        context.append(String.format("              mean = %.2f\n", histogram.mean()));
        context.append(String.format("            stddev = %.2f\n", histogram.stdDev()));
        context.append(String.format("            median = %.2f\n", snapshot.getMedian()));
        context.append(String.format("              75%% <= %.2f\n", snapshot.get75thPercentile()));
        context.append(String.format("              90%% <= %.2f\n", snapshot.getValue(0.9D)));
        context.append(String.format("              95%% <= %.2f\n", snapshot.get95thPercentile()));
        context.append(String.format("              98%% <= %.2f\n", snapshot.get98thPercentile()));
        context.append(String.format("              99%% <= %.2f\n", snapshot.get99thPercentile()));
    }

    public void processTimer(Timer timer, StringBuilder context) throws Exception {
        this.processMeter(timer, context);
        String durationUnit = Utils.abbrev(timer.durationUnit());
        Snapshot snapshot = timer.getSnapshot();
        context.append(String.format("               min = %.2f%s\n", timer.min(), durationUnit));
        context.append(String.format("               max = %.2f%s\n", timer.max(), durationUnit));
        context.append(String.format("              mean = %.2f%s\n", timer.mean(), durationUnit));
        context.append(String.format("            stddev = %.2f%s\n", timer.stdDev(), durationUnit));
        context.append(String.format("            median = %.2f%s\n", snapshot.getMedian(), durationUnit));
        context.append(String.format("              75%% <= %.2f%s\n", snapshot.get75thPercentile(), durationUnit));
        context.append(String.format("              90%% <= %.2f%s\n", snapshot.getValue(0.9D), durationUnit));
        context.append(String.format("              95%% <= %.2f%s\n", snapshot.get95thPercentile(), durationUnit));
        context.append(String.format("              98%% <= %.2f%s\n", snapshot.get98thPercentile(), durationUnit));
        context.append(String.format("              99%% <= %.2f%s\n", snapshot.get99thPercentile(), durationUnit));
    }

    public void processGauge(Gauge<?> gauge, StringBuilder context) throws Exception {
        context.append(String.format("    value = %s\n", gauge.value()));
    }
}
