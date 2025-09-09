package com.github.bannirui.msb.mq.configuration;

import com.github.bannirui.mms.client.config.MmsClientConfig;
import com.github.bannirui.msb.constant.AppEventListenerSort;
import com.github.bannirui.msb.ex.FrameworkException;
import com.github.bannirui.msb.plugin.InterceptorUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;

import java.util.HashMap;
import java.util.Map;

/**
 * 接收到{@link ApplicationReadyEvent}生命周期回调时机向mq服务端发起订阅.
 */
public class SubscribeEventListener implements ApplicationListener<ApplicationReadyEvent>, Ordered {
    private static final Logger logger = LoggerFactory.getLogger(SubscribeEventListener.class);

    @Autowired
    private SubscribeTemplate subscribeTemplate;

    /**
     * 应用准备好 订阅mq消息.
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // 缓存的监听器配置信息
        Map<String, SubscribeInfo> consumerInfo = MMSContext.getConsumerInfo();
        for (Map.Entry<String, SubscribeInfo> entry : consumerInfo.entrySet()) {
            String consumerGroup = entry.getKey();
            SubscribeInfo subscribeInfo = entry.getValue();
            try {
                // 创建代理
                MessageListenerImpl listenerProxy = InterceptorUtil.getProxyObj(MessageListenerImpl.class, new Class[]{String.class}, new Object[]{consumerGroup}, "MMS.Consumer");
                listenerProxy.setEasy(subscribeInfo.isEasy());
                Map<MmsClientConfig.CONSUMER, Object> properties = new HashMap<>();
                if (StringUtils.isNotBlank(subscribeInfo.getConsumeThreadMax())) {
                    properties.put(MmsClientConfig.CONSUMER.CONSUME_THREAD_MAX, subscribeInfo.getConsumeThreadMax());
                }
                if (StringUtils.isNotBlank(subscribeInfo.getConsumeThreadMin())) {
                    properties.put(MmsClientConfig.CONSUMER.CONSUME_THREAD_MIN, subscribeInfo.getConsumeThreadMin());
                }
                if (StringUtils.isNotEmpty(subscribeInfo.getOrderlyConsumePartitionParallelism())) {
                    properties.put(MmsClientConfig.CONSUMER.ORDERLY_CONSUME_PARTITION_PARALLELISM, subscribeInfo.getOrderlyConsumePartitionParallelism());
                }
                if (StringUtils.isNotBlank(subscribeInfo.getMaxBatchRecords())) {
                    properties.put(MmsClientConfig.CONSUMER.MAX_BATCH_RECORDS, subscribeInfo.getMaxBatchRecords());
                }
                if (StringUtils.isNotBlank(subscribeInfo.getIsOrderly())) {
                    properties.put(MmsClientConfig.CONSUMER.CONSUME_ORDERLY, subscribeInfo.getIsOrderly());
                }
                if (StringUtils.isNotBlank(subscribeInfo.getConsumeTimeoutMs())) {
                    properties.put(MmsClientConfig.CONSUMER.CONSUME_TIMEOUT_MS, subscribeInfo.getConsumeTimeoutMs());
                }
                if (StringUtils.isNotBlank(subscribeInfo.getMaxReconsumeTimes())) {
                    properties.put(MmsClientConfig.CONSUMER.MAX_RECONSUME_TIMES, subscribeInfo.getMaxReconsumeTimes());
                }
                if (StringUtils.isNotBlank(subscribeInfo.getIsNewPush())) {
                    properties.put(MmsClientConfig.CONSUMER.CONSUME_LITE_PUSH, subscribeInfo.getIsNewPush());
                }
                if (StringUtils.isNotBlank(subscribeInfo.getOrderlyConsumeThreadSize())) {
                    properties.put(MmsClientConfig.CONSUMER.ORDERLY_CONSUME_THREAD_SIZE, subscribeInfo.getOrderlyConsumeThreadSize());
                }
                this.subscribeTemplate.subscribe(consumerGroup, subscribeInfo.getTags(), listenerProxy, properties);
                logger.info("订阅消息ConsumerGroup={} tags={}", consumerGroup, subscribeInfo.getTags());
            } catch (Exception e) {
                throw FrameworkException.getInstance(e, "消费组订阅失败：ConsumerGroup={0}", consumerGroup);
            }
        }
    }

    @Override
    public int getOrder() {
        return AppEventListenerSort.MMS;
    }
}
