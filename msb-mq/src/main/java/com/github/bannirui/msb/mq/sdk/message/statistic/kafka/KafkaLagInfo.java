package com.github.bannirui.msb.mq.sdk.message.statistic.kafka;

public class KafkaLagInfo {
    private int partition;
    private double lag;

    public int getPartition() {
        return this.partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public double getLag() {
        return this.lag;
    }

    public void setLag(double lag) {
        this.lag = lag;
    }
}
