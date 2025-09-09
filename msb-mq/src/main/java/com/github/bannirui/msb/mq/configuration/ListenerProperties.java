package com.github.bannirui.msb.mq.configuration;

/**
 * MQ监听器的配置信息.
 */
public class ListenerProperties {
    private String consumerGroup;
    private String templateName;
    private String consumeThreadMax;
    private String consumeThreadMin;
    private String orderlyConsumePartitionParallelism;
    private String maxBatchRecords;
    private String consumeBatchSize;
    private String isOrderly;
    private String tags;
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

    public String getConsumerGroup() {
        return this.consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public String getTemplateName() {
        return this.templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getConsumeThreadMax() {
        return this.consumeThreadMax;
    }

    public void setConsumeThreadMax(String consumeThreadMax) {
        this.consumeThreadMax = consumeThreadMax;
    }

    public String getConsumeThreadMin() {
        return this.consumeThreadMin;
    }

    public void setConsumeThreadMin(String consumeThreadMin) {
        this.consumeThreadMin = consumeThreadMin;
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

    public String getIsOrderly() {
        return this.isOrderly;
    }

    public void setIsOrderly(String isOrderly) {
        this.isOrderly = isOrderly;
    }

    public String getTags() {
        return this.tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
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
