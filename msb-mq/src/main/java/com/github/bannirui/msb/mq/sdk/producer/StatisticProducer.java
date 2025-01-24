package com.github.bannirui.msb.mq.sdk.producer;

import com.github.bannirui.msb.mq.sdk.metadata.ClusterMetadata;
import com.github.bannirui.msb.mq.sdk.zookeeper.RouterManager;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatisticProducer {
    private static final Logger logger = LoggerFactory.getLogger(StatisticProducer.class);
    private static final KafkaProducer<String, byte[]> statistics_producer;

    static {
        Properties kafkaConfig = new Properties();
        kafkaConfig.put("acks", "1");
        kafkaConfig.put("retries", 0);
        kafkaConfig.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        kafkaConfig.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        kafkaConfig.put("batch.size", "0");
        ClusterMetadata clusterMetadata = RouterManager.getZkInstance().readClusterMetadata("statistic_cluster");
        kafkaConfig.put("bootstrap.servers", clusterMetadata.getBootAddr());
        statistics_producer = new KafkaProducer<>(kafkaConfig);
    }

    public static void sendMessage(byte[] message) {
        ProducerRecord<String, byte[]> record = new ProducerRecord<>("statistic_topic_producerinfo", message);
        Future<RecordMetadata> send = statistics_producer.send(record);
        try {
            RecordMetadata o = send.get();
            long offset = o.offset();
            logger.info("statistics data offset is:{} ", offset);
        } catch (ExecutionException | InterruptedException var6) {
            logger.error("StatisticProducer send message failure", var6);
        }
    }
}
