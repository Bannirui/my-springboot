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
import com.github.bannirui.msb.mq.sdk.utils.ListUtil;
import com.github.bannirui.msb.mq.sdk.utils.Utils;
import com.google.common.collect.Lists;
import java.lang.reflect.Field;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
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

public class KafkaConsumerProxy extends MmsConsumerProxy<ConsumerRecord<String, byte[]>> {
    private final int ackRecords = Integer.parseInt(System.getProperty("ack.records", "100000"));
    private final int consumerPollTimeoutMs = Integer.parseInt(System.getProperty("consumer.poll.timeout.ms", "3000"));
    private final int concurrentlyThreadPoolQueueSize = Integer.parseInt(System.getProperty("concurrently.threadpool.queue.size", "10000"));
    private final int ackPartitionRecords = Integer.parseInt(System.getProperty("ack.partition.records", "10000"));
    private final int orderlyPartitionConsumeQueueSize = Integer.parseInt(System.getProperty("orderly.partition.consume.queue.size", "10000"));
    private final int orderlyPartitionMaxConsumeRecords = Integer.parseInt(System.getProperty("orderly.partition.max.consume.records", "2000"));
    private final int orderlyPartitionMaxPollRecords = Integer.parseInt(System.getProperty("orderly.partition.max.poll.records", "500"));
    KafkaConsumer<String, byte[]> consumer;
    List<ThreadPoolExecutor> executors = Lists.newArrayList();
    private final Properties kafkaProperties = new Properties();
    private final Map<String, Map<Integer, Long>> offsets = new HashMap<>();
    private final BlockingQueue<ConsumerRecord<String, byte[]>> acks;
    private final Map<Integer, BlockingQueue<ConsumerRecord<String, byte[]>>> acksMap;
    private final Map<Integer, KafkaConsumerProxy.ConsumeMessageService> consumeMessageServiceTable;
    private final KafkaConsumerProxy.PartitionOperateContext partitionOperateContext;
    private final BlockingQueue<TopicPartitionInitialOffset> seeks;
    private int consumeBatchSize;

    private void addOffset(ConsumerRecord<String, byte[]> record) {
        (this.offsets.computeIfAbsent(record.topic(), (v) -> new ConcurrentHashMap<>())).compute(record.partition(), (k, v) -> v == null ? record.offset() : Math.max(v, record.offset()));
    }

    public KafkaConsumerProxy(MmsMetadata metadata, SLA sla, String instanceName, Properties properties, MessageListener listener) {
        super(metadata, sla, instanceName, properties, listener);
        this.acks = new LinkedBlockingQueue<>(this.ackRecords);
        this.acksMap = new ConcurrentHashMap<>();
        this.consumeMessageServiceTable = new ConcurrentHashMap<>();
        this.partitionOperateContext = new KafkaConsumerProxy.PartitionOperateContext();
        this.seeks = new LinkedBlockingQueue<>();
        this.consumeBatchSize = 1;
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
        this.kafkaProperties.put("client.id", this.metadata.getName() + "--" + MmsEnv.MMS_IP + "--" + ThreadLocalRandom.current().nextInt(100000));
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
        this.consumer = new KafkaConsumer(this.kafkaProperties);
        this.consumer.subscribe(Lists.newArrayList(((ConsumerGroupMetadata)this.metadata).getBindingTopic()), new ConsumerRebalanceListener() {
            public void onPartitionsRevoked(Collection<TopicPartition> collection) {
                MmsConsumerProxy.logger.info("partition revoked for {} at {}", KafkaConsumerProxy.this.metadata.getName(), LocalDateTime.now());
                if (KafkaConsumerProxy.this.sla.isOrderly()) {
                    KafkaConsumerProxy.this.consumeMessageServiceTable.forEach((partition, consumeMessageService) -> {
                        MmsConsumerProxy.logger.info("stopping partition[{}] orderly consume.", partition);
                        consumeMessageService.stop();
                        KafkaConsumerProxy.this.commitOffsets(partition);
                    });
                    KafkaConsumerProxy.this.consumeMessageServiceTable.clear();
                } else {
                    KafkaConsumerProxy.this.commitOffsets();
                }
            }
            public void onPartitionsAssigned(Collection<TopicPartition> collection) {
                MmsConsumerProxy.logger.info("partition assigned for {} at {}", KafkaConsumerProxy.this.metadata.getName(), LocalDateTime.now());
                MmsConsumerProxy.logger.info("partition assigned " + StringUtils.joinWith(",", collection));
                collection.forEach(partition -> {
                    OffsetAndMetadata offset = KafkaConsumerProxy.this.consumer.committed(partition);
                    if (ConsumeFrom.LATEST.getName().equalsIgnoreCase(String.valueOf(KafkaConsumerProxy.this.kafkaProperties.get("auto.offset.reset"))) && offset == null) {
                        KafkaConsumerProxy.this.consumer.seek(partition, 0L);
                    }
                });
                KafkaConsumerProxy.this.initConsumeMessageOrderlyService(collection);
            }
        });
    }

    private void initConsumeMessageOrderlyService(Collection<TopicPartition> topicPartitions) {
        if (this.sla.isOrderly()) {
            String[] topics = this.consumer.subscription().toArray(new String[0]);
            int orderlyConsumePartitionParallelism = 1;
            if (this.customizedProperties.containsKey(MmsClientConfig.CONSUMER.ORDERLY_CONSUME_PARTITION_PARALLELISM.getKey())) {
                orderlyConsumePartitionParallelism = Integer.parseInt(String.valueOf(this.customizedProperties.get(MmsClientConfig.CONSUMER.ORDERLY_CONSUME_PARTITION_PARALLELISM.getKey())));
            }
            for (TopicPartition partition : topicPartitions) {
                List<ExecutorService> singleExecutors = new ArrayList<>();
                AtomicInteger index = new AtomicInteger(0);
                for(int i = 0; i < orderlyConsumePartitionParallelism; ++i) {
                    singleExecutors.add(Executors.newSingleThreadExecutor((runnable) -> new Thread(runnable, "MmsKafkaOrderlyConsumeThread_" + this.metadata.getName() + "_partition_" + partition.partition() + "_" + index.incrementAndGet())));
                }
                this.consumeMessageServiceTable.computeIfAbsent(partition.partition(), (p) -> new ConsumeMessageOrderlyService(singleExecutors, topics[0], p));
            }
        }
    }

    private String buildConsumerInfo(Properties properties) {
        StringBuilder stringBuilder = new StringBuilder();
        properties.forEach((k,v)->{
            stringBuilder.append(k).append(": ").append(v);
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
                            ConsumerRecords<String, byte[]> records = this.consumer.poll(Duration.ofMillis(this.consumerPollTimeoutMs));
                            if (logger.isDebugEnabled()) {
                                logger.debug("messaged pulled at {} for topic {} ", System.currentTimeMillis(), ((ConsumerGroupMetadata)this.metadata).getBindingTopic());
                            }
                            if (listener instanceof KafkaBatchMsgListener) {
                                this.submitBatchRecords(records, (KafkaBatchMsgListener)listener);
                            } else {
                                this.submitRecords(records, listener);
                            }
                            if (this.sla.isOrderly()) {
                                this.partitionOperateContext.commitOffsets();
                                this.partitionOperateContext.pause();
                                this.partitionOperateContext.resume();
                                continue;
                            }
                            this.commitOffsets();
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

    private synchronized void commitOffsets() {
        this.handleAcks();
        Map<TopicPartition, OffsetAndMetadata> commits = this.buildCommits();
        if (!commits.isEmpty()) {
            this.consumer.commitAsync(commits, new KafkaConsumerProxy.LoggingCommitCallback());
        }
    }

    private void handleAcks() {
        for(ConsumerRecord<String, byte[]> record = this.acks.poll(); record != null; record = this.acks.poll()) {
            this.addOffset(record);
        }
    }

    private synchronized void commitOffsets(Integer partition) {
        this.handleAcks(partition);
        Map<TopicPartition, OffsetAndMetadata> commits = this.buildCommits(partition);
        if (!commits.isEmpty()) {
            this.consumer.commitAsync(commits, new KafkaConsumerProxy.LoggingCommitCallback());
        }
    }

    private void handleAcks(Integer partition) {
        if (this.acksMap.containsKey(partition)) {
            BlockingQueue<ConsumerRecord<String, byte[]>> bq = this.acksMap.get(partition);
            for(ConsumerRecord<String, byte[]> record = bq.poll(); record != null; record = bq.poll()) {
                this.addOffset(record);
            }
        }
    }

    private Map<TopicPartition, Long> getHighestTopicPartitionOffset(List<ConsumerRecord<String, byte[]>> records) {
        Map<TopicPartition, Long> highestOffsetMap = new HashMap<>();
        records.forEach((r) -> {
            highestOffsetMap.compute(new TopicPartition(r.topic(), r.partition()), (k, v) ->
                v == null ? r.offset() : (r.offset() > v ? r.offset() : v));
        });
        return highestOffsetMap;
    }

    private Collection<ConsumerRecord<String, byte[]>> getHighestTopicOffset(List<ConsumerRecord<String, byte[]>> records) {
        Map<TopicPartition, ConsumerRecord<String, byte[]>> highestOffsetMap = new HashMap<>();
        records.forEach((r) -> {
            highestOffsetMap.compute(new TopicPartition(r.topic(), r.partition()), (k, v) -> v == null ? r : (r.offset() > v.offset() ? r : v));
        });
        return highestOffsetMap.values();
    }

    private synchronized Map<TopicPartition, OffsetAndMetadata> buildCommits() {
        Map<TopicPartition, OffsetAndMetadata> commits = new HashMap<>();
        this.offsets.forEach((k, v) -> v.forEach((key, val)-> commits.put(new TopicPartition(String.valueOf(key), key), new OffsetAndMetadata(val + 1L))));
        this.offsets.clear();
        return commits;
    }

    private synchronized Map<TopicPartition, OffsetAndMetadata> buildCommits(Integer partition) {
        Map<TopicPartition, OffsetAndMetadata> commits = new HashMap<>();
        this.offsets.forEach((k,v)-> v.forEach((key, val)->{
            if (Objects.equals(partition, key)) {
                commits.put(new TopicPartition(String.valueOf(key), key), new OffsetAndMetadata(val + 1L));
            }
        }));
        this.offsets.clear();
        return commits;
    }

    private void submitBatchRecords(ConsumerRecords<String, byte[]> records, final KafkaBatchMsgListener listener) {
        if (!records.isEmpty()) {
            Iterable<ConsumerRecord<String, byte[]>> recordsIter = records.records(((ConsumerGroupMetadata)this.metadata).getBindingTopic());
            ArrayList<ConsumerRecord<String, byte[]>> consumerRecords = Lists.newArrayList(recordsIter);
            if (this.sla.isOrderly()) {
                Map<Integer, List<ConsumerRecord<String, byte[]>>> consumerRecordsMap =
                    consumerRecords.stream().collect(Collectors.groupingBy(ConsumerRecord::partition));
                consumerRecordsMap.forEach((partition, consumerRecord) -> {
                    this.consumeMessageServiceTable.get(partition).execute(consumerRecord);
                });
            } else {
                List<List<ConsumerRecord<String, byte[]>>> subList = ListUtil.subList(consumerRecords, this.consumeBatchSize);
                CountDownLatch countDownLatch = new CountDownLatch(subList.size());
                for (List<ConsumerRecord<String, byte[]>> recordList : subList) {
                    List<ConsumerRecord<String, byte[]>> needConusmeLst = recordList.stream().filter((record) -> this.msgFilter(this.getMqTagValue(record))).filter((record) -> this.msgFilterByColor(this.getMqColorValue(record))).collect(Collectors.toList());
                    needConusmeLst.forEach(this::decryptMsgBodyIfNecessary);
                    Runnable task = () -> {
                        boolean succ = false;
                        label162: {
                            try {
                                succ = true;
                                long begin = System.currentTimeMillis();
                                listener.onMessage(needConusmeLst);
                                long duration = System.currentTimeMillis() - begin;
                                this.mmsMetrics.userCostTimeMs().update(duration, TimeUnit.MILLISECONDS);
                                succ = false;
                                break label162;
                            } catch (Throwable e) {
                                logger.error("consume message error", e);
                                succ = false;
                            } finally {
                                if (succ) {
                                    int ix = 0;
                                    while(true) {
                                        if (ix >= consumerRecords.size()) {
                                            countDownLatch.countDown();
                                        } else {
                                            ConsumerRecord<String, byte[]> consumerRecord = consumerRecords.get(ix);
                                            for(boolean offer = this.acks.offer(consumerRecord); !offer; offer = this.acks.offer(consumerRecord)) {
                                                logger.info("add consumer record to acks full and trigger commit offsets at {}", LocalDateTime.now());
                                                this.commitOffsets();
                                            }
                                            ++ix;
                                        }
                                    }
                                }
                            }
                            for(int i = 0; i < consumerRecords.size(); ++i) {
                                ConsumerRecord<String, byte[]> consumerRecordx = consumerRecords.get(i);
                                for(boolean offerx = this.acks.offer(consumerRecordx); !offerx; offerx = this.acks.offer(consumerRecordx)) {
                                    logger.info("add consumer record to acks full and trigger commit offsets at {}", LocalDateTime.now());
                                    this.commitOffsets();
                                }
                            }
                            countDownLatch.countDown();
                            return;
                        }
                        for(int i = 0; i < consumerRecords.size(); ++i) {
                            ConsumerRecord<String, byte[]> consumerRecordx = consumerRecords.get(i);
                            for(boolean offerx = this.acks.offer(consumerRecordx); !offerx; offerx = this.acks.offer(consumerRecordx)) {
                                logger.info("add consumer record to acks full and trigger commit offsets at {}", LocalDateTime.now());
                                this.commitOffsets();
                            }
                        }
                        countDownLatch.countDown();
                    };
                    boolean done = false;
                    while(!done) {
                        try {
                            ThreadPoolExecutor executor = this.executors.get(0);
                            executor.execute(task);
                            done = true;
                        } catch (RejectedExecutionException e) {
                            logger.error("consume slow, wait for moment");
                            try {
                                Thread.sleep(50L);
                            } catch (InterruptedException ex) {
                                logger.error("interupted when consume slow");
                            }
                        }
                    }
                }
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    logger.error("wait for kafka consumer latch interupted", e);
                }
            }
        }
    }

    private void submitRecords(ConsumerRecords<String, byte[]> records, final MessageListener listener) {
        if (!records.isEmpty()) {
            Iterable<ConsumerRecord<String, byte[]>> recordsIter = records.records(((ConsumerGroupMetadata)this.metadata).getBindingTopic());
            CountDownLatch countDownLatch = new CountDownLatch(records.count());
            ArrayList<ConsumerRecord<String, byte[]>> consumerRecords = Lists.newArrayList(recordsIter);
            if (this.sla.isOrderly()) {
                Map<Integer, List<ConsumerRecord<String, byte[]>>> consumerRecordsMap = consumerRecords.stream().collect(Collectors.groupingBy(ConsumerRecord::partition));
                consumerRecordsMap.forEach((partition, consumerRecordx) -> {
                    this.consumeMessageServiceTable.get(partition).execute(consumerRecordx);
                });
            } else {
                for(int i = 0; i < consumerRecords.size(); ++i) {
                    ConsumerRecord<String, byte[]> consumerRecord = consumerRecords.get(i);
                    if (logger.isDebugEnabled()) {
                        logger.debug("consumerRecord submitted topic {} partition {} offset {}", consumerRecord.topic(), consumerRecord.partition(),
                            consumerRecord.offset());
                    }
                    Runnable task = () -> {
                        boolean var13 = false;
                        boolean offer;
                        label188: {
                            label196: {
                                label197: {
                                    try {
                                        var13 = true;
                                        if (!this.msgFilter(this.getMqTagValue(consumerRecord))) {
                                            var13 = false;
                                            break label188;
                                        }
                                        if (!this.msgFilterByColor(this.getMqColorValue(consumerRecord))) {
                                            var13 = false;
                                            break label196;
                                        }
                                        this.decryptMsgBodyIfNecessary(consumerRecord);
                                        long begin = System.currentTimeMillis();
                                        MsgConsumedStatus msgConsumedStatus = MsgConsumedStatus.SUCCEED;
                                        if (listener.isEasy()) {
                                            ConsumeMessage consumeMessage = ConsumeMessage.parse(consumerRecord);
                                            msgConsumedStatus = listener.onMessage(consumeMessage);
                                        } else {
                                            KafkaMessageListener l = (KafkaMessageListener)listener;
                                            msgConsumedStatus = l.onMessage(consumerRecord);
                                        }
                                        long duration = System.currentTimeMillis() - begin;
                                        this.mmsMetrics.userCostTimeMs().update(duration, TimeUnit.MILLISECONDS);
                                        if (logger.isDebugEnabled()) {
                                            logger.debug("consumerRecord  topic {} partition {} offset {} consumed {}", consumerRecord.topic(),
                                                consumerRecord.partition(), consumerRecord.offset(), msgConsumedStatus.name());
                                            var13 = false;
                                        } else {
                                            var13 = false;
                                        }
                                        break label197;
                                    } catch (Throwable e) {
                                        logger.error("consume message error", e);
                                        var13 = false;
                                    } finally {
                                        if (var13) {
                                            for(boolean offerx = this.acks.offer(consumerRecord); !offerx; offerx = this.acks.offer(consumerRecord)) {
                                                logger.info("add consumer record to acks full and trigger commit offsets at {}", LocalDateTime.now());
                                                this.commitOffsets();
                                            }
                                            countDownLatch.countDown();
                                        }
                                    }

                                    for(offer = this.acks.offer(consumerRecord); !offer; offer = this.acks.offer(consumerRecord)) {
                                        logger.info("add consumer record to acks full and trigger commit offsets at {}", LocalDateTime.now());
                                        this.commitOffsets();
                                    }

                                    countDownLatch.countDown();
                                    return;
                                }

                                for(offer = this.acks.offer(consumerRecord); !offer; offer = this.acks.offer(consumerRecord)) {
                                    logger.info("add consumer record to acks full and trigger commit offsets at {}", LocalDateTime.now());
                                    this.commitOffsets();
                                }

                                countDownLatch.countDown();
                                return;
                            }

                            for(offer = this.acks.offer(consumerRecord); !offer; offer = this.acks.offer(consumerRecord)) {
                                logger.info("add consumer record to acks full and trigger commit offsets at {}", LocalDateTime.now());
                                this.commitOffsets();
                            }

                            countDownLatch.countDown();
                            return;
                        }

                        for(offer = this.acks.offer(consumerRecord); !offer; offer = this.acks.offer(consumerRecord)) {
                            logger.info("add consumer record to acks full and trigger commit offsets at {}", LocalDateTime.now());
                            this.commitOffsets();
                        }

                        countDownLatch.countDown();
                    };
                    boolean done = false;

                    while(!done) {
                        try {
                            ThreadPoolExecutor executor = this.executors.get(consumerRecord.partition() % this.executors.size());
                            executor.execute(task);
                            done = true;
                        } catch (RejectedExecutionException e) {
                            logger.error("consume slow, wait for moment");
                            try {
                                Thread.sleep(50L);
                            } catch (InterruptedException var13) {
                                logger.error("interupted when consume slow");
                            }
                        }
                    }
                }

                try {
                    countDownLatch.await();
                } catch (InterruptedException var12) {
                    logger.error("wait for kafka consumer latch interupted", var12);
                }
            }
        }

    }

    protected void consumerShutdown() {
        try {
            this.consumer.close();
        } catch (ConcurrentModificationException e) {
            logger.info("consumer shutdown changes to wakeup for: {}", e.getMessage());
            this.consumer.wakeup();
        }

        Iterator var1 = this.executors.iterator();

        while(var1.hasNext()) {
            ThreadPoolExecutor executor = (ThreadPoolExecutor)var1.next();
            Utils.gracefullyShutdown(executor);
        }
        this.executors.clear();
    }

    public void statistics() {
        super.statistics();
        KafkaConsumerStatusReporter.getInstance().reportConsumerStatus();
    }

    public void addUserDefinedProperties(Properties properties) {
        this.kafkaProperties.put("max.poll.records", properties.get(MmsClientConfig.CONSUMER.CONSUME_MESSAGES_SIZE.getKey()));
        if (properties.containsKey(MmsClientConfig.CONSUMER.CONSUME_TIMEOUT_MS.getKey())) {
            this.kafkaProperties.put("max.poll.interval.ms", properties.get(MmsClientConfig.CONSUMER.CONSUME_TIMEOUT_MS.getKey()));
        }
        if (properties.containsKey(MmsClientConfig.CONSUMER.CONSUME_BATCH_SIZE.getKey())) {
            this.consumeBatchSize = Integer.parseInt(String.valueOf(properties.get(MmsClientConfig.CONSUMER.CONSUME_BATCH_SIZE.getKey())));
        }
        int threadsNumMin;
        if (properties.containsKey(MmsClientConfig.CONSUMER.CONSUME_THREAD_MIN.getKey())) {
            threadsNumMin = Integer.parseInt(String.valueOf(properties.get(MmsClientConfig.CONSUMER.CONSUME_THREAD_MIN.getKey())));
        } else {
            threadsNumMin = Runtime.getRuntime().availableProcessors();
        }
        int threadsNumMax;
        if (properties.containsKey(MmsClientConfig.CONSUMER.CONSUME_THREAD_MAX.getKey())) {
            threadsNumMax = Integer.parseInt(String.valueOf(properties.get(MmsClientConfig.CONSUMER.CONSUME_THREAD_MAX.getKey())));
        } else {
            threadsNumMax = Math.max(Runtime.getRuntime().availableProcessors() * 2, threadsNumMin);
        }
        logger.info("kafka consumer thread set to min: " + threadsNumMin + " max: " + threadsNumMax);
        if (!this.sla.isOrderly()) {
            BlockingQueue blockingQueue = new ArrayBlockingQueue<>(this.concurrentlyThreadPoolQueueSize);
            ThreadPoolExecutor.AbortPolicy policy = new ThreadPoolExecutor.AbortPolicy();
            ThreadFactory threadFactory = new ThreadFactory() {
                final AtomicInteger index = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "MmsKafkaConcurrentlyConsumeThread_" + KafkaConsumerProxy.this.metadata.getName() + "_" + this.index.incrementAndGet());
                }
            };
            ThreadPoolExecutor executor = new ThreadPoolExecutor(threadsNumMin, threadsNumMax, 1000L, TimeUnit.MILLISECONDS, blockingQueue, threadFactory, policy);
            this.executors.add(executor);
        }
    }

    protected void decryptMsgBodyIfNecessary(ConsumerRecord<String, byte[]> msg) {
        Headers headers = msg.headers();
        Header header = headers.lastHeader("encrypt_mark");
        if (header != null) {
            byte[] decryptedBody = MMSCryptoManager.decrypt(msg.topic(), msg.value());
            try {
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
        return header == null ? null : new String(header.value());
    }

    private String getMqColorValue(ConsumerRecord<String, byte[]> msg) {
        Header header = msg.headers().lastHeader("mqColor");
        return header == null ? null : new String(header.value());
    }

    class PartitionOperateContext {
        private final Set<TopicPartition> commitOffsetPartitions = new HashSet<>();
        private final Set<TopicPartition> pausePartitions = new HashSet<>();
        private final Set<TopicPartition> resumePartitions = new HashSet<>();
        private final ReentrantLock commitOffsetLock = new ReentrantLock();
        private final ReentrantLock pauseAndResumeLock = new ReentrantLock();

        public void addCommitOffsetPartition(TopicPartition partition) {
            this.commitOffsetLock.lock();
            try {
                this.commitOffsetPartitions.add(partition);
            } finally {
                this.commitOffsetLock.unlock();
            }
        }

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

        public void commitOffsets() {
            this.commitOffsetLock.lock();
            try {
                Set<TopicPartition> assignment = KafkaConsumerProxy.this.consumer.assignment();
                Set<Integer> commitOffsetPartitionSet = commitOffsetPartitions.stream().filter(assignment::contains).map(TopicPartition::partition).collect(Collectors.toSet());
                if (MmsConsumerProxy.logger.isDebugEnabled()) {
                    MmsConsumerProxy.logger.debug("commitOffsets => partitions:{}", commitOffsetPartitionSet);
                }
                commitOffsetPartitionSet.forEach(KafkaConsumerProxy.this::commitOffsets);
                this.commitOffsetPartitions.clear();
            } finally {
                this.commitOffsetLock.unlock();
            }
        }

        public void pause() {
            this.pauseAndResumeLock.lock();
            try {
                Set<TopicPartition> assignment = KafkaConsumerProxy.this.consumer.assignment();
                Set<TopicPartition> pausePartitionSet = this.pausePartitions.stream().filter(assignment::contains).collect(Collectors.toSet());
                if (MmsConsumerProxy.logger.isDebugEnabled()) {
                    MmsConsumerProxy.logger.debug("pause => partitions:{}", pausePartitionSet);
                }
                KafkaConsumerProxy.this.consumer.pause(pausePartitionSet);
                this.pausePartitions.clear();
            } finally {
                this.pauseAndResumeLock.unlock();
            }
        }

        public void resume() {
            this.pauseAndResumeLock.lock();
            try {
                Set<TopicPartition> assignment = KafkaConsumerProxy.this.consumer.assignment();
                Set<TopicPartition> resumePartitionSet = resumePartitions.stream().filter(assignment::contains).collect(Collectors.toSet());
                if (MmsConsumerProxy.logger.isDebugEnabled()) {
                    MmsConsumerProxy.logger.debug("resume => partitions:{}", resumePartitionSet);
                }
                KafkaConsumerProxy.this.consumer.resume(resumePartitionSet);
                this.pausePartitions.clear();
            } finally {
                this.pauseAndResumeLock.unlock();
            }
        }
    }

    private class ConsumeMessageOrderlyService implements KafkaConsumerProxy.ConsumeMessageService {
        private final Integer partition;
        private final TopicPartition topicPartition;
        private final List<ExecutorService> executors;
        private final BlockingQueue<ConsumerRecord<String, byte[]>> msgConsumeQueue;
        private final AtomicBoolean started;

        public ConsumeMessageOrderlyService(List<ExecutorService> executors, String topic, Integer partition) {
            this.msgConsumeQueue = new ArrayBlockingQueue<>(KafkaConsumerProxy.this.orderlyPartitionConsumeQueueSize);
            this.started = new AtomicBoolean(false);
            this.executors = executors;
            this.partition = partition;
            this.topicPartition = new TopicPartition(topic, partition);
            this.started.compareAndSet(false, true);
            this.start();
        }

        public void execute(List<ConsumerRecord<String, byte[]>> consumerRecords) {
            if(!this.isStarted()) return;
            for (ConsumerRecord<String, byte[]> consumerRecord : consumerRecords) {
                while(!this.msgConsumeQueue.offer(consumerRecord)) {
                    MmsConsumerProxy.logger.warn("offer consumer record to msgConsumeQueue full {}, topicPartition:{}", LocalDateTime.now(), this.topicPartition);
                    try {
                        TimeUnit.MILLISECONDS.sleep(50L);
                    } catch (InterruptedException e) {
                        MmsConsumerProxy.logger.error("interrupted when offer consumerRecord to msgConsumeQueue.");
                    }
                }
            }
            if (this.msgConsumeQueue.size() > KafkaConsumerProxy.this.orderlyPartitionMaxConsumeRecords) {
                KafkaConsumerProxy.this.partitionOperateContext.addPausePartition(this.topicPartition);
            }
        }

        public void start() {
            MmsConsumerProxy.logger.info("Partition[{}] starting consume orderly.", this.topicPartition);
            (new Thread(() -> {
                while(this.isStarted()) {
                    if (this.msgConsumeQueue.isEmpty()) {
                        KafkaConsumerProxy.this.partitionOperateContext.addResumePartition(this.topicPartition);
                        try {
                            TimeUnit.MILLISECONDS.sleep(100L);
                        } catch (InterruptedException e) {
                            MmsConsumerProxy.logger.error("interrupted when add resume partition");
                        }
                    } else {
                        ArrayList<ConsumerRecord<String, byte[]>> consumerRecords = new ArrayList(KafkaConsumerProxy.this.orderlyPartitionMaxPollRecords);
                        this.msgConsumeQueue.drainTo(consumerRecords, KafkaConsumerProxy.this.orderlyPartitionMaxPollRecords);
                        CountDownLatch countDownLatch = new CountDownLatch(consumerRecords.size());
                        Iterator var3 = consumerRecords.iterator();
                        while(var3.hasNext()) {
                            ConsumerRecord<String, byte[]> consumerRecord = (ConsumerRecord)var3.next();
                            if (MmsConsumerProxy.logger.isDebugEnabled()) {
                                MmsConsumerProxy.logger.debug("consumerRecord submitted topic {} partition {} offset {}", consumerRecord.topic(),
                                    consumerRecord.partition(), consumerRecord.offset());
                            }
                            Runnable task = () -> {
                                boolean succ = false;
                                BlockingQueue bq;
                                label249: {
                                    label262: {
                                        label263: {
                                            try {
                                                succ = true;
                                                if (!KafkaConsumerProxy.this.msgFilter(KafkaConsumerProxy.this.getMqTagValue(consumerRecord))) {
                                                    succ = false;
                                                    break label249;
                                                }
                                                if (!KafkaConsumerProxy.this.msgFilterByColor(KafkaConsumerProxy.this.getMqColorValue(consumerRecord))) {
                                                    succ = false;
                                                    break label262;
                                                }
                                                KafkaConsumerProxy.this.decryptMsgBodyIfNecessary(consumerRecord);
                                                long begin = System.currentTimeMillis();
                                                MsgConsumedStatus msgConsumedStatus = MsgConsumedStatus.SUCCEED;
                                                if (KafkaConsumerProxy.this.listener.isEasy()) {
                                                    ConsumeMessage consumeMessage = ConsumeMessage.parse(consumerRecord);
                                                    msgConsumedStatus = KafkaConsumerProxy.this.listener.onMessage(consumeMessage);
                                                } else if (KafkaConsumerProxy.this.listener instanceof KafkaMessageListener l) {
                                                    msgConsumedStatus = l.onMessage(consumerRecord);
                                                } else {
                                                    KafkaBatchMsgListener lx = (KafkaBatchMsgListener)KafkaConsumerProxy.this.listener;
                                                    List<ConsumerRecord<String, byte[]>> consumerRecordList = new ArrayList<>();
                                                    consumerRecordList.add(consumerRecord);
                                                    msgConsumedStatus = lx.onMessage(consumerRecordList);
                                                }
                                                long duration = System.currentTimeMillis() - begin;
                                                KafkaConsumerProxy.this.mmsMetrics.userCostTimeMs().update(duration, TimeUnit.MILLISECONDS);
                                                if (MmsConsumerProxy.logger.isDebugEnabled()) {
                                                    MmsConsumerProxy.logger.debug("consumerRecord  topic {} partition {} offset {} consumed {}", consumerRecord.topic(), consumerRecord.partition(), consumerRecord.offset(), msgConsumedStatus.name());
                                                    succ = false;
                                                } else {
                                                    succ = false;
                                                }
                                                break label263;
                                            } catch (Throwable var24) {
                                                MmsConsumerProxy.logger.error("consume message error, record:{}", consumerRecord, var24);
                                                succ = false;
                                            } finally {
                                                if (succ) {
                                                    BlockingQueue bqx = KafkaConsumerProxy.this.acksMap.computeIfAbsent(this.partition, (p) -> new ArrayBlockingQueue(KafkaConsumerProxy.this.ackPartitionRecords));
                                                    while(!bqx.offer(consumerRecord)) {
                                                        MmsConsumerProxy.logger.warn("add consumer record to acksMap full and trigger commit offsets at {}, topicPartition:{}", LocalDateTime.now(), this.topicPartition);
                                                        KafkaConsumerProxy.this.partitionOperateContext.addCommitOffsetPartition(this.topicPartition);
                                                        try {
                                                            TimeUnit.MILLISECONDS.sleep(50L);
                                                        } catch (InterruptedException var19) {
                                                            MmsConsumerProxy.logger.error("interrupted when offer consumerRecord to msgConsumeQueue.");
                                                        }
                                                    }
                                                    countDownLatch.countDown();
                                                }
                                            }
                                            bq = KafkaConsumerProxy.this.acksMap.computeIfAbsent(this.partition, (p) -> new ArrayBlockingQueue(KafkaConsumerProxy.this.ackPartitionRecords));
                                            while(!bq.offer(consumerRecord)) {
                                                MmsConsumerProxy.logger.warn("add consumer record to acksMap full and trigger commit offsets at {}, topicPartition:{}", LocalDateTime.now(), this.topicPartition);
                                                KafkaConsumerProxy.this.partitionOperateContext.addCommitOffsetPartition(this.topicPartition);
                                                try {
                                                    TimeUnit.MILLISECONDS.sleep(50L);
                                                } catch (InterruptedException e) {
                                                    MmsConsumerProxy.logger.error("interrupted when offer consumerRecord to msgConsumeQueue.");
                                                }
                                            }
                                            countDownLatch.countDown();
                                            return;
                                        }
                                        bq = KafkaConsumerProxy.this.acksMap.computeIfAbsent(this.partition, (p) -> new ArrayBlockingQueue(KafkaConsumerProxy.this.ackPartitionRecords));
                                        while(!bq.offer(consumerRecord)) {
                                            MmsConsumerProxy.logger.warn("add consumer record to acksMap full and trigger commit offsets at {}, topicPartition:{}", LocalDateTime.now(), this.topicPartition);
                                            KafkaConsumerProxy.this.partitionOperateContext.addCommitOffsetPartition(this.topicPartition);
                                            try {
                                                TimeUnit.MILLISECONDS.sleep(50L);
                                            } catch (InterruptedException e) {
                                                MmsConsumerProxy.logger.error("interrupted when offer consumerRecord to msgConsumeQueue.");
                                            }
                                        }
                                        countDownLatch.countDown();
                                        return;
                                    }
                                    bq = KafkaConsumerProxy.this.acksMap.computeIfAbsent(this.partition, (p) -> new ArrayBlockingQueue<>(KafkaConsumerProxy.this.ackPartitionRecords));
                                    while(!bq.offer(consumerRecord)) {
                                        MmsConsumerProxy.logger.warn("add consumer record to acksMap full and trigger commit offsets at {}, topicPartition:{}", LocalDateTime.now(), this.topicPartition);
                                        KafkaConsumerProxy.this.partitionOperateContext.addCommitOffsetPartition(this.topicPartition);
                                        try {
                                            TimeUnit.MILLISECONDS.sleep(50L);
                                        } catch (InterruptedException e) {
                                            MmsConsumerProxy.logger.error("interrupted when offer consumerRecord to msgConsumeQueue.");
                                        }
                                    }
                                    countDownLatch.countDown();
                                    return;
                                }
                                bq = KafkaConsumerProxy.this.acksMap.computeIfAbsent(this.partition, (p) -> new ArrayBlockingQueue<>(KafkaConsumerProxy.this.ackPartitionRecords));
                                while(!bq.offer(consumerRecord)) {
                                    MmsConsumerProxy.logger.warn("add consumer record to acksMap full and trigger commit offsets at {}, topicPartition:{}", LocalDateTime.now(), this.topicPartition);
                                    KafkaConsumerProxy.this.partitionOperateContext.addCommitOffsetPartition(this.topicPartition);
                                    try {
                                        TimeUnit.MILLISECONDS.sleep(50L);
                                    } catch (InterruptedException e) {
                                        MmsConsumerProxy.logger.error("interrupted when offer consumerRecord to msgConsumeQueue.");
                                    }
                                }
                                countDownLatch.countDown();
                            };
                            boolean done = false;
                            while(!done) {
                                try {
                                    ExecutorService executor = this.executors.get(Math.abs(consumerRecord.key().hashCode() % this.executors.size()));
                                    executor.execute(task);
                                    done = true;
                                } catch (RejectedExecutionException e) {
                                    MmsConsumerProxy.logger.error("consume slow, wait for moment");
                                    try {
                                        TimeUnit.MILLISECONDS.sleep(50L);
                                    } catch (InterruptedException ex) {
                                        MmsConsumerProxy.logger.error("interrupted when consume slow");
                                    }
                                }
                            }
                        }
                        try {
                            countDownLatch.await();
                        } catch (InterruptedException var10) {
                            MmsConsumerProxy.logger.error("wait for kafka consumer latch interrupted", var10);
                        }
                        KafkaConsumerProxy.this.partitionOperateContext.addCommitOffsetPartition(this.topicPartition);
                    }
                }
            })).start();
        }

        public boolean isStarted() {
            return this.started.get();
        }

        public void stop() {
            this.started.compareAndSet(true, false);
            this.msgConsumeQueue.clear();
            this.executors.forEach(ExecutorService::shutdown);
            MmsConsumerProxy.logger.info("Partition[{}] stopped consume orderly.", this.topicPartition);
        }
    }

    interface ConsumeMessageService {
        void execute(List<ConsumerRecord<String, byte[]>> consumerRecords);

        void start();

        boolean isStarted();

        void stop();
    }

    private static final class LoggingCommitCallback implements OffsetCommitCallback {
        private static final Logger logger = LoggerFactory.getLogger(LoggingCommitCallback.class);

        public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception exception) {
            if (exception != null) {
                logger.error("Commit failed for " + offsets, exception);
            } else if (logger.isDebugEnabled()) {
                logger.debug("Commits for " + offsets + " completed");
            }
        }
    }
}
