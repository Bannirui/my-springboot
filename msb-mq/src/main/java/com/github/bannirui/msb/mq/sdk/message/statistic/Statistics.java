package com.github.bannirui.msb.mq.sdk.message.statistic;

import java.util.ArrayList;
import java.util.List;

public class Statistics {
    private List<ProducerStatisticsInfo> producerInfos = new ArrayList<>();
    private List<ConsumerStatisticsInfo> consumerInfos = new ArrayList<>();

    public List<ProducerStatisticsInfo> getProducerInfos() {
        return this.producerInfos;
    }

    public void setProducerInfos(List<ProducerStatisticsInfo> producerInfos) {
        this.producerInfos = producerInfos;
    }

    public List<ConsumerStatisticsInfo> getConsumerInfos() {
        return this.consumerInfos;
    }

    public void setConsumerInfos(List<ConsumerStatisticsInfo> consumerInfos) {
        this.consumerInfos = consumerInfos;
    }
}
