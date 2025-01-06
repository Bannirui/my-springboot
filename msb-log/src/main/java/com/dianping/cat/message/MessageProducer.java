package com.dianping.cat.message;

public interface MessageProducer {

    String createMessageId();

    boolean isEnabled();

    void logError(Throwable cause);

    void logError(String message, Throwable cause);

    void logEvent(String type, String name);

    void logTrace(String type, String name);

    void logEvent(String type, String name, String status, String nameValuePairs);

    void logTrace(String type, String name, String status, String nameValuePairs);

    void logHeartbeat(String type, String name, String status, String nameValuePairs);

    void logMetric(String name, String status, String nameValuePairs);

    Event newEvent(String type, String name);

    Trace newTrace(String type, String name);

    Heartbeat newHeartbeat(String type, String name);

    Metric newMetric(String type, String name);

    Transaction newTransaction(String type, String name);

    ForkedTransaction newForkedTransaction(String type, String name);

    TaggedTransaction newTaggedTransaction(String type, String name, String tag);
}
