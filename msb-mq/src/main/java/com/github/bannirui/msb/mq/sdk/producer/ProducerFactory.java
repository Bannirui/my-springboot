package com.github.bannirui.msb.mq.sdk.producer;

import com.github.bannirui.msb.mq.sdk.common.BrokerType;
import com.github.bannirui.msb.mq.sdk.common.SLA;
import com.github.bannirui.msb.mq.sdk.common.MmsException;
import com.github.bannirui.msb.mq.sdk.metadata.TopicMetadata;
import com.github.bannirui.msb.mq.sdk.zookeeper.RouterManager;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProducerFactory {
    private static final Logger logger = LoggerFactory.getLogger(ProducerFactory.class);
    private static final Map<String, MmsProducerProxy> TOPIC_PRODUCERS;
    static {
        TOPIC_PRODUCERS = new ConcurrentHashMap<>();
    }

    public static MmsProducerProxy getProducer(String topic, String name) {
        if (!"PRODUCER_DEFAULT_NAME".equalsIgnoreCase(name) && !StringUtils.isEmpty(name)) {
            return doGetProducer(topic, name);
        } else {
            throw MmsException.INVALID_NAME_EXCEPTION;
        }
    }

    public static MmsProducerProxy getProducer(String topic, String name, Properties properties) {
        if(StringUtils.isNotEmpty(name) && !"PRODUCER_DEFAULT_NAME".equalsIgnoreCase(name)) {
            return doGetProducer(topic, name, properties);
        } else {
            throw MmsException.INVALID_NAME_EXCEPTION;
        }
    }

    private static MmsProducerProxy doGetProducer(String topic, String name, Properties properties) {
        checkTopic(topic);
        String cacheName = topic + "_" + name;
        if (TOPIC_PRODUCERS.get(cacheName) == null) {
            synchronized(ProducerFactory.class) {
                if (TOPIC_PRODUCERS.get(cacheName) == null) {
                    MmsProducerProxy producer = null;
                    TopicMetadata metadata = null;
                    try {
                        metadata = RouterManager.getZkInstance().readTopicMetadata(topic);
                    } catch (Exception var9) {
                        logger.error("read topic {} metadata error", topic, var9);
                        throw MmsException.METAINFO_EXCEPTION;
                    }
                    logger.info("Producer create: topic metadata is {}", metadata.toString());
                    if (BrokerType.ROCKETMQ.equals(metadata.getClusterMetadata().getBrokerType())) {
                        producer = new RocketmqProducerProxy(metadata, new SLA(), name, properties);
                    } else {
                        producer = new KafkaProducerProxy(metadata, new SLA(), name, properties);
                    }
                    TOPIC_PRODUCERS.putIfAbsent(cacheName, producer);
                    return producer;
                }
            }
        }

        return TOPIC_PRODUCERS.get(cacheName);
    }

    private static MmsProducerProxy doGetProducer(String topic, String name) {
        checkTopic(topic);
        String cacheName = topic + "_" + name;
        if (TOPIC_PRODUCERS.get(cacheName) == null) {
            synchronized(TOPIC_PRODUCERS) {
                if (TOPIC_PRODUCERS.get(cacheName) == null) {
                    MmsProducerProxy producer = null;
                    TopicMetadata metadata = null;
                    try {
                        metadata = RouterManager.getZkInstance().readTopicMetadata(topic);
                    } catch (Exception e) {
                        logger.error("read topic {} metadata error", topic, e);
                        throw MmsException.METAINFO_EXCEPTION;
                    }
                    logger.info("Producer create: topic metadata is {}", metadata.toString());
                    if (BrokerType.ROCKETMQ.equals(metadata.getClusterMetadata().getBrokerType())) {
                        producer = new RocketmqProducerProxy(metadata, new SLA(), name);
                    } else {
                        producer = new KafkaProducerProxy(metadata, new SLA(), name);
                    }
                    TOPIC_PRODUCERS.putIfAbsent(cacheName, producer);
                    return producer;
                }
            }
        }
        return TOPIC_PRODUCERS.get(cacheName);
    }

    public static MmsProducerProxy getProducer(String topic) {
        return doGetProducer(topic, "PRODUCER_DEFAULT_NAME");
    }

    public static MmsProducerProxy getProducer(String topic, Properties properties) {
        return properties != null && !properties.isEmpty() ? doGetProducer(topic, "PRODUCER_DEFAULT_NAME", properties) : getProducer(topic);
    }

    public static synchronized void shutdown() {
        TOPIC_PRODUCERS.entrySet().forEach(entry -> {
            entry.getValue().shutdown();
        });
        TOPIC_PRODUCERS.clear();
        logger.info("ProducerFactory has been shutdown");
    }

    public static synchronized void shutdown(String topic) {
        String key = topic + "_" + "PRODUCER_DEFAULT_NAME";
        if (TOPIC_PRODUCERS.containsKey(key)) {
            TOPIC_PRODUCERS.get(key).shutdown();
            TOPIC_PRODUCERS.remove(key);
        }
        logger.info("Producer of " + topic + " has been shutdown");
    }

    public static void recycle(String name, String instanceName) {
        String key = name + "_" + instanceName;
        TOPIC_PRODUCERS.remove(key);
        logger.info("producer {} has been remove", key);
    }

    public static Collection<MmsProducerProxy> getProducers() {
        return TOPIC_PRODUCERS.values();
    }

    private static void checkTopic(String topic) {
        if (topic.contains(" ")) {
            logger.warn("topic 中有空格，请检查 topic 是否填多了空格，这很可能会消息发送不成功！");
        }
    }
}
