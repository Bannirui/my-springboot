package com.github.bannirui.msb.mq.sdk.consumer;

public enum MsgConsumedStatus {
    SUCCEED(-1),
    /** @deprecated */
    @Deprecated
    FAILURE(0),
    RETRY(0),
    RETRY_1S(1),
    RETRY_5S(2),
    RETRY_10S(3),
    RETRY_30S(4),
    RETRY_1M(5),
    RETRY_2M(6),
    RETRY_3M(7),
    RETRY_4M(8),
    RETRY_5M(9),
    RETRY_6M(10),
    RETRY_7M(11),
    RETRY_8M(12),
    RETRY_9M(13),
    RETRY_10M(14),
    RETRY_20M(15),
    RETRY_30M(16),
    RETRY_1H(17),
    RETRY_2H(18);

    int level;

    private MsgConsumedStatus(int level) {
        this.level = level;
    }

    public int getLevel() {
        return this.level;
    }
}
