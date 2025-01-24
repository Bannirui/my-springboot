package com.github.bannirui.msb.mq.sdk.consumer;

import com.github.bannirui.msb.mq.sdk.common.RocketMQConsumeType;
import com.github.bannirui.msb.mq.sdk.common.SLA;
import com.github.bannirui.msb.mq.sdk.common.MmsException;
import com.github.bannirui.msb.mq.sdk.metadata.ConsumerGroupMetadata;
import com.github.bannirui.msb.mq.sdk.zookeeper.RouterManager;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumerFactory {
    /**
     * <ul>
     *     <li>key ConsumerGroup_ConsumerName</li>
     *     <li>val </li>
     * </ul>
     */
    private static final Map<String, MmsConsumerProxy> consumers = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(ConsumerFactory.class);

    public static MmsConsumerProxy getConsumer(ConsumerGroup consumerGroup, Properties properties, MessageListener listener) {
        return doGetConsumer(consumerGroup.getConsumerGroup(), consumerGroup.getConsumerName(), consumerGroup.getTags(), properties == null ? new Properties() : properties, listener);
    }

    /**
     *
     * @param consumerGroup consumer group name
     * @param name consumer name
     * @param tags
     * @param properties
     * @param listener
     * @return
     */
    private static MmsConsumerProxy doGetConsumer(String consumerGroup, String name, Set<String> tags, Properties properties, MessageListener listener) {
        if (consumerGroup.contains(" ")) {
            logger.warn("consumerGroup中有空格 请检查监听器中的consumerGroup是否填多了空格 这很可能会导致消费监听不成功");
        }
        String cacheName = consumerGroup + "_" + name;
        return consumers.computeIfAbsent(cacheName, k->{
            MmsConsumerProxy consumer = null;
            ConsumerGroupMetadata metadata = null;
            try {
                metadata = RouterManager.getZkInstance().readConsumerGroupMetadata(consumerGroup);
            } catch (Throwable e) {
                logger.error("get consumer metadata error", e);
                throw MmsException.METAINFO_EXCEPTION;
            }
            if(Objects.isNull(metadata)) {
                throw MmsException.MetainfoException;
            }
            rewriteConsumerGroup(metadata);
            logger.info("Consumer created {}", metadata);
            SLA sla = SLA.parse(properties);
            RocketMQConsumeType type = RocketMQConsumeType.parse(properties);
            Properties configProperties = new Properties();
            configProperties.put("metadata", metadata);
            configProperties.put("sla", sla);
            configProperties.put("name", name);
            configProperties.put("tags", tags);
            configProperties.put("properties", properties);
            configProperties.put("listener", listener);
            configProperties.put("isNewPush", type.isNewPush);
            consumer = ProxyFactory.getProxy(configProperties);
            consumers.putIfAbsent(cacheName, consumer);
            return consumer;
        });
    }

    protected static void rewriteConsumerGroup(ConsumerGroupMetadata metadata) {
        String env = System.getProperty("env");
        String mqTag = System.getProperty("mqTag");
        String mqColor = System.getProperty("mqColor");
        String mmsRewrite;
        if (!"PRO".equals(env) || StringUtils.isNotBlank(mqTag)) {
            mmsRewrite = System.getProperty("mmsRewrite");
            if (StringUtils.isNotBlank(mmsRewrite)) {
                String rewriteConsumerGroup = mmsRewrite + metadata.getName();
                metadata.setName(rewriteConsumerGroup);
                RouterManager.getZkInstance().writeConsumerGroupMetadata(metadata);
            }
        }
        mmsRewrite = metadata.getReleaseStatus();
        if (StringUtils.isNotBlank(mmsRewrite) && !mmsRewrite.equals("all") && StringUtils.isBlank(mqColor)) {
            throw MmsException.RELEASE_EXCEPTION;
        }
    }

    public static synchronized void shutdown() {
        consumers.forEach((k,v)->{
            v.shutdown();
        });
        consumers.clear();
        logger.info("ConsumerFactory shutdown");
    }

    public static synchronized void shutdown(String consumerGroup) {
        String key = consumerGroup + "_" + "CONSUMER_DEFAULT_NAME";
        if (consumers.containsKey(key)) {
            consumers.get(key).shutdown();
            consumers.remove(key);
        }
        logger.info("ConsumerFactory shutdown");
    }

    public static void add(String name, MmsConsumerProxy consumer) {
        consumers.putIfAbsent(name + "_" + "CONSUMER_DEFAULT_NAME", consumer);
        logger.info("Consumer {} is put", consumer.getMetadata().getName());
    }

    public static void recycle(String name, String instanceName) {
        String key = name + "_" + instanceName;
        consumers.remove(key);
        logger.info("Consumer {} removed", key);
    }

    public static Collection<MmsConsumerProxy> getConsumers() {
        return consumers.values();
    }
}
