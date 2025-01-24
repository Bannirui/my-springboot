package com.github.bannirui.msb.mq.sdk.message.statistic.kafka;

import java.util.List;

public class KafkaConsumerStatistics {
    List<KafkaConsumerInfo> kafkaConsumerInfos;

    public List<KafkaConsumerInfo> getKafkaConsumerInfos() {
        return this.kafkaConsumerInfos;
    }

    public void setKafkaConsumerInfos(List<KafkaConsumerInfo> kafkaConsumerInfos) {
        this.kafkaConsumerInfos = kafkaConsumerInfos;
    }
}
