package com.github.bannirui.msb.mq.sdk.consumer;

import com.google.common.collect.Sets;
import java.util.Set;

public class ConsumerGroup {
    /**
     * consumer group
     */
    private String consumerGroup;
    /**
     * consumer
     */
    private String consumerName = "CONSUMER_DEFAULT_NAME";
    /**
     * tag *标识所有
     */
    private Set<String> tags = Sets.newHashSet();

    public ConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public ConsumerGroup(String consumerGroup, String consumerName) {
        this.consumerGroup = consumerGroup;
        this.consumerName = consumerName;
    }

    public ConsumerGroup(String consumerGroup, String consumerName, Set<String> tags) {
        this.consumerGroup = consumerGroup;
        this.consumerName = consumerName;
        this.tags = tags;
    }

    public String getConsumerGroup() {
        return this.consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public String getConsumerName() {
        return this.consumerName;
    }

    public void setConsumerName(String consumerName) {
        this.consumerName = consumerName;
    }

    public Set<String> getTags() {
        return this.tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }
}
