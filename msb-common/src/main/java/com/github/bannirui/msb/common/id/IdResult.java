package com.github.bannirui.msb.common.id;

public class IdResult {

    private long timestamp;
    private long sequence;
    private long nodeId;

    public IdResult(long timestamp, long sequence, long nodeId) {
        this.timestamp = timestamp;
        this.sequence = sequence;
        this.nodeId = nodeId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getSequence() {
        return sequence;
    }

    public long getNodeId() {
        return nodeId;
    }

    public long generateId() {
        return this.timestamp << 31 | this.sequence << 10 | this.nodeId;
    }
}
