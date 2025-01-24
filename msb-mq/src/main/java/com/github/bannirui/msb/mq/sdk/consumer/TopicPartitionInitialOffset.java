package com.github.bannirui.msb.mq.sdk.consumer;

import java.util.Objects;
import org.apache.kafka.common.TopicPartition;

public class TopicPartitionInitialOffset {
    private final TopicPartition topicPartition;
    private final Long initialOffset;
    private final boolean relativeToCurrent;
    private final TopicPartitionInitialOffset.SeekPosition position;

    public TopicPartitionInitialOffset(String topic, int partition) {
        this(topic, partition, null, false);
    }

    public TopicPartitionInitialOffset(String topic, int partition, Long initialOffset) {
        this(topic, partition, initialOffset, false);
    }

    public TopicPartitionInitialOffset(String topic, int partition, Long initialOffset, boolean relativeToCurrent) {
        this.topicPartition = new TopicPartition(topic, partition);
        this.initialOffset = initialOffset;
        this.relativeToCurrent = relativeToCurrent;
        this.position = null;
    }

    public TopicPartitionInitialOffset(String topic, int partition, TopicPartitionInitialOffset.SeekPosition position) {
        this.topicPartition = new TopicPartition(topic, partition);
        this.initialOffset = null;
        this.relativeToCurrent = false;
        this.position = position;
    }

    public TopicPartition topicPartition() {
        return this.topicPartition;
    }

    public int partition() {
        return this.topicPartition.partition();
    }

    public String topic() {
        return this.topicPartition.topic();
    }

    public Long initialOffset() {
        return this.initialOffset;
    }

    public boolean isRelativeToCurrent() {
        return this.relativeToCurrent;
    }

    public TopicPartitionInitialOffset.SeekPosition getPosition() {
        return this.position;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            TopicPartitionInitialOffset that = (TopicPartitionInitialOffset)o;
            return Objects.equals(this.topicPartition, that.topicPartition);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return this.topicPartition.hashCode();
    }

    public String toString() {
        return "TopicPartitionInitialOffset{topicPartition=" + this.topicPartition + ", initialOffset=" + this.initialOffset + ", relativeToCurrent=" + this.relativeToCurrent + (this.position == null ? "" : ", position=" + this.position.name()) + '}';
    }

    public static enum SeekPosition {
        BEGINNING,
        END;

        private SeekPosition() {
        }
    }
}
