package com.github.bannirui.msb.mq.sdk.consumer;

import com.github.bannirui.msb.mq.sdk.common.ConsumeFrom;
import com.github.bannirui.msb.mq.sdk.common.KafkaVersion;
import com.github.bannirui.msb.mq.sdk.common.SLA;
import com.github.bannirui.msb.mq.sdk.common.MmsEnv;
import com.github.bannirui.msb.mq.sdk.config.DefaultMmsClientConfig;
import com.github.bannirui.msb.mq.sdk.config.MmsClientConfig;
import com.github.bannirui.msb.mq.sdk.crypto.MMSCryptoManager;
import com.github.bannirui.msb.mq.sdk.metadata.ConsumerGroupMetadata;
import com.github.bannirui.msb.mq.sdk.metadata.MmsMetadata;
import com.github.bannirui.msb.mq.sdk.metrics.KafkaConsumerStatusReporter;
import com.google.common.collect.Lists;
import java.lang.reflect.Field;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.NoOffsetForPartitionException;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.consumer.OffsetCommitCallback;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaLiteConsumerProxy extends MmsConsumerProxy<ConsumerRecord<String, byte[]>> {

    private final int consumerPollTimeoutMs = Integer.parseInt(System.getProperty("consumer.poll.timeout.ms", "3000"));
    private final int concurrentlyThreadPoolQueueSize = Integer.parseInt(System.getProperty("concurrently.threadpool.queue.size", "10000"));
    private final int orderlyPartitionMaxConsumeRecords = Integer.parseInt(System.getProperty("orderly.partition.max.consume.records", "2000"));
    KafkaConsumer<String, byte[]> consumer;
    private final Properties kafkaProperties = new Properties();
    private final Map<String, Map<Integer, Long>> offsets = new ConcurrentHashMap<>();
    private final Map<Integer, KafkaLiteConsumerProxy.ConsumeMessageService> consumeMessageServiceTable = new ConcurrentHashMap<>();
    private final KafkaLiteConsumerProxy.PartitionOperateContext partitionOperateContext = new KafkaLiteConsumerProxy.PartitionOperateContext();
    private int consumeBatchSize = 1;
    private List<AbstractConsumerRunner> consumerRunners = new ArrayList<>();
    private ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
    private ReentrantReadWriteLock.ReadLock commitsReadLock;

    private void addOffset(ConsumerRecord<String, byte[]> record, long offset) {
        long off = offset > -1L ? offset : record.offset();
        (this.offsets.computeIfAbsent(record.topic(), (v) -> new ConcurrentHashMap<>())).compute(record.partition(), (k, v) -> Objects.isNull(v) ? off : Math.max(v, off));
    }

    public KafkaLiteConsumerProxy(MmsMetadata metadata, SLA sla, String instanceName, Properties properties, MessageListener listener) {
        super(metadata, sla, instanceName, properties, listener);
        this.commitsReadLock = this.reentrantReadWriteLock.readLock();
        this.instanceName = instanceName;
        this.start();
    }

    protected void consumerStart() {
        this.kafkaProperties.putAll(DefaultMmsClientConfig.DEFAULT_KAFKA_CONSUMER_CONFIG);
        if (this.metadata.isGatedLaunch()) {
            this.kafkaProperties.put("bootstrap.servers", this.metadata.getGatedCluster().getBootAddr());
        } else {
            this.kafkaProperties.put("bootstrap.servers", this.metadata.getClusterMetadata().getBootAddr());
        }
        this.kafkaProperties.put("group.id", this.metadata.getName());
        this.kafkaProperties.put("enable.auto.commit", false);
        this.kafkaProperties.put("client.id", this.metadata.getName() + "--" + MmsEnv.MMS_IP + "--" + ThreadLocalRandom.current().nextInt(100_000));
        String consumeFrom = ((ConsumerGroupMetadata)this.metadata).getConsumeFrom();
        if (StringUtils.isEmpty(consumeFrom)) {
            this.kafkaProperties.put("auto.offset.reset", ConsumeFrom.EARLIEST.getName());
        } else if (ConsumeFrom.EARLIEST.getName().equalsIgnoreCase(consumeFrom)) {
            this.kafkaProperties.put("auto.offset.reset", ConsumeFrom.EARLIEST.getName());
        } else if (ConsumeFrom.LATEST.getName().equalsIgnoreCase(consumeFrom)) {
            this.kafkaProperties.put("auto.offset.reset", ConsumeFrom.LATEST.getName());
        } else {
            this.kafkaProperties.put("auto.offset.reset", ConsumeFrom.NONE.getName());
        }
        if (this.customizedProperties != null) {
            this.addUserDefinedProperties(this.customizedProperties);
        }
        logger.info("consumer {} start with param {}", this.instanceName, this.buildConsumerInfo(this.kafkaProperties));
        this.consumer = new KafkaConsumer<>(this.kafkaProperties);
        this.consumer.subscribe(Lists.newArrayList(((ConsumerGroupMetadata)this.metadata).getBindingTopic()), new ConsumerRebalanceListener() {
            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> collection) {
                MmsConsumerProxy.logger.info("partition revoked for {} at {}", KafkaLiteConsumerProxy.this.metadata.getName(), LocalDateTime.now());
                KafkaLiteConsumerProxy.this.commitOffsets();
            }

            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> collection) {
                MmsConsumerProxy.logger.info("partition assigned for {} at {}", KafkaLiteConsumerProxy.this.metadata.getName(), LocalDateTime.now());
                MmsConsumerProxy.logger.info("partition assigned " + StringUtils.joinWith(",", new Object[]{collection}));
                TopicPartition partition;
                for(Iterator<TopicPartition> v = collection.iterator(); v.hasNext(); KafkaLiteConsumerProxy.this.getOrCreateConsumeMessageService(partition)) {
                    partition = (TopicPartition)v.next();
                    OffsetAndMetadata offset = KafkaLiteConsumerProxy.this.consumer.committed(partition);
                    if (offset == null) {
                        if (ConsumeFrom.EARLIEST.getName().equalsIgnoreCase(String.valueOf(KafkaLiteConsumerProxy.this.kafkaProperties.get("auto.offset.reset")))) {
                            KafkaLiteConsumerProxy.this.consumer.seekToBeginning(Collections.singleton(partition));
                        }
                        if (ConsumeFrom.LATEST.getName().equalsIgnoreCase(String.valueOf(KafkaLiteConsumerProxy.this.kafkaProperties.get("auto.offset.reset")))) {
                            KafkaLiteConsumerProxy.this.consumer.seekToEnd(Collections.singleton(partition));
                        }
                    }
                }
                KafkaLiteConsumerProxy.this.consumeMessageServiceTable.forEach((partitionx, consumeMessageService) -> {
                    if (!collection.contains(partitionx)) {
                        consumeMessageService.stop();
                        KafkaLiteConsumerProxy.this.consumeMessageServiceTable.remove(partitionx);
                    }
                });
            }
        });
    }

    private KafkaLiteConsumerProxy.ConsumeMessageService getOrCreateConsumeMessageService(TopicPartition partition) {
        return this.consumeMessageServiceTable.computeIfAbsent(partition.partition(), (key) -> this.createConsumeMessageService(partition));
    }

    private String buildConsumerInfo(Properties properties) {
        StringBuilder stringBuilder = new StringBuilder();
        properties.forEach((k,v)->{
            stringBuilder.append(v).append(": ").append(v);
            stringBuilder.append(System.lineSeparator());
        });
        return stringBuilder.toString();
    }

    public void register(MessageListener listener) {
        String threadName = "MmsKafkaPollThread-" + this.metadata.getName() + "-" + this.instanceName + LocalDateTime.now();
        KafkaVersion.checkVersion();
        Thread mmsPullThread = new Thread(() -> {
            while(true) {
                try {
                    if (this.running) {
                        try {
                            ConsumerRecords<String, byte[]> records = this.consumer.poll(Duration.ofMillis((long)this.consumerPollTimeoutMs));
                            if (logger.isDebugEnabled()) {
                                logger.debug("messaged pulled at {} for topic {} ", System.currentTimeMillis(), ((ConsumerGroupMetadata)this.metadata).getBindingTopic());
                            }
                            this.submitRecords(records);
                            this.commitOffsets();
                            this.partitionOperateContext.pause();
                            this.partitionOperateContext.resume();
                        } catch (NoOffsetForPartitionException e) {
                            Thread.sleep(100L);
                            logger.error("can not find offset,continue to cycle", e);
                        }
                        continue;
                    }
                } catch (WakeupException e) {
                    logger.info("consumer poll wakeup:{}", e.getMessage());
                } catch (Throwable e) {
                    logger.error("consume poll error", e);
                } finally {
                    logger.error("consumer will shutdown.");
                    super.setRunning(false);
                    this.consumerShutdown();
                }
                return;
            }
        }, threadName);
        mmsPullThread.setUncaughtExceptionHandler((t, e) -> {
            logger.error("{} thread get a.factories.Interceptor.properties exception ", threadName, e);
        });
        mmsPullThread.start();
        logger.info("ConsumerProxy started at {}, consumer group name:{}", System.currentTimeMillis(), this.metadata.getName());
    }

    private void submitRecords(ConsumerRecords<String, byte[]> records) {
        if (records != null && !records.isEmpty()) {
            Iterable<ConsumerRecord<String, byte[]>> recordsIter = records.records(((ConsumerGroupMetadata)this.metadata).getBindingTopic());
            ArrayList<ConsumerRecord<String, byte[]>> consumerRecords = Lists.newArrayList(recordsIter);
            Map<Integer, List<ConsumerRecord<String, byte[]>>> consumerRecordsMap = consumerRecords.stream().collect(Collectors.groupingBy(ConsumerRecord::partition));
            consumerRecordsMap.forEach((k,v)->{
                TopicPartition topicPartition = new TopicPartition(v.getFirst().topic(), v.getFirst().partition());
                KafkaLiteConsumerProxy.ConsumeMessageService consumeMessageService = this.getOrCreateConsumeMessageService(topicPartition);
                consumeMessageService.execute(v);
            });
        }
    }

    private void commitOffsets() {
        Map<TopicPartition, OffsetAndMetadata> commits = this.buildCommits();
        if (!commits.isEmpty()) {
            this.consumer.commitAsync(commits, new KafkaLiteConsumerProxy.LoggingCommitCallback());
        }
    }

    private Map<TopicPartition, OffsetAndMetadata> buildCommits() {
        this.commitsReadLock.lock();
        try {
            Map<TopicPartition, OffsetAndMetadata> commits = new HashMap<>();
            this.offsets.forEach((k,v)-> v.forEach((key, val)-> commits.put(new TopicPartition(String.valueOf(key), key), new OffsetAndMetadata(val + 1L))));
            this.offsets.clear();
            return commits;
        } finally {
            this.commitsReadLock.unlock();
        }
    }

    protected void consumerShutdown() {
        try {
            this.consumer.close();
        } catch (ConcurrentModificationException e) {
            logger.info("consumer shutdown changes to wakeup for: {}", e.getMessage());
            this.consumer.wakeup();
        }
        this.consumerRunners.forEach(AbstractConsumerRunner::stop);
        this.consumerRunners.clear();
    }

    public void statistics() {
        super.statistics();
        KafkaConsumerStatusReporter.getInstance().reportConsumerStatus();
    }

    public void addUserDefinedProperties(Properties properties) {
        if (properties.containsKey(MmsClientConfig.CONSUMER.MAX_BATCH_RECORDS.getKey())) {
            this.kafkaProperties.put("max.poll.records", properties.get(MmsClientConfig.CONSUMER.MAX_BATCH_RECORDS.getKey()));
        } else if (properties.containsKey(MmsClientConfig.CONSUMER.CONSUME_MESSAGES_SIZE.getKey())) {
            this.kafkaProperties.put("max.poll.records", properties.get(MmsClientConfig.CONSUMER.CONSUME_MESSAGES_SIZE.getKey()));
        }
        if (properties.containsKey(MmsClientConfig.CONSUMER.CONSUME_TIMEOUT_MS.getKey())) {
            this.kafkaProperties.put("max.poll.interval.ms", properties.get(MmsClientConfig.CONSUMER.CONSUME_TIMEOUT_MS.getKey()));
        }
        if (properties.containsKey(MmsClientConfig.CONSUMER.CONSUME_BATCH_SIZE.getKey())) {
            this.consumeBatchSize = Integer.parseInt(String.valueOf(properties.get(MmsClientConfig.CONSUMER.CONSUME_BATCH_SIZE.getKey())));
        }
        int threadsNumMin=0;
        if (properties.containsKey(MmsClientConfig.CONSUMER.CONSUME_THREAD_MIN.getKey())) {
            threadsNumMin = Integer.parseInt(String.valueOf(properties.get(MmsClientConfig.CONSUMER.CONSUME_THREAD_MIN.getKey())));
        } else {
            threadsNumMin = Runtime.getRuntime().availableProcessors();
        }
        int threadsNumMax=0;
        if (properties.containsKey(MmsClientConfig.CONSUMER.CONSUME_THREAD_MAX.getKey())) {
            threadsNumMax = Integer.parseInt(String.valueOf(properties.get(MmsClientConfig.CONSUMER.CONSUME_THREAD_MAX.getKey())));
        } else {
            threadsNumMax = Math.max(Runtime.getRuntime().availableProcessors() * 2, threadsNumMin);
        }
        logger.info("kafka consumer thread set to min: {} max: {}", threadsNumMin, threadsNumMax);
        int orderlyConsumeThreadSize;
        if (!this.sla.isOrderly()) {
            for(orderlyConsumeThreadSize = 0; orderlyConsumeThreadSize < threadsNumMax; ++orderlyConsumeThreadSize) {
                KafkaLiteConsumerProxy.AbstractConsumerRunner consumerRunner = new KafkaLiteConsumerProxy.ConcurrentlyConsumerRunner();
                String threadName = "MmsKafkaMqConcurrentlyConsumeThread_" + this.metadata.getName() + "_" + orderlyConsumeThreadSize;
                this.startConsumerThread(threadName, consumerRunner);
                this.consumerRunners.add(consumerRunner);
            }
        } else {
            orderlyConsumeThreadSize = threadsNumMin;
            if (this.customizedProperties.containsKey(MmsClientConfig.CONSUMER.ORDERLY_CONSUME_THREAD_SIZE.getKey())) {
                orderlyConsumeThreadSize = Integer.parseInt(String.valueOf(this.customizedProperties.get(MmsClientConfig.CONSUMER.ORDERLY_CONSUME_THREAD_SIZE.getKey())));
                orderlyConsumeThreadSize = Math.max(threadsNumMin, orderlyConsumeThreadSize);
            }
            for(int i = 0; i < orderlyConsumeThreadSize; ++i) {
                KafkaLiteConsumerProxy.AbstractConsumerRunner consumerRunner = new KafkaLiteConsumerProxy.OrderConsumerRunner();
                String threadName = "MmsKafkaMqOrderlyConsumeThread_" + this.metadata.getName() + "_" + i;
                this.startConsumerThread(threadName, consumerRunner);
                this.consumerRunners.add(consumerRunner);
            }
        }
    }

    private void startConsumerThread(String threadName, KafkaLiteConsumerProxy.AbstractConsumerRunner consumerRunner) {
        Thread consumerThread = new Thread(consumerRunner);
        consumerThread.setName(threadName);
        consumerThread.start();
    }

    protected void decryptMsgBodyIfNecessary(ConsumerRecord<String, byte[]> msg) {
        Headers headers = msg.headers();
        Header header = headers.lastHeader("encrypt_mark");
        if(Objects.nonNull(header)) {
            byte[] decryptedBody = MMSCryptoManager.decrypt(msg.topic(), msg.value());
            try {
                // reflect
                Field valueField = msg.getClass().getDeclaredField("value");
                valueField.setAccessible(true);
                valueField.set(msg, decryptedBody);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                logger.error("消息解密错误", e);
            }
        }
    }

    private String getMqTagValue(ConsumerRecord<String, byte[]> msg) {
        Header header = msg.headers().lastHeader("mqTag");
        return Objects.isNull(header) ? null : new String(header.value());
    }

    private String getMqColorValue(ConsumerRecord<String, byte[]> msg) {
        Header header = msg.headers().lastHeader("mqColor");
        return Objects.isNull(header) ? null : new String(header.value());
    }

    private List<ConsumerRecord<String, byte[]>> filterMsg(List<ConsumerRecord<String, byte[]>> records) {
        return records.stream().filter((record) -> this.msgFilter(this.getMqTagValue(record))).filter((consumerRecord) -> this.msgFilterByColor(this.getMqColorValue(consumerRecord))).collect(Collectors.toList());
    }

    private String showBatchMsgInfo(List<ConsumerRecord<String, byte[]>> records) {
        StringBuilder sbf = new StringBuilder(200);
        records.forEach(record -> {
            sbf.append(",key:").append(record.key()).append(",partition:").append(record.partition()).append(",offset:").append(record.offset());
        });
        return sbf.toString().substring(1);
    }

    private KafkaLiteConsumerProxy.ConsumeMessageService createConsumeMessageService(TopicPartition topicPartition) {
        return this.sla.isOrderly ? new ConsumeMessageOrderlyService(topicPartition) : new ConsumerMessageConcurrentlyService(topicPartition);
    }

    class PartitionOperateContext {
        private final Set<TopicPartition> pausePartitions = new HashSet<>();
        private final Set<TopicPartition> resumePartitions = new HashSet<>();
        private final ReentrantLock pauseAndResumeLock = new ReentrantLock();

        public void addPausePartition(TopicPartition partition) {
            this.pauseAndResumeLock.lock();
            try {
                this.resumePartitions.remove(partition);
                this.pausePartitions.add(partition);
            } finally {
                this.pauseAndResumeLock.unlock();
            }
        }

        public void addResumePartition(TopicPartition partition) {
            this.pauseAndResumeLock.lock();
            try {
                this.pausePartitions.remove(partition);
                this.resumePartitions.add(partition);
            } finally {
                this.pauseAndResumeLock.unlock();
            }
        }

        public void pause() {
            this.pauseAndResumeLock.lock();
            try {
                Set<TopicPartition> assignment = KafkaLiteConsumerProxy.this.consumer.assignment();
                Set<TopicPartition> pausePartitionSet = this.pausePartitions.stream().filter(assignment::contains).collect(Collectors.toSet());
                if (MmsConsumerProxy.logger.isDebugEnabled()) {
                    MmsConsumerProxy.logger.debug("pause => partitions:{}", pausePartitionSet);
                }
                KafkaLiteConsumerProxy.this.consumer.pause(pausePartitionSet);
                this.pausePartitions.clear();
            } finally {
                this.pauseAndResumeLock.unlock();
            }
        }

        public void resume() {
            this.pauseAndResumeLock.lock();
            try {
                Set<TopicPartition> assignment = KafkaLiteConsumerProxy.this.consumer.assignment();
                Set<TopicPartition> resumePartitionSet = this.resumePartitions.stream().filter(assignment::contains).collect(Collectors.toSet());
                if (MmsConsumerProxy.logger.isDebugEnabled()) {
                    MmsConsumerProxy.logger.debug("resume => partitions:{}", resumePartitionSet);
                }
                KafkaLiteConsumerProxy.this.consumer.resume(resumePartitionSet);
                this.pausePartitions.clear();
            } finally {
                this.pauseAndResumeLock.unlock();
            }
        }
    }

    private class ConsumerMessageConcurrentlyService extends KafkaLiteConsumerProxy.AbstractConsumeMessageService {
        private AtomicInteger nextId = new AtomicInteger(0);
        private static final int reset = 2146483647;

        public ConsumerMessageConcurrentlyService(TopicPartition topicPartition) {
            super(topicPartition);
            this.start();
        }

        protected KafkaLiteConsumerProxy.AbstractConsumerRunner selectConsumerRunner(ConsumerRecord<String, byte[]> msg) {
            int next = this.nextId.incrementAndGet();
            if (next > reset) {
                this.nextId.set(0);
            }

            return KafkaLiteConsumerProxy.this.consumerRunners.get(Math.abs(next % KafkaLiteConsumerProxy.this.consumerRunners.size()));
        }

        public void start() {
            MmsConsumerProxy.logger.info("Partition[{}] starting consume concurrently.", this.topicPartition.partition());
        }

        public void stop() {
            this.started.compareAndSet(true, false);
            MmsConsumerProxy.logger.info("Partition[{}] stop consume concurrently.", this.topicPartition.partition());
        }
    }

    class OrderConsumerRunner extends KafkaLiteConsumerProxy.AbstractConsumerRunner {
        OrderConsumerRunner() {
            super();
        }

        public void doTask(List<ConsumerRecord<String, byte[]>> records) {
            try {
                if(CollectionUtils.isEmpty(records)) {
                    return;
                }
                List<ConsumerRecord<String, byte[]>> needConusmeList = KafkaLiteConsumerProxy.this.filterMsg(records);
                if (records.size() != needConusmeList.size()) {
                    List<ConsumerRecord<String, byte[]>> waitRemovedMsgs = records.stream().filter((item) -> !needConusmeList.contains(item)).collect(Collectors.toList());
                    this.removeMessageAndCommitOffset(waitRemovedMsgs);
                }
                if (needConusmeList.isEmpty()) {
                    return;
                }
                needConusmeList.forEach((recordx) -> {
                    try {
                        KafkaLiteConsumerProxy.this.decryptMsgBodyIfNecessary(recordx);
                    } catch (Throwable e) {
                        MmsConsumerProxy.logger.error("消息解密失败", e);
                        MmsConsumerProxy.logger.error("并发消费失败, recordInfo: record Key:{}, record partition:{},record offset:{}", recordx.key(), recordx.partition(), recordx.offset());
                        throw new RuntimeException(e);
                    }
                });
                if (!KafkaLiteConsumerProxy.this.listener.isEasy() && !(KafkaLiteConsumerProxy.this.listener instanceof KafkaMessageListener)) {
                    KafkaBatchMsgListener batchMsgListener = (KafkaBatchMsgListener)KafkaLiteConsumerProxy.this.listener;
                    long beginx = System.currentTimeMillis();
                    while(true) {
                        try {
                            MsgConsumedStatus statusx = batchMsgListener.onMessage(needConusmeList);
                            long duration;
                            if (statusx == MsgConsumedStatus.SUCCEED) {
                                duration = System.currentTimeMillis() - beginx;
                                KafkaLiteConsumerProxy.this.mmsMetrics.userCostTimeMs().update(duration, TimeUnit.MILLISECONDS);
                                KafkaLiteConsumerProxy.this.mmsMetrics.consumeSuccessRate().mark();
                                break;
                            }
                            duration = System.currentTimeMillis() - beginx;
                            KafkaLiteConsumerProxy.this.mmsMetrics.userCostTimeMs().update(duration, TimeUnit.MILLISECONDS);
                            KafkaLiteConsumerProxy.this.mmsMetrics.consumeFailureRate().mark();
                        } catch (Throwable e) {
                            MmsConsumerProxy.logger.error("顺序消费失败", e);
                            MmsConsumerProxy.logger.error("顺序消费失败, 待消费详情: {}", KafkaLiteConsumerProxy.this.showBatchMsgInfo(needConusmeList));
                        }
                    }
                    this.removeMessageAndCommitOffset(needConusmeList);
                } else {
                    KafkaMessageListener kafkaMessageListener = (KafkaMessageListener)KafkaLiteConsumerProxy.this.listener;
                    for (ConsumerRecord<String, byte[]> record : needConusmeList) {
                        long begin = System.currentTimeMillis();
                        if (KafkaLiteConsumerProxy.this.listener.isEasy()) {
                            ConsumeMessage consumeMessage = ConsumeMessage.parse(record);
                            while(true) {
                                try {
                                    MsgConsumedStatus statusxx = kafkaMessageListener.onMessage(consumeMessage);
                                    long durationxx;
                                    if (statusxx == MsgConsumedStatus.SUCCEED) {
                                        durationxx = System.currentTimeMillis() - begin;
                                        KafkaLiteConsumerProxy.this.mmsMetrics.userCostTimeMs().update(durationxx, TimeUnit.MILLISECONDS);
                                        KafkaLiteConsumerProxy.this.mmsMetrics.consumeSuccessRate().mark();
                                        break;
                                    }
                                    durationxx = System.currentTimeMillis() - begin;
                                    KafkaLiteConsumerProxy.this.mmsMetrics.userCostTimeMs().update(durationxx, TimeUnit.MILLISECONDS);
                                    KafkaLiteConsumerProxy.this.mmsMetrics.consumeFailureRate().mark();
                                } catch (Throwable e) {
                                    MmsConsumerProxy.logger.error("顺序消费失败, recordInfo: record key:{}, record partition:{},record offset:{}", new Object[]{record.key(), record.partition(), record.offset()});
                                    MmsConsumerProxy.logger.error("顺序消费失败", e);
                                }
                                TimeUnit.MILLISECONDS.sleep(2000L);
                            }
                            this.removeMessageAndCommitOffset(record);
                        } else {
                            while(true) {
                                try {
                                    MsgConsumedStatus status = kafkaMessageListener.onMessage(record);
                                    long durationx;
                                    if (status == MsgConsumedStatus.SUCCEED) {
                                        durationx = System.currentTimeMillis() - begin;
                                        KafkaLiteConsumerProxy.this.mmsMetrics.userCostTimeMs().update(durationx, TimeUnit.MILLISECONDS);
                                        KafkaLiteConsumerProxy.this.mmsMetrics.consumeSuccessRate().mark();
                                        break;
                                    }
                                    durationx = System.currentTimeMillis() - begin;
                                    KafkaLiteConsumerProxy.this.mmsMetrics.userCostTimeMs().update(durationx, TimeUnit.MILLISECONDS);
                                    KafkaLiteConsumerProxy.this.mmsMetrics.consumeFailureRate().mark();
                                } catch (Throwable e) {
                                    MmsConsumerProxy.logger.error("顺序消费失败", e);
                                    MmsConsumerProxy.logger.error("顺序消费失败, recordInfo: record key:{}, record partition:{},record offset:{}", new Object[]{record.key(), record.partition(), record.offset()});
                                }
                                TimeUnit.MILLISECONDS.sleep(5_000L);
                            }
                            this.removeMessageAndCommitOffset(record);
                        }
                    }
                }
            } catch (Throwable e) {
                MmsConsumerProxy.logger.error("consume message error", e);
            }
        }
    }

    class ConcurrentlyConsumerRunner extends KafkaLiteConsumerProxy.AbstractConsumerRunner {
        ConcurrentlyConsumerRunner() {
            super();
        }

        public void doTask(List<ConsumerRecord<String, byte[]>> consumerRecords) {
            try {
                if(CollectionUtils.isEmpty(consumerRecords)) {
                    return;
                }
                List<ConsumerRecord<String, byte[]>> needConusmeList = KafkaLiteConsumerProxy.this.filterMsg(consumerRecords);
                if (consumerRecords.size() != needConusmeList.size()) {
                    List<ConsumerRecord<String, byte[]>> waitRemovedMsgs = consumerRecords.stream().filter((item) -> !needConusmeList.contains(item)).collect(Collectors.toList());
                    this.removeMessageAndCommitOffset(waitRemovedMsgs);
                }
                if (needConusmeList.isEmpty()) {
                    return;
                }
                needConusmeList.forEach((recordx) -> {
                    try {
                        KafkaLiteConsumerProxy.this.decryptMsgBodyIfNecessary(recordx);
                    } catch (Throwable e) {
                        MmsConsumerProxy.logger.error("消息解密失败", e);
                        MmsConsumerProxy.logger.error("并发消费失败, msgInfo: msg key:{}, msg partition:{},msg offset:{}", new Object[]{recordx.key(), recordx.partition(), recordx.offset()});
                        throw new RuntimeException(e);
                    }
                });
                long beginx;
                if (KafkaLiteConsumerProxy.this.listener.isEasy() || KafkaLiteConsumerProxy.this.listener instanceof KafkaMessageListener) {
                    KafkaMessageListener kafkaMessageListener = (KafkaMessageListener)KafkaLiteConsumerProxy.this.listener;
                    for (ConsumerRecord<String, byte[]> record : needConusmeList) {
                        beginx = System.currentTimeMillis();
                        if (KafkaLiteConsumerProxy.this.listener.isEasy()) {
                            ConsumeMessage consumeMessage = null;
                            try {
                                consumeMessage = ConsumeMessage.parse(record);
                                kafkaMessageListener.onMessage(consumeMessage);
                            } catch (Throwable e) {
                                MmsConsumerProxy.logger.error("并发消费失败,将按重试策略进行重试", e);
                                MmsConsumerProxy.logger.error("并发消费失败, msginfo: msgKey:{}, msg queueid:{},msg offset:{}", record.key(), record.partition(), record.offset());
                            }
                            long durationx = System.currentTimeMillis() - beginx;
                            KafkaLiteConsumerProxy.this.mmsMetrics.userCostTimeMs().update(durationx, TimeUnit.MILLISECONDS);
                            this.removeMessageAndCommitOffset(record);
                        } else {
                            try {
                                kafkaMessageListener.onMessage(record);
                            } catch (Throwable e) {
                                MmsConsumerProxy.logger.error("并发消费失败，将按重试策略进行重试", e);
                                MmsConsumerProxy.logger.error("并发消费失败, msginfo: msgKey:{}, msg queueid:{},msg offset:{}", new Object[]{record.key(), record.partition(), record.offset()});
                            }
                            long duration = System.currentTimeMillis() - beginx;
                            KafkaLiteConsumerProxy.this.mmsMetrics.userCostTimeMs().update(duration, TimeUnit.MILLISECONDS);
                            this.removeMessageAndCommitOffset(record);
                        }
                    }
                } else {
                    KafkaBatchMsgListener kafkaBatchMsgListener = (KafkaBatchMsgListener)KafkaLiteConsumerProxy.this.listener;
                    long begin = System.currentTimeMillis();

                    try {
                        kafkaBatchMsgListener.onMessage(needConusmeList);
                    } catch (Throwable var11) {
                        MmsConsumerProxy.logger.error("并发消费失败，将按照重试策略进行重试", var11);
                        MmsConsumerProxy.logger.error("并发消费失败, 待消费详情: {}", KafkaLiteConsumerProxy.this.showBatchMsgInfo(needConusmeList));
                    }
                    beginx = System.currentTimeMillis() - begin;
                    KafkaLiteConsumerProxy.this.mmsMetrics.userCostTimeMs().update(beginx, TimeUnit.MILLISECONDS);
                    this.removeMessageAndCommitOffset(needConusmeList);
                }
            } catch (Throwable e) {
                MmsConsumerProxy.logger.error("consume message error", e);
            }
        }
    }

    abstract class AbstractConsumerRunner implements Runnable {
        protected volatile boolean isRunning = true;
        private BlockingQueue<ConsumerRecord<String, byte[]>> msgQueue = new LinkedBlockingQueue<>();

        public void putMessage(ConsumerRecord<String, byte[]> msg) {
            try {
                this.msgQueue.put(msg);
            } catch (InterruptedException e) {
                MmsConsumerProxy.logger.error("ignore interrupt ", e);
            }
        }

        public void run() {
            while(true) {
                try {
                    if (this.isRunning) {
                        try {
                            List<ConsumerRecord<String, byte[]>> msgs = new ArrayList<>(KafkaLiteConsumerProxy.this.consumeBatchSize);
                            while(this.msgQueue.drainTo(msgs, KafkaLiteConsumerProxy.this.consumeBatchSize) <= 0) {
                                Thread.sleep(20L);
                            }
                            this.doTask(msgs);
                            continue;
                        } catch (InterruptedException e) {
                            MmsConsumerProxy.logger.info("{} is Interrupt", Thread.currentThread().getName(), e);
                        } catch (Throwable e) {
                            MmsConsumerProxy.logger.error("consume message error, ", e);
                            continue;
                        }
                    }
                } catch (Throwable e) {
                    MmsConsumerProxy.logger.error("consume message error, ", e);
                }
                return;
            }
        }

        public void stop() {
            this.isRunning = false;
        }

        protected abstract void doTask(List<ConsumerRecord<String, byte[]>> consumerRecords);

        protected void removeMessageAndCommitOffset(List<ConsumerRecord<String, byte[]>> msgs) {
            Iterator var2 = msgs.iterator();

            while(var2.hasNext()) {
                ConsumerRecord<String, byte[]> msg = (ConsumerRecord)var2.next();
                this.removeMessageAndCommitOffset(msg);
            }

        }

        protected void removeMessageAndCommitOffset(ConsumerRecord<String, byte[]> msg) {
            TopicPartition topicPartition = new TopicPartition(msg.topic(), msg.partition());
            KafkaLiteConsumerProxy.AbstractConsumeMessageService consumeMessageService = (KafkaLiteConsumerProxy.AbstractConsumeMessageService)KafkaLiteConsumerProxy.this.getOrCreateConsumeMessageService(topicPartition);
            long offset = consumeMessageService.removeMessage(msg);
            KafkaLiteConsumerProxy.this.addOffset(msg, offset);
            consumeMessageService.maybeNeedResume();
        }
    }

    private class ConsumeMessageOrderlyService extends KafkaLiteConsumerProxy.AbstractConsumeMessageService {
        private final int NO_KEY_HASH = "__nokey".hashCode();

        public ConsumeMessageOrderlyService(TopicPartition topicPartition) {
            super(topicPartition);
            this.start();
        }

        protected KafkaLiteConsumerProxy.AbstractConsumerRunner selectConsumerRunner(ConsumerRecord<String, byte[]> msg) {
            return (KafkaLiteConsumerProxy.AbstractConsumerRunner)KafkaLiteConsumerProxy.this.consumerRunners.get(Math.abs(this.getHashCode(msg) % KafkaLiteConsumerProxy.this.consumerRunners.size()));
        }

        public void start() {
            MmsConsumerProxy.logger.info("partition[{}] starting consume orderly.", this.topicPartition.partition());
        }

        public void stop() {
            this.started.compareAndSet(true, false);
            MmsConsumerProxy.logger.info("TopicPartition[{}] stopped consume orderly.", this.topicPartition);
        }

        private final int getHashCode(ConsumerRecord<String, byte[]> record) {
            String key = (String)record.key();
            if (StringUtils.isEmpty(key)) {
                MmsConsumerProxy.logger.error("顺序消费没有设置key,将采用默认key，请及时优化");
                return this.NO_KEY_HASH;
            } else {
                return key.hashCode();
            }
        }
    }

    private abstract class AbstractConsumeMessageService implements KafkaLiteConsumerProxy.ConsumeMessageService {
        protected TopicPartition topicPartition;
        protected final AtomicBoolean started = new AtomicBoolean(false);
        protected ReentrantReadWriteLock msgTreeMapLock = new ReentrantReadWriteLock();
        protected ReentrantReadWriteLock.ReadLock msgTreeMapReadLock;
        protected ReentrantReadWriteLock.WriteLock msgTreeMapWriteLock;
        protected final TreeMap<Long, ConsumerRecord<String, byte[]>> msgTreeMap;

        public AbstractConsumeMessageService(TopicPartition topicPartition) {
            this.msgTreeMapReadLock = this.msgTreeMapLock.readLock();
            this.msgTreeMapWriteLock = this.msgTreeMapLock.writeLock();
            this.msgTreeMap = new TreeMap();
            this.topicPartition = topicPartition;
        }

        public void execute(List<ConsumerRecord<String, byte[]>> consumerRecords) {
            if(CollectionUtils.isEmpty(consumerRecords)) {
                return;
            }
            this.putMessage(consumerRecords);
            if (this.isNeedPause()) {
                KafkaLiteConsumerProxy.this.partitionOperateContext.addPausePartition(this.topicPartition);
            }
            consumerRecords.forEach(msg->{
                KafkaLiteConsumerProxy.AbstractConsumerRunner consumerRunner = this.selectConsumerRunner(msg);
                consumerRunner.putMessage(msg);
            });
        }

        protected abstract KafkaLiteConsumerProxy.AbstractConsumerRunner selectConsumerRunner(ConsumerRecord<String, byte[]> msg);

        protected Long removeMessage(List<ConsumerRecord<String, byte[]>> records) {
            long result = -1L;
            try {
                this.msgTreeMapWriteLock.lock();
                if(MapUtils.isNotEmpty(this.msgTreeMap)) {
                    records.forEach(record-> this.msgTreeMap.remove(record.offset()));
                    if(MapUtils.isNotEmpty(this.msgTreeMap)) {
                        result = this.msgTreeMap.firstKey();
                    }
                }
            } finally {
                this.msgTreeMapWriteLock.unlock();
            }
            return result;
        }

        protected Long removeMessage(ConsumerRecord<String, byte[]> record) {
            long result = -1L;
            try {
                this.msgTreeMapWriteLock.lock();
                if(MapUtils.isNotEmpty(this.msgTreeMap)) {
                    this.msgTreeMap.remove(record.offset());
                    if(MapUtils.isNotEmpty(this.msgTreeMap)) result = this.msgTreeMap.firstKey();
                }
            } finally {
                this.msgTreeMapWriteLock.unlock();
            }
            return result;
        }

        protected void putMessage(List<ConsumerRecord<String, byte[]>> records) {
            try {
                this.msgTreeMapWriteLock.lock();
                records.forEach((record) -> this.msgTreeMap.put(record.offset(), record));
            } finally {
                this.msgTreeMapWriteLock.unlock();
            }
        }

        protected boolean isNeedPause() {
            this.msgTreeMapReadLock.lock();
            try {
                if(MapUtils.isEmpty(this.msgTreeMap)) {
                    return false;
                }
                return this.msgTreeMap.size() > KafkaLiteConsumerProxy.this.orderlyPartitionMaxConsumeRecords
                    || this.msgTreeMap.lastKey() - this.msgTreeMap.firstKey() > (long)KafkaLiteConsumerProxy.this.orderlyPartitionMaxConsumeRecords;
            } finally {
                this.msgTreeMapReadLock.unlock();
            }
        }

        protected boolean isNeedResume() {
            this.msgTreeMapReadLock.lock();
            try {
                if(MapUtils.isEmpty(this.msgTreeMap)) {
                    return true;
                }
                if (this.msgTreeMap.size() >= KafkaLiteConsumerProxy.this.orderlyPartitionMaxConsumeRecords / 2) {
                    return false;
                }
            } finally {
                this.msgTreeMapReadLock.unlock();
            }
            return true;
        }

        protected void maybeNeedResume() {
            if (this.isNeedResume()) {
                KafkaLiteConsumerProxy.this.partitionOperateContext.addResumePartition(this.topicPartition);
            }
        }
    }

    interface ConsumeMessageService {
        void execute(List<ConsumerRecord<String, byte[]>> consumerRecords);

        void start();

        void stop();
    }

    private static final class LoggingCommitCallback implements OffsetCommitCallback {
        private static final Logger logger = LoggerFactory.getLogger(LoggingCommitCallback.class);

        public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception e) {
            if (Objects.nonNull(e)) {
                logger.error("Commit failed for {}", offsets, e);
            } else if (logger.isDebugEnabled()) {
                logger.debug("Commits for {} completed", offsets);
            }
        }
    }
}
