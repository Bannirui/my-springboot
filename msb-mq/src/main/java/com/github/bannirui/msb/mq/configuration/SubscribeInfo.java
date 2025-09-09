package com.github.bannirui.msb.mq.configuration;

import java.util.HashSet;
import java.util.Set;

/**
 * mq消费者配置.
 */
public class SubscribeInfo {
    private Set<String> tags = new HashSet<>();
    private String consumeThreadMin;
    private String consumeThreadMax;
    private String orderlyConsumePartitionParallelism;
    private String maxBatchRecords;
    private String consumeBatchSize;
    private boolean easy;
    private String isOrderly;
    private String consumeTimeoutMs;
    private String maxReconsumeTimes;
    private String isNewPush;
    private String orderlyConsumeThreadSize;

    public String getConsumeBatchSize() {
        return this.consumeBatchSize;
    }

    public void setConsumeBatchSize(String consumeBatchSize) {
        this.consumeBatchSize = consumeBatchSize;
    }

    public String getConsumeTimeoutMs() {
        return this.consumeTimeoutMs;
    }

    public void setConsumeTimeoutMs(String consumeTimeoutMs) {
        this.consumeTimeoutMs = consumeTimeoutMs;
    }

    public String getMaxReconsumeTimes() {
        return this.maxReconsumeTimes;
    }

    public void setMaxReconsumeTimes(String maxReconsumeTimes) {
        this.maxReconsumeTimes = maxReconsumeTimes;
    }

    public Set<String> getTags() {
        return this.tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public String getConsumeThreadMin() {
        return this.consumeThreadMin;
    }

    public void setConsumeThreadMin(String consumeThreadMin) {
        this.consumeThreadMin = consumeThreadMin;
    }

    public String getConsumeThreadMax() {
        return this.consumeThreadMax;
    }

    public void setConsumeThreadMax(String consumeThreadMax) {
        this.consumeThreadMax = consumeThreadMax;
    }

    public String getOrderlyConsumePartitionParallelism() {
        return this.orderlyConsumePartitionParallelism;
    }

    public void setOrderlyConsumePartitionParallelism(String orderlyConsumePartitionParallelism) {
        this.orderlyConsumePartitionParallelism = orderlyConsumePartitionParallelism;
    }

    public String getMaxBatchRecords() {
        return this.maxBatchRecords;
    }

    public void setMaxBatchRecords(String maxBatchRecords) {
        this.maxBatchRecords = maxBatchRecords;
    }

    public boolean isEasy() {
        return this.easy;
    }

    public void setEasy(boolean easy) {
        this.easy = easy;
    }

    public String getIsOrderly() {
        return this.isOrderly;
    }

    public void setIsOrderly(String isOrderly) {
        this.isOrderly = isOrderly;
    }

    public String getIsNewPush() {
        return this.isNewPush;
    }

    public void setIsNewPush(String isNewPush) {
        this.isNewPush = isNewPush;
    }

    public String getOrderlyConsumeThreadSize() {
        return this.orderlyConsumeThreadSize;
    }

    public void setOrderlyConsumeThreadSize(String orderlyConsumeThreadSize) {
        this.orderlyConsumeThreadSize = orderlyConsumeThreadSize;
    }
}
