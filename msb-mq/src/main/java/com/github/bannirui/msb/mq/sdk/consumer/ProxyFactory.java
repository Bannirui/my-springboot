package com.github.bannirui.msb.mq.sdk.consumer;

import com.github.bannirui.msb.mq.sdk.common.BrokerType;
import com.github.bannirui.msb.mq.sdk.common.SLA;
import com.github.bannirui.msb.mq.sdk.metadata.ConsumerGroupMetadata;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

public class ProxyFactory {

    public static MmsConsumerProxy getProxy(Properties configProperties) {
        MmsConsumerProxy consumer = null;
        ConsumerGroupMetadata metadata = (ConsumerGroupMetadata)configProperties.get("metadata");
        Boolean isNewPush = (Boolean)configProperties.get("isNewPush");
        SLA sla = (SLA) configProperties.get("sla");
        String name = (String)configProperties.get("name");
        Set<String> tags = (Set<String>) configProperties.get("tags");
        Properties properties = (Properties)configProperties.get("properties");
        MessageListener listener = (MessageListener)configProperties.get("listener");
        if(Objects.equals(BrokerType.ROCKETMQ, metadata.getClusterMetadata().getBrokerType())) {
            if (!StringUtils.isBlank(metadata.getReleaseStatus()) && !metadata.getReleaseStatus().equals("all")) {
                if (isNewPush) {
                    consumer = new MmsLiteConsumerGroupProxy(metadata, sla, name, tags, properties, listener, BrokerType.ROCKETMQ.getName());
                } else {
                    consumer = new MmsConsumerGroupProxy(metadata, sla, name, tags, properties, listener, BrokerType.ROCKETMQ.getName());
                }
            } else if (isNewPush) {
                consumer = new RocketmqLiteConsumerProxy(metadata, sla, name, tags, properties, listener);
            } else {
                consumer = new RocketmqConsumerProxy(metadata, sla, name, tags, properties, listener);
            }
        } else if (!StringUtils.isBlank(metadata.getReleaseStatus()) && !metadata.getReleaseStatus().equals("all")) {
            if (isNewPush)
                consumer = new MmsLiteConsumerGroupProxy(metadata, sla, name, null, properties, listener, BrokerType.KAFKA.getName());
            else {
                consumer = new MmsConsumerGroupProxy(metadata, sla, name, null, properties, listener, BrokerType.KAFKA.getName());
            }
        } else if (isNewPush) {
            consumer = new KafkaLiteConsumerProxy(metadata, sla, name, properties, listener);
        } else {
            consumer = new KafkaConsumerProxy(metadata, sla, name, properties, listener);
        }
        return consumer;
    }
}
