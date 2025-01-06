package com.github.bannirui.msb.log.cat;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;
import com.github.bannirui.msb.common.plugin.InterceptorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MsbCat {

    private static final Logger log = LoggerFactory.getLogger(MsbCat.class);

    private static MsbCat instance;

    static {
        try {
            MsbCat.instance = InterceptorUtil.getProxyObj(MsbCat.class, "Msb.Cat");
        } catch (Exception var1) {
            log.error("get msb-cat instance failed");
        }
    }

    public static MsbCat getInstance() {
        return MsbCat.instance;
    }

    public void logEvent(String type, String name) {
        Cat.getProducer().logEvent(type, name);
    }

    public void logEvent(String type, String name, String status, String nameValuePairs) {
        Cat.getProducer().logEvent(type, name, status, nameValuePairs);
    }

    public void logTrace(String type, String name, String status, String nameValuePairs) {
        Cat.getProducer().logTrace(type, name, status, nameValuePairs);
    }

    public Event newEvent(String type, String name) {
        return Cat.getProducer().newEvent(type, name);
    }

    public Transaction newTransaction(String type, String name) {
        return Cat.getProducer().newTransaction(type, name);
    }

    public static void logMetricForDuration(String name, long durationInMillis) {
        logMetricInternal(name, "T", String.valueOf(durationInMillis));
    }

    public static void logMetricForSum(String name, double value) {
        logMetricInternal(name, "S", String.format("%.2f", value));
    }

    public static void logMetricForSum(String name, double sum, int quantity) {
        logMetricInternal(name, "S,C", String.format("%s,%.2f", quantity, sum));
    }

    private static void logMetricInternal(String name, String status, String keyValuePairs) {
        Cat.getProducer().logMetric(name, status, keyValuePairs);
    }
}
