package com.github.bannirui.msb.mq.sdk.consumer;

import com.github.bannirui.mms.client.consumer.KafkaConsumerProxy;
import com.github.bannirui.mms.client.consumer.KafkaLiteConsumerProxy;
import com.github.bannirui.mms.client.consumer.MessageListener;
import com.github.bannirui.mms.client.consumer.MmsConsumerProxy;
import com.github.bannirui.mms.client.consumer.RocketmqConsumerProxy;
import com.github.bannirui.mms.client.consumer.RocketmqLiteConsumerProxy;
import com.github.bannirui.mms.common.BrokerType;
import com.github.bannirui.mms.metadata.ConsumerGroupMetadata;
import com.github.bannirui.msb.mq.sdk.common.SLA;
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
        // mq消息消费有序性
        boolean order=Objects.isNull(sla)?false:sla.isOrderly();
        String name = (String)configProperties.get("name");
        Set<String> tags = (Set<String>) configProperties.get("tags");
        Properties properties = (Properties)configProperties.get("properties");
        MessageListener listener = (MessageListener)configProperties.get("listener");
        if(Objects.equals(BrokerType.ROCKETMQ, metadata.getClusterMetadata().getBrokerType())) {
            if (!StringUtils.isBlank(metadata.getReleaseStatus()) && !metadata.getReleaseStatus().equals("all")) {
                if (isNewPush) {
                    consumer = new MmsLiteConsumerGroupProxy(metadata, order, name, tags, properties, listener, BrokerType.ROCKETMQ.getName());
                } else {
                    consumer = new MmsConsumerGroupProxy(metadata, order, name, tags, properties, listener, BrokerType.ROCKETMQ.getName());
                }
            } else if (isNewPush) {
                consumer = new RocketmqLiteConsumerProxy(metadata, order, name, tags, properties, listener);
            } else {
                consumer = new RocketmqConsumerProxy(metadata, order, name, tags, properties, listener);
            }
        } else if (!StringUtils.isBlank(metadata.getReleaseStatus()) && !metadata.getReleaseStatus().equals("all")) {
            if (isNewPush)
                consumer = new MmsLiteConsumerGroupProxy(metadata, order, name, null, properties, listener, BrokerType.KAFKA.getName());
            else {
                consumer = new MmsConsumerGroupProxy(metadata, order, name, null, properties, listener, BrokerType.KAFKA.getName());
            }
        } else if (isNewPush) {
            consumer = new KafkaLiteConsumerProxy(metadata, order, name, properties, listener);
        } else {
            consumer = new KafkaConsumerProxy(metadata, order, name, properties, listener);
        }
        return consumer;
    }
}
