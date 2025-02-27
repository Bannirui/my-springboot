package com.github.bannirui.msb.endpoint.health;

import com.codahale.metrics.Meter;
import com.github.bannirui.msb.ex.FrameworkException;
import com.github.bannirui.msb.mq.sdk.consumer.ConsumerFactory;
import com.github.bannirui.msb.mq.sdk.consumer.KafkaConsumerProxy;
import com.github.bannirui.msb.mq.sdk.consumer.MmsConsumerProxy;
import com.github.bannirui.msb.mq.sdk.consumer.RocketmqConsumerProxy;
import com.github.bannirui.msb.mq.sdk.metrics.MmsProducerMetrics;
import com.github.bannirui.msb.mq.sdk.producer.KafkaProducerProxy;
import com.github.bannirui.msb.mq.sdk.producer.MmsProducerProxy;
import com.github.bannirui.msb.mq.sdk.producer.ProducerFactory;
import com.github.bannirui.msb.mq.sdk.producer.RocketmqProducerProxy;
import org.apache.commons.collections.MapUtils;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.internals.Fetcher;
import org.apache.kafka.clients.consumer.internals.SubscriptionState;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.internals.Sender;
import org.apache.kafka.common.TopicPartition;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl;
import org.apache.rocketmq.client.impl.consumer.PullMessageService;
import org.apache.rocketmq.client.impl.consumer.PullRequest;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.client.impl.producer.DefaultMQProducerImpl;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.ServiceState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class MQHealthIndicator implements HealthIndicator {
    private static final Logger logger = LoggerFactory.getLogger(MQHealthIndicator.class);
    private Map<String, MmsConsumerProxy> consumers;
    private Map<String, MmsProducerProxy> producers;

    @Override
    public synchronized Health health() {
        Health health = new Health();
        health.up();
        Map<String, MmsConsumerProxy> consumers = this.getConsumers();
        Map<String, MmsProducerProxy> producers = this.getProducers();
        Map<String, String> consumerStatusMap = null;
        if(MapUtils.isNotEmpty(consumers)) {
            consumerStatusMap = this.getConsumerStatus(consumers);
            health.withDetail("consumerStatus", consumerStatusMap.toString());
            health.withDetail("consumeLatency", this.getConsumeLatency(consumers).toString());
        }
        Map<String, String> producerStatusMap = null;
        if (MapUtils.isNotEmpty(producers)) {
            producerStatusMap = this.getProducerStatus(producers);
            health.withDetail("producerStatus", producerStatusMap.toString());
            health.withDetail("sendFailRate", this.getSendFailRate(producers).toString());
        }
        if (consumerStatusMap != null && consumerStatusMap.entrySet().stream().anyMatch((entry) -> entry.getValue().equals("DOWN")) || producerStatusMap != null && producerStatusMap.entrySet().stream().anyMatch((entry) -> entry.getValue().equals("DOWN"))) {
            health.down();
        }
        return health;
    }

    private Map<String, String> getConsumerStatus(Map<String, MmsConsumerProxy> consumers) {
        Map<String, String> consumerStatusMap = new HashMap<>();
        consumers.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, (entry) -> CompletableFuture.supplyAsync(() -> this.determineConsumerClient(entry.getValue()))))
            .forEach((k, v) -> {
            try {
                consumerStatusMap.put(this.getConsumerName(k), v.get(10_000L, TimeUnit.MILLISECONDS) ? "DOWN" : "UP");
            } catch (ExecutionException | TimeoutException | InterruptedException e) {
                logger.error("消费者已关闭 {}", k, e);
                consumerStatusMap.put(k, "DOWN");
            }
        });
        return consumerStatusMap;
    }

    private Map<String, String> getProducerStatus(Map<String, MmsProducerProxy> producers) {
        Map<String, String> producerStatusMap = new HashMap<>();
        producers.entrySet().stream().filter((entry) -> !entry.getKey().equals("statistic_topic_producerinfo".concat("_").concat("PRODUCER_DEFAULT_NAME")) && !entry.getKey().equals("statistic_topic_consumerinfo".concat("_").concat("PRODUCER_DEFAULT_NAME")))
            .collect(Collectors.toMap(Map.Entry::getKey, (entry) -> CompletableFuture.supplyAsync(() -> this.determineProducerClient(entry.getValue()))))
            .forEach((k, v) -> {
            try {
                producerStatusMap.put(this.getProducerName(k), v.get(10_000L, TimeUnit.MILLISECONDS) ? "DOWN" : "UP");
            } catch (ExecutionException | TimeoutException | InterruptedException e) {
                logger.error("生产者已关闭 {}", k, e);
                producerStatusMap.put(k, "DOWN");
            }
        });
        return producerStatusMap;
    }

    private Map<String, String> getConsumeLatency(Map<String, MmsConsumerProxy> consumers) {
        Map<String, String> consumeLatencyMap = new HashMap<>();
        consumers.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, (entry) -> CompletableFuture.supplyAsync(() -> this.statisticsConsumeLatency(entry.getValue()))))
            .forEach((k, v) -> {
            try {
                consumeLatencyMap.put(this.getConsumerName(k), v.get(10_000L, TimeUnit.MILLISECONDS).toString());
            } catch (ExecutionException | TimeoutException | InterruptedException e) {
                logger.error("采集消费堆积失败:{}", k, e);
                consumeLatencyMap.put(k, "DOWN");
            }
        });
        return consumeLatencyMap;
    }

    private Map<String, String> getSendFailRate(Map<String, MmsProducerProxy> producers) {
        Map<String, String> sendFailRateMap = new HashMap<>();
        (producers.entrySet()
            .stream()
            .filter((entry) -> !entry.getKey().equals("statistic_topic_producerinfo".concat("_").concat("PRODUCER_DEFAULT_NAME")) && !entry.getKey().equals("statistic_topic_consumerinfo".concat("_").concat("PRODUCER_DEFAULT_NAME")))
            .collect(Collectors.toMap(Map.Entry::getKey, (entry) -> CompletableFuture.supplyAsync(() -> this.statisticsSendFailRate(entry.getValue())))))
            .forEach((k, v) -> {
            try {
                sendFailRateMap.put(this.getProducerName(k), v.get(10_000L, TimeUnit.MILLISECONDS));
            } catch (ExecutionException | TimeoutException | InterruptedException e) {
                logger.error("采集失败率失败 {}", k, e);
                sendFailRateMap.put(k, "DOWN");
            }
        });
        return sendFailRateMap;
    }

    private boolean determineConsumerClient(MmsConsumerProxy zmsConsumerProxy) {
        boolean isShutdown;
        if (zmsConsumerProxy instanceof KafkaConsumerProxy) {
            isShutdown = this.determineKafkaConsumerClient((KafkaConsumerProxy)zmsConsumerProxy);
        } else if (zmsConsumerProxy instanceof RocketmqConsumerProxy) {
            isShutdown = this.determineRocketMQConsumerClient((RocketmqConsumerProxy)zmsConsumerProxy);
        } else {
            logger.error("未知消费者 {}", zmsConsumerProxy);
            isShutdown = true;
        }
        return isShutdown;
    }

    private boolean determineProducerClient(MmsProducerProxy zmsProducerProxy) {
        boolean isShutdown;
        if (zmsProducerProxy instanceof KafkaProducerProxy) {
            isShutdown = this.determineKafkaProducerClient((KafkaProducerProxy)zmsProducerProxy);
        } else if (zmsProducerProxy instanceof RocketmqProducerProxy) {
            isShutdown = this.determineRocketMQProducerClient((RocketmqProducerProxy)zmsProducerProxy);
        } else {
            logger.error("未知生产者 {}", zmsProducerProxy);
            isShutdown = true;
        }
        return isShutdown;
    }

    private long statisticsConsumeLatency(MmsConsumerProxy zmsConsumerProxy) {
        long msgCount = 0L;
        if (zmsConsumerProxy instanceof RocketmqConsumerProxy) {
            msgCount = this.statisticsRocketMQConsumeLatency((RocketmqConsumerProxy)zmsConsumerProxy);
        } else if (zmsConsumerProxy instanceof KafkaConsumerProxy) {
            msgCount = this.statisticsKafkaConsumeLatency((KafkaConsumerProxy)zmsConsumerProxy);
        } else {
            logger.error("未知消费者 {}", zmsConsumerProxy);
        }
        return msgCount;
    }

    private String statisticsSendFailRate(MmsProducerProxy zmsProducerProxy) {
        String sendFailRate = "";
        try {
            Field mmsMetricsField = this.getField(zmsProducerProxy.getClass(), "zmsMetrics");
            if(Objects.nonNull(mmsMetricsField)) {
                MmsProducerMetrics zmsProducerMetrics = (MmsProducerMetrics)mmsMetricsField.get(zmsProducerProxy);
                Map<String, Double> meterMap = new HashMap<>();
                Meter meter = zmsProducerMetrics.messageFailureRate();
                meterMap.put("oneMinuteRate", meter.getOneMinuteRate());
                meterMap.put("fiveMinuteRate", meter.getFiveMinuteRate());
                meterMap.put("fifteenMinuteRate", meter.getFifteenMinuteRate());
                meterMap.put("meanRate", meter.getMeanRate());
                sendFailRate = meterMap.toString();
            }
        } catch (IllegalAccessException e) {
            logger.error("statisticsSendFailRate error", e);
        }
        return sendFailRate;
    }

    private long statisticsRocketMQConsumeLatency(RocketmqConsumerProxy rocketmqConsumerProxy) {
        long msgCount = 0L;
        try {
            Field consumerField = this.getField(rocketmqConsumerProxy.getClass(), "consumer");
            DefaultMQPushConsumer defaultMQPushConsumer = (DefaultMQPushConsumer)consumerField.get(rocketmqConsumerProxy);
            if (defaultMQPushConsumer != null) {
                DefaultMQPushConsumerImpl defaultMQPushConsumerImpl = defaultMQPushConsumer.getDefaultMQPushConsumerImpl();
                if (defaultMQPushConsumerImpl != null) {
                    MQClientInstance mqClientInstance = defaultMQPushConsumerImpl.getmQClientFactory();
                    PullMessageService pullMessageService = mqClientInstance.getPullMessageService();
                    Field pullRequestQueueField = this.getField(pullMessageService.getClass(), "pullRequestQueue");
                    LinkedBlockingQueue<PullRequest> pullRequestQueue = (LinkedBlockingQueue)pullRequestQueueField.get(pullMessageService);
                    msgCount = pullRequestQueue.stream().map((p) -> p.getProcessQueue().getMsgCount().longValue()).reduce(Long::sum).orElse(0L);
                }
            }
        } catch (IllegalAccessException e) {
            logger.error("statisticsRocketMQConsumeLatency error", e);
        }
        return msgCount;
    }

    private long statisticsKafkaConsumeLatency(KafkaConsumerProxy kafkaConsumerProxy) {
        long msgCount = 0L;
        try {
            Field consumerField = this.getField(kafkaConsumerProxy.getClass(), "consumer");
            KafkaConsumer kafkaConsumer = (KafkaConsumer) consumerField.get(kafkaConsumerProxy);
            if (kafkaConsumer != null) {
                Field fetcherField = this.getField(kafkaConsumer.getClass(), "fetcher");
                Fetcher fetcher = (Fetcher) fetcherField.get(kafkaConsumer);
                if (fetcher != null) {
                    Field completedFetchesField = this.getField(fetcher.getClass(), "completedFetches");
                    ConcurrentLinkedQueue completedFetches = (ConcurrentLinkedQueue)completedFetchesField.get(fetcher);
                    msgCount = completedFetches.size();
                }
            }
        } catch (IllegalAccessException e) {
            logger.error("statisticsKafkaConsumeLatency error", e);
        }
        return msgCount;
    }

    private boolean determineKafkaConsumerClient(KafkaConsumerProxy kafkaConsumerProxy) {
        boolean isShutdown;
        try {
            Field consumerField = this.getField(kafkaConsumerProxy.getClass(), "consumer");
            KafkaConsumer kafkaConsumer = (KafkaConsumer)consumerField.get(kafkaConsumerProxy);
            if (kafkaConsumer != null) {
                Field subscriptionsField = this.getField(kafkaConsumer.getClass(), "subscriptions");
                SubscriptionState subscriptions = (SubscriptionState)subscriptionsField.get(kafkaConsumer);
                Set<TopicPartition> topicPartitions = subscriptions.assignedPartitions();
                isShutdown = !kafkaConsumerProxy.isRunning() || topicPartitions.size() == 0;
            } else {
                isShutdown = true;
            }
        } catch (IllegalAccessException e) {
            logger.error("determineKafkaConsumerClient error", e);
            isShutdown = true;
        }
        return isShutdown;
    }

    private boolean determineRocketMQConsumerClient(RocketmqConsumerProxy rocketmqConsumerProxy) {
        boolean isShutdown;
        try {
            Field consumerField = this.getField(rocketmqConsumerProxy.getClass(), "consumer");
            DefaultMQPushConsumer defaultMQPushConsumer = (DefaultMQPushConsumer)consumerField.get(rocketmqConsumerProxy);
            if (defaultMQPushConsumer != null) {
                DefaultMQPushConsumerImpl defaultMQPushConsumerImpl = defaultMQPushConsumer.getDefaultMQPushConsumerImpl();
                if (defaultMQPushConsumerImpl != null) {
                    MQClientInstance mqClientInstance = defaultMQPushConsumerImpl.getmQClientFactory();
                    PullMessageService pullMessageService = mqClientInstance.getPullMessageService();
                    isShutdown = !rocketmqConsumerProxy.isRunning() || pullMessageService.isStopped();
                } else {
                    isShutdown = true;
                }
            } else {
                isShutdown = true;
            }
        } catch (IllegalAccessException e) {
            logger.error("determineRocketMQConsumerClient error", e);
            isShutdown = true;
        }
        return isShutdown;
    }

    private boolean determineKafkaProducerClient(KafkaProducerProxy kafkaProducerProxy) {
        boolean isShutdown;
        try {
            Field producerField = this.getField(kafkaProducerProxy.getClass(), "producer");
            KafkaProducer kafkaProducer = (KafkaProducer)producerField.get(kafkaProducerProxy);
            if (kafkaProducer != null) {
                Field senderField = this.getField(kafkaProducer.getClass(), "sender");
                Sender sender = (Sender)senderField.get(kafkaProducer);
                if (sender != null) {
                    Field runningField = this.getField(sender.getClass(), "running");
                    isShutdown = !(Boolean)runningField.get(sender);
                } else {
                    isShutdown = true;
                }
            } else {
                isShutdown = true;
            }
        } catch (IllegalAccessException e) {
            logger.error("determineKafkaProducerClient error", e);
            isShutdown = true;
        }
        return isShutdown;
    }

    private boolean determineRocketMQProducerClient(RocketmqProducerProxy rocketmqProducerProxy) {
        boolean isShutdown;
        try {
            Field producerField = this.getField(rocketmqProducerProxy.getClass(), "producer");
            DefaultMQProducer defaultMQProducer = (DefaultMQProducer)producerField.get(rocketmqProducerProxy);
            if (defaultMQProducer != null) {
                Field defaultMQProducerImplField = this.getField(defaultMQProducer.getClass(), "defaultMQProducerImpl");
                DefaultMQProducerImpl defaultMQProducerImpl = (DefaultMQProducerImpl)defaultMQProducerImplField.get(defaultMQProducer);
                if (defaultMQProducerImpl != null) {
                    isShutdown = !defaultMQProducerImpl.getServiceState().equals(ServiceState.RUNNING);
                } else {
                    isShutdown = true;
                }
            } else {
                isShutdown = true;
            }
        } catch (IllegalAccessException e) {
            logger.error("determineRocketMQProducerClient error", e);
            isShutdown = true;
        }
        return isShutdown;
    }

    private Map<String, MmsConsumerProxy> getConsumers() {
        if (this.consumers == null) {
            try {
                Field field = this.getField(ConsumerFactory.class, "consumers");
                this.consumers = (Map)field.get(ConsumerFactory.class);
            } catch (IllegalAccessException e) {
                logger.error("获取consumers失败", e);
            }
        }
        return this.consumers;
    }

    private Map<String, MmsProducerProxy> getProducers() {
        if (this.producers == null) {
            try {
                Field field = this.getField(ProducerFactory.class, "topicProducers");
                this.producers = (Map)field.get(ProducerFactory.class);
            } catch (IllegalAccessException e) {
                logger.error("获取producers失败", e);
            }
        }
        return this.producers;
    }

    private String getConsumerName(String name) {
        return name.replaceAll("_".concat("CONSUMER_DEFAULT_NAME"), "");
    }

    private String getProducerName(String name) {
        return name.replaceAll("_".concat("PRODUCER_DEFAULT_NAME"), "");
    }

    private Field getField(Class<?> sourceClass, String fieldName) {
        Field field = ReflectionUtils.findField(sourceClass, fieldName);
        if (field == null) {
            throw FrameworkException.getInstance("{0} 找不到 {1} 属性", sourceClass.getName(), fieldName);
        } else {
            field.setAccessible(true);
            return field;
        }
    }
}
