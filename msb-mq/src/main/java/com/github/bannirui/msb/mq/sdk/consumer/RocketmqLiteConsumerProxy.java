package com.github.bannirui.msb.mq.sdk.consumer;

import com.github.bannirui.msb.mq.sdk.common.ConsumeFrom;
import com.github.bannirui.msb.mq.sdk.common.SLA;
import com.github.bannirui.msb.mq.sdk.common.MmsEnv;
import com.github.bannirui.msb.mq.sdk.common.MmsException;
import com.github.bannirui.msb.mq.sdk.config.MmsClientConfig;
import com.github.bannirui.msb.mq.sdk.crypto.MMSCryptoManager;
import com.github.bannirui.msb.mq.sdk.metadata.ConsumerGroupMetadata;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.client.consumer.MessageQueueListener;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.protocol.admin.TopicOffset;
import org.apache.rocketmq.remoting.protocol.admin.TopicStatsTable;
import org.apache.rocketmq.remoting.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;

public class RocketmqLiteConsumerProxy extends MmsConsumerProxy<MessageExt> {
    private long consumerPollTimeoutMs = Long.parseLong(System.getProperty("consumer.poll.timeout.ms", "100"));
    private int orderlyPartitionMaxConsumeRecords = Integer.parseInt(System.getProperty("orderly.partition.max.consume.records", "2000"));
    private int orderlyPartitionMaxPollRecords = Integer.parseInt(System.getProperty("orderly.partition.max.poll.records", "200"));
    private int pullTaskThreadCount = Integer.parseInt(System.getProperty("orderly.pull.task.thread.count", String.valueOf(Runtime.getRuntime().availableProcessors() * 2)));
    private int persistConsumerOffsetInterval = Integer.parseInt(System.getProperty("consumer.offset.commit.interval", "5000"));
    private int consumeBatchSize = Integer.parseInt(System.getProperty("consume.batch.size", "1"));
    private final ConcurrentHashMap<MessageQueue, ConsumeMessageService> consumeMessageServiceTable = new ConcurrentHashMap<>();
    private final Set<MessageQueue> lastAssignSet = Collections.synchronizedSet(new HashSet<>());
    private final Set<MessageQueue> lastRetryAssignSet = Collections.synchronizedSet(new HashSet<>());
    private final RocketmqLiteConsumerProxy.TopicOperateContext topicOperateContext = new RocketmqLiteConsumerProxy.TopicOperateContext();
    private final List<AbstractConsumerRunner> consumerRunners = new ArrayList<>();
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor((r) -> new Thread(r, "MmsRocketMqLiteConsumerScheduledThread"));
    private DefaultLitePullConsumer consumer;
    private final Set<String> tags;
    private RocketmqLiteConsumerProxy.MessageQueueCache messageQueueCache = new RocketmqLiteConsumerProxy.MessageQueueCache();
    private ReentrantLock allocateLock = new ReentrantLock();

    public RocketmqLiteConsumerProxy(ConsumerGroupMetadata metadata, SLA sla, String instanceName, Set<String> tags, Properties properties, MessageListener listener) {
        super(metadata, sla, instanceName, properties, listener);
        this.instanceName = instanceName;
        this.tags = tags;
        this.start();
    }

    protected void consumerStart() {
        this.consumer = new DefaultLitePullConsumer(this.metadata.getName());
        this.setClientId();
        this.consumer.setVipChannelEnabled(false);
        this.consumer.setAutoCommit(false);
        this.consumer.setConsumerTimeoutMillisWhenSuspend(3000L);
        this.consumer.setConsumerPullTimeoutMillis(3000L);
        this.consumer.setPullThreadNums(this.pullTaskThreadCount);
        if (this.customizedProperties != null) {
            this.addUserDefinedProperties(this.customizedProperties);
        }
        String bindingTopic = ((ConsumerGroupMetadata)this.metadata).getBindingTopic();
        String consumeFrom = ((ConsumerGroupMetadata)this.metadata).getConsumeFrom();
        String broadCast = ((ConsumerGroupMetadata)this.metadata).getBroadcast();
        if (((ConsumerGroupMetadata)this.metadata).needSuspend()) {
            logger.error("consumer {} suspend is on, please set it to off first", this.metadata.getName());
            throw new RuntimeException(String.format("consumer %s suspend is on, please set it to off first", this.metadata.getName()));
        } else {
            if (StringUtils.isEmpty(consumeFrom)) {
                this.consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
            } else if (ConsumeFrom.EARLIEST.getName().equalsIgnoreCase(consumeFrom)) {
                this.consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
            } else {
                this.consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
            }
            if (!StringUtils.isEmpty(broadCast) && Boolean.parseBoolean(broadCast)) {
                this.consumer.setMessageModel(MessageModel.BROADCASTING);
            }
            logger.info("consumer {} start with param {}", this.instanceName, this.buildConsumerInfo(this.consumer));
            try {
                String retryTopic;
                if (CollectionUtils.isNotEmpty(this.tags)) {
                    retryTopic = StringUtils.join(this.tags, "||");
                    logger.info("consumer {} start with tags {}", this.instanceName, retryTopic);
                    this.consumer.subscribe(bindingTopic, retryTopic);
                } else {
                    this.consumer.subscribe(bindingTopic, "*");
                }
                switch(this.consumer.getMessageModel()) {
                    case CLUSTERING:
                        retryTopic = MixAll.getRetryTopic(this.consumer.getConsumerGroup());
                        this.consumer.subscribe(retryTopic, "*");
                    case BROADCASTING:
                    default:
                        MessageQueueListener messageQueueListener = this.consumer.getMessageQueueListener();
                        this.consumer.setMessageQueueListener((topic, mqAll, mqDivided) -> {
                            messageQueueListener.messageQueueChanged(topic, mqAll, mqDivided);
                            this.allocate(topic, mqDivided, consumeFrom);
                        });
                }
            } catch (MQClientException e) {
                logger.error("RocketMQConsumer register {} error", bindingTopic, e);
                throw MmsException.CONSUMER_START_EXCEPTION;
            }
            try {
                this.consumer.start();
                logger.info("ConsumerProxy started at {}, consumer group name:{}", System.currentTimeMillis(), this.metadata.getName());
            } catch (Exception e) {
                logger.error("RocketMQConsumer start error", e);
            }
            this.scheduledExecutorService.scheduleAtFixedRate(() -> {
                try {
                    this.persistConsumerOffset();
                } catch (Exception e) {
                    logger.error("ScheduledTask persistAllConsumerOffset exception", e);
                }
            }, 10_000L, this.persistConsumerOffsetInterval, TimeUnit.MILLISECONDS);
        }
    }

    public void addUserDefinedProperties(Properties properties) {
        int pullBatchSize = -1;
        if (properties.containsKey(MmsClientConfig.CONSUMER.MAX_BATCH_RECORDS.getKey())) {
            pullBatchSize = Integer.parseInt(String.valueOf(properties.get(MmsClientConfig.CONSUMER.MAX_BATCH_RECORDS.getKey())));
        } else if (properties.containsKey(MmsClientConfig.CONSUMER.CONSUME_MESSAGES_SIZE.getKey())) {
            pullBatchSize = Integer.parseInt(String.valueOf(properties.get(MmsClientConfig.CONSUMER.CONSUME_MESSAGES_SIZE.getKey())));
        }
        pullBatchSize = pullBatchSize > 0 ? pullBatchSize : this.orderlyPartitionMaxPollRecords;
        this.consumer.setPullBatchSize(pullBatchSize);
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
        logger.info("RocketMQ consumer thread set to min: {}, max thread : {}, pullBatchSize : {}, consumeBatchSize:{}, pullTaskThreadCount:{} ", new Object[]{threadsNumMin, threadsNumMax, pullBatchSize, this.consumeBatchSize, this.pullTaskThreadCount});
        int orderlyConsumeThreadSize;
        if (!this.sla.isOrderly()) {
            for(orderlyConsumeThreadSize = 0; orderlyConsumeThreadSize < threadsNumMax; ++orderlyConsumeThreadSize) {
                RocketmqLiteConsumerProxy.AbstractConsumerRunner consumerRunner = new RocketmqLiteConsumerProxy.ConcurrentlyConsumerRunner();
                String threadName = "MmsRocketMqConcurrentlyConsumeThread_" + this.metadata.getName() + "_" + orderlyConsumeThreadSize;
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
                RocketmqLiteConsumerProxy.AbstractConsumerRunner consumerRunner = new RocketmqLiteConsumerProxy.OrderConsumerRunner();
                String threadName = "MmsRocketMqOrderlyConsumeThread_" + this.metadata.getName() + "_" + i;
                this.startConsumerThread(threadName, consumerRunner);
                this.consumerRunners.add(consumerRunner);
            }
        }
    }

    private void startConsumerThread(String threadName, RocketmqLiteConsumerProxy.AbstractConsumerRunner consumerRunner) {
        Thread consumerThread = new Thread(consumerRunner);
        consumerThread.setName(threadName);
        consumerThread.start();
    }

    private void allocate(String topic, Set<MessageQueue> mqDivided, String consumeFrom) {
        try {
            this.allocateLock.lock();
            Long offset;
            TopicOffset topicOffset;
            DefaultMQAdminExt defaultMQAdminExt;
            TopicStatsTable topicStatsTable;
            if (!topic.startsWith("%RETRY%")) {
                if (!this.lastAssignSet.isEmpty()) {
                    this.lastAssignSet.forEach(mq->{
                        if (mq.getTopic().equals(topic) && !mqDivided.contains(mq)) {
                            logger.info("MessageQueue: brokerName:{},topic:{},queueId:{}被移除", mq.getBrokerName(), mq.getTopic(), mq.getQueueId());
                            ConsumeMessageService consumeMessageService = this.consumeMessageServiceTable.remove(mq);
                            if (null != consumeMessageService) {
                                consumeMessageService.stop();
                                this.lastAssignSet.remove(mq);
                            }
                        }
                    });
                }
                defaultMQAdminExt = null;
                topicStatsTable = null;
                try {
                    defaultMQAdminExt = new DefaultMQAdminExt();
                    defaultMQAdminExt.setNamesrvAddr(this.metadata.getClusterMetadata().getBootAddr());
                    defaultMQAdminExt.start();
                    topicStatsTable = defaultMQAdminExt.examineTopicStats(topic);
                } catch (Exception e) {
                    logger.error("defaultMQAdminExt start error.", e);
                }
                MessageQueue mq = null;
                Iterator<MessageQueue> it = mqDivided.iterator();
                while(true) {
                    do {
                        if (!it.hasNext()) {
                            if (null != defaultMQAdminExt) {
                                defaultMQAdminExt.shutdown();
                            }
                            return;
                        }
                        mq = it.next();
                    } while(this.lastAssignSet.contains(mq));
                    logger.info("MessageQueue: brokerName:{},topic:{},queueId:{}被加入", mq.getBrokerName(), mq.getTopic(), mq.getQueueId());
                    try {
                        offset = this.consumer.committed(mq);
                        if (offset == null || offset < 0L) {
                            topicOffset = topicStatsTable.getOffsetTable().get(mq);
                            if (ConsumeFrom.EARLIEST.getName().equalsIgnoreCase(consumeFrom)) {
                                this.consumer.seek(mq, topicOffset.getMinOffset());
                            } else {
                                this.consumer.seek(mq, topicOffset.getMaxOffset());
                            }
                        }
                        this.lastAssignSet.add(mq);
                        if (null == this.consumeMessageServiceTable.get(mq)) {
                            MessageQueue fmq = mq;
                            this.consumeMessageServiceTable.computeIfAbsent(mq, (key) -> this.createConsumeMessageService(fmq));
                        }
                    } catch (Exception e) {
                        logger.error("consumer seek error", e);
                    }
                }
            } else {
                if (CollectionUtils.isNotEmpty(this.lastRetryAssignSet)) {
                    this.lastRetryAssignSet.forEach(mq->{
                        if (mq.getTopic().equals(topic) && !mqDivided.contains(mq)) {
                            logger.info("MessageQueue: brokerName:{},topic:{},queueId:{}被移除", mq.getBrokerName(), mq.getTopic(), mq.getQueueId());
                            ConsumeMessageService service = this.consumeMessageServiceTable.remove(mq);
                            service.stop();
                            this.lastRetryAssignSet.remove(mq);
                        }
                    });
                }
                defaultMQAdminExt = null;
                topicStatsTable = null;
                try {
                    defaultMQAdminExt = new DefaultMQAdminExt();
                    defaultMQAdminExt.setNamesrvAddr(this.metadata.getClusterMetadata().getBootAddr());
                    defaultMQAdminExt.start();
                    topicStatsTable = defaultMQAdminExt.examineTopicStats(topic);
                } catch (Exception var16) {
                    logger.error("defaultMQAdminExt start error.", var16);
                }
                MessageQueue mq = null;
                Iterator<MessageQueue> it = mqDivided.iterator();
                while (true) {
                    do {
                        if (!it.hasNext()) {
                            if (null != defaultMQAdminExt) {
                                defaultMQAdminExt.shutdown();
                            }
                            return;
                        }
                        mq = (MessageQueue)it.next();
                    } while(this.lastRetryAssignSet.contains(mq));
                    logger.info("MessageQueue: brokerName:{},topic:{},queueId:{}被加入", mq.getBrokerName(), mq.getTopic(), mq.getQueueId());
                    try {
                        offset = this.consumer.committed(mq);
                        if (offset == null || offset < 0L) {
                            topicOffset = topicStatsTable.getOffsetTable().get(mq);
                            if (ConsumeFrom.EARLIEST.getName().equalsIgnoreCase(consumeFrom)) {
                                this.consumer.seek(mq, topicOffset.getMinOffset());
                            } else {
                                this.consumer.seek(mq, topicOffset.getMaxOffset());
                            }
                        }
                        this.lastRetryAssignSet.add(mq);
                        if (null == this.consumeMessageServiceTable.get(mq)) {
                            this.consumeMessageServiceTable.put(mq, this.createConsumeMessageService(mq));
                        }
                    } catch (Exception e) {
                        logger.error("consumer seek error", e);
                    }
                }
            }
        } finally {
            this.allocateLock.unlock();
        }
    }

    private RocketmqLiteConsumerProxy.ConsumeMessageService createConsumeMessageService(MessageQueue messageQueue) {
        return this.sla.isOrderly ? new ConsumeMessageOrderlyService(messageQueue) : new ConsumerMessageConcurrentlyService(messageQueue);
    }

    public void register(MessageListener listener) {
        String threadName = "MmsRocketMQPollThread-" + this.metadata.getName() + "-" + this.instanceName + LocalDateTime.now();
        Thread mmsPullThread = new Thread(() -> {
            while(true) {
                try {
                    if (this.running) {
                        List<MessageExt> records = this.consumer.poll(this.consumerPollTimeoutMs);
                        if (logger.isDebugEnabled()) {
                            logger.debug("messaged pulled at {} for topic {}, poll size: {} ", System.currentTimeMillis(), ((ConsumerGroupMetadata)this.metadata).getBindingTopic(),
                                records == null ? 0 : records.size());
                        }
                        this.submitRecords(records);
                        this.topicOperateContext.pause();
                        this.topicOperateContext.resume();
                        continue;
                    }
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

    private void submitRecords(List<MessageExt> records) {
        if (records != null && !records.isEmpty()) {
            MessageExt firstMsg = records.get(0);
            MessageQueue messageQueue = this.messageQueueCache.get(firstMsg.getBrokerName(), firstMsg.getTopic(), firstMsg.getQueueId());
            RocketmqLiteConsumerProxy.ConsumeMessageService tempConsumeMessageService = this.getOrCreateConsumeMessageService(messageQueue);
            tempConsumeMessageService.execute(records);
        }
    }

    private RocketmqLiteConsumerProxy.ConsumeMessageService getOrCreateConsumeMessageService(MessageQueue messageQueue) {
        return this.consumeMessageServiceTable.computeIfAbsent(messageQueue, (key) -> {
            this.lastAssignSet.add(messageQueue);
            return this.createConsumeMessageService(messageQueue);
        });
    }

    private RocketmqLiteConsumerProxy.ConsumeMessageService getOrCreateConsumeMessageService(String brokerName, String topicName, int queueId) {
        MessageQueue messageQueue = this.messageQueueCache.get(brokerName, topicName, queueId);
        return this.consumeMessageServiceTable.computeIfAbsent(messageQueue, (key) -> {
            this.lastAssignSet.add(messageQueue);
            return this.createConsumeMessageService(messageQueue);
        });
    }

    private void setClientId() {
        long now = System.currentTimeMillis();
        if (this.metadata.isGatedLaunch()) {
            this.consumer.setNamesrvAddr(this.metadata.getGatedCluster().getBootAddr());
            this.consumer.setClientIP("consumer-client-id-" + this.metadata.getGatedCluster().getClusterName() + "-" + MmsEnv.MMS_IP + "-" + now);
        } else {
            this.consumer.setNamesrvAddr(this.metadata.getClusterMetadata().getBootAddr());
            this.consumer.setClientIP("consumer-client-id-" + this.metadata.getClusterMetadata().getClusterName() + "-" + MmsEnv.MMS_IP + "-" + now);
        }
    }

    private String getMqTagValue(MessageExt msg) {
        return msg.getProperties().get("mqTag");
    }

    private String getMqColorValue(MessageExt msg) {
        return msg.getProperties().get("mqColor");
    }

    private List<MessageExt> filterMsg(List<MessageExt> msgs) {
        return msgs.stream().filter((record) -> this.msgFilter(this.getMqTagValue(record))).filter((consumerRecord) -> this.msgFilterByColor(this.getMqColorValue(consumerRecord))).collect(Collectors.toList());
    }

    private String showBatchMsgInfo(List<MessageExt> msgs) {
        StringBuilder sbf = new StringBuilder(200);
        msgs.forEach(msg -> {
            sbf.append(",msgId:").append(msg.getMsgId()).append(",keys:").append(msg.getKeys()).append(",queueId:").append(msg.getQueueId()).append(",queueOffset:").append(msg.getQueueOffset());
        });
        return sbf.toString().substring(1);
    }

    private void sendMessageBack(List<MessageExt> msgs, MsgConsumedStatus status) {
        int i;
        MessageExt msg;
        switch(this.consumer.getMessageModel()) {
            case BROADCASTING:
                for(i = 0; i < msgs.size(); ++i) {
                    msg = msgs.get(i);
                    logger.warn("BROADCASTING, the message consume failed, drop it, {}", msg.toString());
                }
                return;
            case CLUSTERING:
                for(i = 0; i < msgs.size(); ++i) {
                    msg = msgs.get(i);
                    String key = msg.getProperties().get("encrypt_mark");
                    if (StringUtils.isNotBlank(key)) {
                        msg.setBody(MMSCryptoManager.encrypt(((ConsumerGroupMetadata)this.metadata).getBindingTopic(), msg.getBody()));
                    }
                    try {
                        while(!this.sendMessageBack(msg, status.getLevel())) {
                            Thread.sleep(1_000L);
                        }
                    } catch (Throwable e) {
                        logger.error("send message back error", e);
                    }
                }
        }
    }

    private boolean sendMessageBack(final MessageExt msg, int delayLevel) {
        msg.setTopic(this.consumer.withNamespace(msg.getTopic()));
        try {
            // TODO: 2025/1/22
            // this.consumer.sendMessageBack(msg, delayLevel);
            return true;
        } catch (Exception e) {
            logger.error("sendMessageBack exception, group: " + this.consumer.getConsumerGroup() + " msg: " + msg.toString(), e);
            return false;
        }
    }

    private String buildConsumerInfo(DefaultLitePullConsumer consumer) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" clientIP: " + consumer.getClientIP());
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append(" nameSrv: " + consumer.getNamesrvAddr());
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append(" batchSize: " + consumer.getPullBatchSize());
        stringBuilder.append(System.lineSeparator());
        return stringBuilder.toString();
    }

    protected void decryptMsgBodyIfNecessary(MessageExt msg) {
        Map<String, String> properties = msg.getProperties();
        String encryptMarkValue = properties.get("encrypt_mark");
        if (StringUtils.isNotBlank(encryptMarkValue)) {
            msg.setBody(MMSCryptoManager.decrypt(((ConsumerGroupMetadata)this.metadata).getBindingTopic(), msg.getBody()));
        }
    }

    protected void consumerShutdown() {
        this.consumer.shutdown();
        if(CollectionUtils.isEmpty(this.consumerRunners)) return;
        this.consumerRunners.forEach(AbstractConsumerRunner::stop);
        this.consumerRunners.clear();
    }

    private void addOffset(MessageQueue messageQueue, Long offset) {
        if (offset >= 0L) {
            this.consumer.getOffsetStore().updateOffset(messageQueue, offset, true);
        }
    }

    private void persistConsumerOffset() {
        try {
            Set<MessageQueue> assignSet = new HashSet<>();
            assignSet.addAll(this.lastAssignSet);
            this.consumer.getOffsetStore().persistAll(assignSet);
            Set<MessageQueue> retryssignSet = new HashSet<>();
            retryssignSet.addAll(this.lastRetryAssignSet);
            this.consumer.getOffsetStore().persistAll(retryssignSet);
        } catch (Throwable e) {
            logger.error("提交消费位点失败", e);
        }

    }

    static class MessageQueueCache {
        private ConcurrentHashMap<String, MessageQueue> poll = new ConcurrentHashMap();

        public MessageQueue get(String brokerName, String topicName, int queueId) {
            String key = String.format("%s:%s:%s", brokerName, topicName, queueId);
            return this.poll.computeIfAbsent(key, (queue) -> new MessageQueue(topicName, brokerName, queueId));
        }
    }

    class TopicOperateContext {
        private final Set<MessageQueue> pausePartitions = new HashSet<>();
        private final Set<MessageQueue> alreadyPausePartitions = new HashSet<>();
        private final Set<MessageQueue> resumePartitions = new HashSet<>();
        private final ReentrantReadWriteLock pauseAndResumeLock = new ReentrantReadWriteLock();
        private final ReentrantReadWriteLock.ReadLock pauseAndResumeReadLock;
        private final ReentrantReadWriteLock.WriteLock pauseAndResumeWriteLock;

        TopicOperateContext() {
            this.pauseAndResumeReadLock = this.pauseAndResumeLock.readLock();
            this.pauseAndResumeWriteLock = this.pauseAndResumeLock.writeLock();
        }

        public void addPausePartition(MessageQueue messageQueue) {
            this.pauseAndResumeWriteLock.lock();

            try {
                this.resumePartitions.remove(messageQueue);
                this.pausePartitions.add(messageQueue);
                MmsConsumerProxy.logger.warn("MessageQueue consumerGroup:{},brokerName:{}, topic:{},queueId:{} is pause", RocketmqLiteConsumerProxy.this.consumer.getConsumerGroup(),
                    messageQueue.getBrokerName(), messageQueue.getTopic(), messageQueue.getQueueId());
            } finally {
                this.pauseAndResumeWriteLock.unlock();
            }
        }

        public void addResumePartition(MessageQueue messageQueue) {
            this.pauseAndResumeReadLock.lock();
            try {
                if (!this.alreadyPausePartitions.contains(messageQueue)) {
                    return;
                }
            } finally {
                this.pauseAndResumeReadLock.unlock();
            }
            this.pauseAndResumeWriteLock.lock();
            try {
                if (this.alreadyPausePartitions.contains(messageQueue)) {
                    this.pausePartitions.remove(messageQueue);
                    this.alreadyPausePartitions.remove(messageQueue);
                    this.resumePartitions.add(messageQueue);
                    MmsConsumerProxy.logger.info("MessageQueue consumerGroup:{},brokerName:{}, topic:{},queueId:{} is resume",
                        RocketmqLiteConsumerProxy.this.consumer.getConsumerGroup(), messageQueue.getBrokerName(), messageQueue.getTopic(), messageQueue.getQueueId());
                }
            } finally {
                this.pauseAndResumeWriteLock.unlock();
            }
        }

        public void pause() {
            this.pauseAndResumeReadLock.lock();
            try {
                if (!this.pausePartitions.isEmpty()) {
                    RocketmqLiteConsumerProxy.this.consumer.pause(this.pausePartitions);
                    this.alreadyPausePartitions.addAll(this.pausePartitions);
                    this.pausePartitions.clear();
                }
            } finally {
                this.pauseAndResumeReadLock.unlock();
            }
        }

        public void resume() {
            this.pauseAndResumeReadLock.lock();
            try {
                if (!this.resumePartitions.isEmpty()) {
                    RocketmqLiteConsumerProxy.this.consumer.resume(this.resumePartitions);
                    this.pausePartitions.clear();
                }
            } finally {
                this.pauseAndResumeReadLock.unlock();
            }
        }
    }

    class OrderConsumerRunner extends RocketmqLiteConsumerProxy.AbstractConsumerRunner {
        OrderConsumerRunner() {
            super();
        }

        protected void doTask(List<MessageExt> msgs) {
            try {
                if(CollectionUtils.isEmpty(msgs)) {
                    return;
                }
                List<MessageExt> needConsumeList = RocketmqLiteConsumerProxy.this.filterMsg(msgs);
                if (msgs.size() != needConsumeList.size()) {
                    List<MessageExt> waitRemovedMsgs = msgs.stream().filter((item) -> !needConsumeList.contains(item)).collect(Collectors.toList());
                    this.removeMessageAndCommitOffset(waitRemovedMsgs);
                }
                needConsumeList.forEach((msgx) -> {
                    try {
                        RocketmqLiteConsumerProxy.this.decryptMsgBodyIfNecessary(msgx);
                    } catch (Throwable e) {
                        MmsConsumerProxy.logger.error("消息解密失败", e);
                        MmsConsumerProxy.logger.error("消息解密失败, msginfo: msgId:{},msgKey:{}, msg queueid:{},msg offset:{}", msgx.getMsgId(), msgx.getKeys(), msgx.getQueueId(), msgx.getQueueOffset());
                        throw new RuntimeException(e);
                    }
                });
                if (RocketmqLiteConsumerProxy.this.listener.isEasy() || RocketmqLiteConsumerProxy.this.listener instanceof RocketmqMessageListener) {
                    RocketmqMessageListener rocketmqMessageListener = (RocketmqMessageListener)RocketmqLiteConsumerProxy.this.listener;
                    for (MessageExt msg : needConsumeList) {
                        long beginx = System.currentTimeMillis();
                        if (RocketmqLiteConsumerProxy.this.listener.isEasy()) {
                            ConsumeMessage consumeMessage = ConsumeMessage.parse(msg);
                            while(true) {
                                try {
                                    MsgConsumedStatus statusxx = rocketmqMessageListener.onMessage(consumeMessage);
                                    long durationxx;
                                    if (statusxx == MsgConsumedStatus.SUCCEED) {
                                        durationxx = System.currentTimeMillis() - beginx;
                                        RocketmqLiteConsumerProxy.this.mmsMetrics.userCostTimeMs().update(durationxx, TimeUnit.MILLISECONDS);
                                        RocketmqLiteConsumerProxy.this.mmsMetrics.consumeSuccessRate().mark();
                                        break;
                                    }
                                    durationxx = System.currentTimeMillis() - beginx;
                                    RocketmqLiteConsumerProxy.this.mmsMetrics.userCostTimeMs().update(durationxx, TimeUnit.MILLISECONDS);
                                    RocketmqLiteConsumerProxy.this.mmsMetrics.consumeFailureRate().mark();
                                } catch (Throwable e) {
                                    MmsConsumerProxy.logger.error("顺序消费失败, msginfo: msgId:{},msgKey:{}, msg queueid:{},msg offset:{}", consumeMessage.getMsgId(), msg.getKeys(), msg.getQueueId(), msg.getQueueOffset());
                                    MmsConsumerProxy.logger.error("顺序消费失败", e);
                                }
                                TimeUnit.MILLISECONDS.sleep(1_000L);
                            }
                            this.removeMessageAndCommitOffset(msg);
                        } else {
                            while(true) {
                                try {
                                    MsgConsumedStatus statusx = rocketmqMessageListener.onMessage(msg);
                                    long durationx;
                                    if (statusx == MsgConsumedStatus.SUCCEED) {
                                        durationx = System.currentTimeMillis() - beginx;
                                        RocketmqLiteConsumerProxy.this.mmsMetrics.userCostTimeMs().update(durationx, TimeUnit.MILLISECONDS);
                                        RocketmqLiteConsumerProxy.this.mmsMetrics.consumeSuccessRate().mark();
                                        break;
                                    }
                                    durationx = System.currentTimeMillis() - beginx;
                                    RocketmqLiteConsumerProxy.this.mmsMetrics.userCostTimeMs().update(durationx, TimeUnit.MILLISECONDS);
                                    RocketmqLiteConsumerProxy.this.mmsMetrics.consumeFailureRate().mark();
                                } catch (Throwable e) {
                                    MmsConsumerProxy.logger.error("顺序消费失败", e);
                                    MmsConsumerProxy.logger.error("顺序消费失败, msginfo: msgId:{},msgKey:{}, msg queueid:{},msg offset:{}", msg.getMsgId(), msg.getKeys(), msg.getQueueId(), msg.getQueueOffset());
                                }
                                TimeUnit.MILLISECONDS.sleep(5_000L);
                            }
                            this.removeMessageAndCommitOffset(msg);
                        }
                    }
                } else {
                    RocketmqBatchMsgListener batchMsgListener = (RocketmqBatchMsgListener)RocketmqLiteConsumerProxy.this.listener;
                    long begin = System.currentTimeMillis();

                    while(true) {
                        try {
                            MsgConsumedStatus status = batchMsgListener.onMessage(needConsumeList);
                            long duration;
                            if (status == MsgConsumedStatus.SUCCEED) {
                                duration = System.currentTimeMillis() - begin;
                                RocketmqLiteConsumerProxy.this.mmsMetrics.userCostTimeMs().update(duration, TimeUnit.MILLISECONDS);
                                RocketmqLiteConsumerProxy.this.mmsMetrics.consumeSuccessRate().mark();
                                break;
                            }
                            duration = System.currentTimeMillis() - begin;
                            RocketmqLiteConsumerProxy.this.mmsMetrics.userCostTimeMs().update(duration, TimeUnit.MILLISECONDS);
                            RocketmqLiteConsumerProxy.this.mmsMetrics.consumeFailureRate().mark();
                        } catch (Throwable e) {
                            MmsConsumerProxy.logger.error("顺序消费失败", e);
                            MmsConsumerProxy.logger.error("顺序消费失败, 待消费详情: {}", RocketmqLiteConsumerProxy.this.showBatchMsgInfo(needConsumeList));
                        }
                    }
                    this.removeMessageAndCommitOffset(msgs);
                }
            } catch (Throwable e) {
                MmsConsumerProxy.logger.error("consume message error", e);
            }
        }
    }

    class ConcurrentlyConsumerRunner extends RocketmqLiteConsumerProxy.AbstractConsumerRunner {
        ConcurrentlyConsumerRunner() {
            super();
        }

        protected void doTask(List<MessageExt> msgs) {
            try {
                if(CollectionUtils.isEmpty(msgs)) {
                    return;
                }
                List<MessageExt> needConsumeList = RocketmqLiteConsumerProxy.this.filterMsg(msgs);
                if (msgs.size() != needConsumeList.size()) {
                    List<MessageExt> waitRemovedMsgs = msgs.stream().filter((item) -> !needConsumeList.contains(item)).collect(Collectors.toList());
                    this.removeMessageAndCommitOffset(waitRemovedMsgs);
                }
                needConsumeList.forEach((msgx) -> {
                    try {
                        RocketmqLiteConsumerProxy.this.decryptMsgBodyIfNecessary(msgx);
                    } catch (Throwable e) {
                        MmsConsumerProxy.logger.error("消息解密失败", e);
                        MmsConsumerProxy.logger.error("消息解密失败, msginfo: msgId:{},msgKey:{}, msg queueid:{},msg offset:{}", new Object[]{msgx.getMsgId(), msgx.getKeys(), msgx.getQueueId(), msgx.getQueueOffset()});
                        throw new RuntimeException(e);
                    }
                });
                MsgConsumedStatus status = null;
                long beginx;
                if (RocketmqLiteConsumerProxy.this.listener.isEasy() || RocketmqLiteConsumerProxy.this.listener instanceof RocketmqMessageListener) {
                    RocketmqMessageListener rocketmqMessageListener = (RocketmqMessageListener)RocketmqLiteConsumerProxy.this.listener;
                    for (MessageExt msg : needConsumeList) {
                        beginx = System.currentTimeMillis();
                        if (RocketmqLiteConsumerProxy.this.listener.isEasy()) {
                            ConsumeMessage consumeMessage = null;
                            try {
                                consumeMessage = ConsumeMessage.parse(msg);
                                status = rocketmqMessageListener.onMessage(consumeMessage);
                            } catch (Throwable e) {
                                MmsConsumerProxy.logger.error("并发消费失败,将按重试策略进行重试", e);
                                MmsConsumerProxy.logger.error("并发消费失败, msginfo: msgId:{},msgKey:{}, msg queueid:{},msg offset:{}", msg.getMsgId(), msg.getKeys(), msg.getQueueId(), msg.getQueueOffset());
                            }
                            if (status != MsgConsumedStatus.SUCCEED) {
                                RocketmqLiteConsumerProxy.this.mmsMetrics.consumeFailureRate().mark();
                                RocketmqLiteConsumerProxy.this.sendMessageBack(Arrays.asList(msg), status);
                            } else {
                                RocketmqLiteConsumerProxy.this.mmsMetrics.consumeSuccessRate().mark();
                            }
                            long durationx = System.currentTimeMillis() - beginx;
                            RocketmqLiteConsumerProxy.this.mmsMetrics.userCostTimeMs().update(durationx, TimeUnit.MILLISECONDS);
                            this.removeMessageAndCommitOffset(msg);
                        } else {
                            try {
                                status = rocketmqMessageListener.onMessage(msg);
                            } catch (Throwable e) {
                                MmsConsumerProxy.logger.error("并发消费失败，将按重试策略进行重试", e);
                                MmsConsumerProxy.logger.error("并发消费失败, msginfo: msgId:{},msgKey:{}, msg queueid:{},msg offset:{}", msg.getMsgId(), msg.getKeys(), msg.getQueueId(), msg.getQueueOffset());
                            }
                            if (status != MsgConsumedStatus.SUCCEED) {
                                RocketmqLiteConsumerProxy.this.mmsMetrics.consumeFailureRate().mark();
                                RocketmqLiteConsumerProxy.this.sendMessageBack(Arrays.asList(msg), status);
                            } else {
                                RocketmqLiteConsumerProxy.this.mmsMetrics.consumeSuccessRate().mark();
                            }
                            long duration = System.currentTimeMillis() - beginx;
                            RocketmqLiteConsumerProxy.this.mmsMetrics.userCostTimeMs().update(duration, TimeUnit.MILLISECONDS);
                            this.removeMessageAndCommitOffset(msg);
                        }
                    }
                } else {
                    RocketmqBatchMsgListener batchMsgListener = (RocketmqBatchMsgListener)RocketmqLiteConsumerProxy.this.listener;
                    long begin = System.currentTimeMillis();
                    try {
                        status = batchMsgListener.onMessage(needConsumeList);
                    } catch (Throwable e) {
                        MmsConsumerProxy.logger.error("并发消费失败，将按照重试策略进行重试", e);
                        MmsConsumerProxy.logger.error("并发消费失败, 待消费详情: {}", RocketmqLiteConsumerProxy.this.showBatchMsgInfo(needConsumeList));
                    }
                    if (status != MsgConsumedStatus.SUCCEED) {
                        RocketmqLiteConsumerProxy.this.mmsMetrics.consumeFailureRate().mark();
                        RocketmqLiteConsumerProxy.this.sendMessageBack(needConsumeList, status);
                    } else {
                        RocketmqLiteConsumerProxy.this.mmsMetrics.consumeSuccessRate().mark();
                    }
                    beginx = System.currentTimeMillis() - begin;
                    RocketmqLiteConsumerProxy.this.mmsMetrics.userCostTimeMs().update(beginx, TimeUnit.MILLISECONDS);
                    this.removeMessageAndCommitOffset(needConsumeList);
                }
            } catch (Throwable e) {
                MmsConsumerProxy.logger.error("consume message error", e);
            }
        }
    }

    abstract class AbstractConsumerRunner implements Runnable {
        protected volatile boolean isRunning = true;
        private BlockingQueue<MessageExt> msgQueue = new LinkedBlockingQueue<>();

        public void putMessage(MessageExt msg) {
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
                            List<MessageExt> msgs = new ArrayList<>(RocketmqLiteConsumerProxy.this.consumeBatchSize);
                            while(this.msgQueue.drainTo(msgs, RocketmqLiteConsumerProxy.this.consumeBatchSize) <= 0) {
                                Thread.sleep(20L);
                            }
                            this.doTask(msgs);
                            continue;
                        } catch (InterruptedException e) {
                            MmsConsumerProxy.logger.info("{} is Interrupt", Thread.currentThread().getName());
                        } catch (Throwable e) {
                            MmsConsumerProxy.logger.error("consume message error ", e);
                            continue;
                        }
                    }
                } catch (Throwable e) {
                    MmsConsumerProxy.logger.error("consume message error ", e);
                }
                return;
            }
        }

        public void stop() {
            this.isRunning = false;
        }

        protected abstract void doTask(List<MessageExt> msgs);

        protected void removeMessageAndCommitOffset(List<MessageExt> msgs) {
            msgs.forEach(this::removeMessageAndCommitOffset);
        }

        protected void removeMessageAndCommitOffset(MessageExt msg) {
            MessageQueue messageQueue = RocketmqLiteConsumerProxy.this.messageQueueCache.get(msg.getBrokerName(), msg.getTopic(), msg.getQueueId());
            RocketmqLiteConsumerProxy.AbstractConsumeMessageService consumeMessageService = (RocketmqLiteConsumerProxy.AbstractConsumeMessageService)RocketmqLiteConsumerProxy.this.getOrCreateConsumeMessageService(messageQueue);
            long offset = consumeMessageService.removeMessage(msg);
            RocketmqLiteConsumerProxy.this.addOffset(messageQueue, offset);
            consumeMessageService.maybeNeedResume();
        }
    }

    private class ConsumeMessageOrderlyService extends RocketmqLiteConsumerProxy.AbstractConsumeMessageService {
        private final int NO_KEY_HASH = "__nokey".hashCode();

        public ConsumeMessageOrderlyService(MessageQueue messageQueue) {
            super(messageQueue);
            this.start();
        }

        protected RocketmqLiteConsumerProxy.AbstractConsumerRunner selectConsumerRunner(MessageExt msg) {
            return RocketmqLiteConsumerProxy.this.consumerRunners.get(Math.abs(this.getHashCode(msg) % RocketmqLiteConsumerProxy.this.consumerRunners.size()));
        }

        public void start() {
            MmsConsumerProxy.logger.info("QueueId[{}] starting consume orderly.", this.messageQueue.getQueueId());
        }

        public void stop() {
            this.started.compareAndSet(true, false);
            MmsConsumerProxy.logger.info("MessageQueue[{}] stopped consume orderly.", this.messageQueue);
        }

        private final int getHashCode(MessageExt msg) {
            String keys = msg.getKeys();
            if (StringUtils.isEmpty(keys)) {
                MmsConsumerProxy.logger.error("顺序消费没有设置key,将采用默认key，请及时优化");
                return this.NO_KEY_HASH;
            } else {
                return keys.hashCode();
            }
        }
    }

    private class ConsumerMessageConcurrentlyService extends RocketmqLiteConsumerProxy.AbstractConsumeMessageService {
        private AtomicInteger nextId = new AtomicInteger(0);
        private static final int reset = 2146483647;

        public ConsumerMessageConcurrentlyService(MessageQueue messageQueue) {
            super(messageQueue);
            this.start();
        }

        protected RocketmqLiteConsumerProxy.AbstractConsumerRunner selectConsumerRunner(MessageExt msg) {
            int next = this.nextId.incrementAndGet();
            if (next > 2146483647) {
                this.nextId.set(0);
            }
            return RocketmqLiteConsumerProxy.this.consumerRunners.get(Math.abs(next % RocketmqLiteConsumerProxy.this.consumerRunners.size()));
        }

        public void start() {
            MmsConsumerProxy.logger.info("QueueId[{}] starting consume concurrently.", this.messageQueue.getQueueId());
        }

        public void stop() {
            this.started.compareAndSet(true, false);
            MmsConsumerProxy.logger.info("QueueId[{}] stop consume concurrently.", this.messageQueue.getQueueId());
        }
    }

    private abstract class AbstractConsumeMessageService implements RocketmqLiteConsumerProxy.ConsumeMessageService {
        protected MessageQueue messageQueue;
        private long queueMaxOffset = -1L;
        protected final AtomicBoolean started = new AtomicBoolean(false);
        protected ReentrantReadWriteLock msgTreeMapLock = new ReentrantReadWriteLock();
        protected ReentrantReadWriteLock.ReadLock msgTreeMapReadLock;
        protected ReentrantReadWriteLock.WriteLock msgTreeMapWriteLock;
        protected final TreeMap<Long, MessageExt> msgTreeMap;

        public AbstractConsumeMessageService(MessageQueue messageQueue) {
            this.msgTreeMapReadLock = this.msgTreeMapLock.readLock();
            this.msgTreeMapWriteLock = this.msgTreeMapLock.writeLock();
            this.msgTreeMap = new TreeMap<>();
            this.messageQueue = messageQueue;
        }

        public void execute(List<MessageExt> consumerRecords) {
            if (consumerRecords != null && !consumerRecords.isEmpty()) {
                this.putMessage(consumerRecords);
                if (this.isNeedPause()) {
                    RocketmqLiteConsumerProxy.this.topicOperateContext.addPausePartition(this.messageQueue);
                }
                consumerRecords.forEach(msg -> {
                    RocketmqLiteConsumerProxy.AbstractConsumerRunner orderConsumerRunner = this.selectConsumerRunner(msg);
                    orderConsumerRunner.putMessage(msg);
                });
            }
        }

        protected abstract RocketmqLiteConsumerProxy.AbstractConsumerRunner selectConsumerRunner(MessageExt msg);

        protected Long removeMessage(MessageExt msg) {
            long result = -1L;
            try {
                this.msgTreeMapWriteLock.lock();
                if (!this.msgTreeMap.isEmpty()) {
                    result = this.queueMaxOffset + 1L;
                    this.msgTreeMap.remove(msg.getQueueOffset());
                    if (!this.msgTreeMap.isEmpty()) {
                        result = this.msgTreeMap.firstKey();
                    }
                }
            } finally {
                this.msgTreeMapWriteLock.unlock();
            }
            return result;
        }

        protected Long removeMessage(List<MessageExt> msgs) {
            long result = -1L;
            try {
                this.msgTreeMapWriteLock.lock();
                if (!this.msgTreeMap.isEmpty()) {
                    result = this.queueMaxOffset + 1L;
                    msgs.forEach(msg->{
                        this.msgTreeMap.remove(msg.getQueueOffset());
                    });
                    if(MapUtils.isNotEmpty(this.msgTreeMap)) {
                        result = this.msgTreeMap.firstKey();
                    }
                }
            } finally {
                this.msgTreeMapWriteLock.unlock();
            }
            return result;
        }

        protected void putMessage(List<MessageExt> msgs) {
            try {
                this.msgTreeMapWriteLock.lock();
                msgs.forEach((msg) -> {
                    MessageExt old = this.msgTreeMap.put(msg.getQueueOffset(), msg);
                    if (old == null) {
                        this.queueMaxOffset = msg.getQueueOffset();
                    }
                });
            } finally {
                this.msgTreeMapWriteLock.unlock();
            }
        }

        protected void maybeNeedResume() {
            if (this.isNeedResume()) {
                RocketmqLiteConsumerProxy.this.topicOperateContext.addResumePartition(this.messageQueue);
            }
        }

        protected boolean isNeedPause() {
            this.msgTreeMapReadLock.lock();
            try {
                if (this.msgTreeMap.isEmpty()) {
                    return false;
                }
                return this.msgTreeMap.size() > RocketmqLiteConsumerProxy.this.orderlyPartitionMaxConsumeRecords
                    || this.msgTreeMap.lastKey() - this.msgTreeMap.firstKey() > (long)RocketmqLiteConsumerProxy.this.orderlyPartitionMaxConsumeRecords;
            } finally {
                this.msgTreeMapReadLock.unlock();
            }
        }

        protected boolean isNeedResume() {
            this.msgTreeMapReadLock.lock();
            try {
                if (!this.msgTreeMap.isEmpty()) {
                    if (this.msgTreeMap.size() < RocketmqLiteConsumerProxy.this.orderlyPartitionMaxConsumeRecords / 2) {
                        return true;
                    }
                    return false;
                }
                return true;
            } finally {
                this.msgTreeMapReadLock.unlock();
            }
        }
    }

    interface ConsumeMessageService {
        void execute(List<MessageExt> consumerRecords);

        void start();

        void stop();
    }
}
