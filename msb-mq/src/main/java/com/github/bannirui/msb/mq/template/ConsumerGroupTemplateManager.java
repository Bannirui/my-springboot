package com.github.bannirui.msb.mq.template;

import com.github.bannirui.msb.common.ex.FrameworkException;
import com.github.bannirui.msb.common.plugin.InterceptorUtil;
import com.github.bannirui.msb.mq.configuration.MMSConf;
import com.github.bannirui.msb.mq.configuration.MMSContext;
import com.github.bannirui.msb.mq.configuration.MMSMessageListenerImpl;
import com.github.bannirui.msb.mq.configuration.MMSSubscribeInfo;
import com.github.bannirui.msb.mq.sdk.Mms;
import com.github.bannirui.msb.mq.sdk.config.MmsClientConfig;
import com.github.bannirui.msb.mq.sdk.consumer.ConsumerFactory;
import com.github.bannirui.msb.mq.sdk.consumer.MmsConsumerProxy;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumerGroupTemplateManager implements ConsumerGroupManager {
    private static final Logger logger = LoggerFactory.getLogger(ConsumerGroupTemplateManager.class);
    public static final ConsumerGroupTemplateManager INSTANCE = new ConsumerGroupTemplateManager();
    private final Map<String, Set<String>> enabledConsumerGroupMap = new HashMap<>();
    private final Set<String> templateNames = new HashSet<>();

    public Set<String> getTemplateNames() {
        return this.templateNames;
    }

    public Set<String> getEnabledConsumerGroups() {
        return this.enabledConsumerGroupMap.keySet();
    }

    public Set<String> getEnabledTags(String consumerGroup) {
        return this.enabledConsumerGroupMap.get(consumerGroup);
    }

    public void enableSubscribe(String templateName, String consumerGroup, String tag) {
        synchronized(this) {
            if (StringUtils.isEmpty(consumerGroup)) {
                throw FrameworkException.getInstance("订阅消费组不能为空");
            } else if (StringUtils.isEmpty(templateName)) {
                throw FrameworkException.getInstance("模版名称不能为空");
            } else if (!this.templateNames.contains(templateName)) {
                throw FrameworkException.getInstance("模版消费组[{}]没有初始化，禁止启动，请检查该模版消费组在项目中是否有使用MMSListenerTemplate注解", templateName);
            } else {
                if (StringUtils.isEmpty(tag)) {
                    tag = "*";
                }
                Set<String> consumerGroupSet = this.enabledConsumerGroupMap.keySet();
                if (!consumerGroupSet.isEmpty()) {
                    Set<String> tagSet = this.enabledConsumerGroupMap.get(consumerGroup);
                    if (consumerGroupSet.contains(consumerGroup) && tagSet != null && tagSet.contains(tag)) {
                        throw FrameworkException.getInstance("消费组[group:{},tag:{}]已启动完成，无需重复启动", consumerGroup, tag);
                    }
                }
                MMSSubscribeInfo mmsTemplateSubscribeInfo = MMSContext.getTemplateConsumerInfo().get(templateName);
                MMSConf mmsTemplateConf = MMSContext.getTemplateMmsConfMap().get(templateName);
                if (mmsTemplateSubscribeInfo != null && mmsTemplateConf != null) {
                    MMSContext.putConsumerInfo(consumerGroup, mmsTemplateSubscribeInfo);
                    String mmsConfMapKey = consumerGroup + "~" + tag;
                    MMSContext.getMmsConfMap().put(mmsConfMapKey, mmsTemplateConf);
                    MMSMessageListenerImpl proxyObj = this.buildListener(consumerGroup);
                    Map<MmsClientConfig.CONSUMER, Object> properties = this.buildProperties(consumerGroup);
                    Set<String> tagSet = Collections.newSetFromMap(new ConcurrentHashMap<>());
                    tagSet.add(tag);
                    if (this.enabledConsumerGroupMap.get(consumerGroup) != null && !this.enabledConsumerGroupMap.get(consumerGroup).isEmpty()) {
                        tagSet.addAll(this.enabledConsumerGroupMap.get(consumerGroup));
                    }
                    List<MmsConsumerProxy> mmsConsumerProxies = Lists.newArrayList(ConsumerFactory.getConsumers()).stream().filter((proxy) -> proxy.getMetadata().getName().equals(consumerGroup)).toList();
                    if (mmsConsumerProxies.isEmpty()) {
                        Mms.subscribe(consumerGroup, tagSet, proxyObj, properties);
                    } else {
                        mmsConsumerProxies.forEach(MmsConsumerProxy::shutdown);
                        Mms.subscribe(consumerGroup, tagSet, proxyObj, properties);
                    }
                    this.enabledConsumerGroupMap.put(consumerGroup, tagSet);
                    logger.info("消费组[group:{}, tag:{}]通过消费组模版[{}]启动成功!", consumerGroup, tag, templateName);
                } else {
                    throw FrameworkException.getInstance("MMSContext上下文环境没有该模版消费组信息: mmsTemplateSubscribeInfo:{}, MMSConf:{}，请检查项目是否有启用@MMSListenerTemplate对[{}]模版消费组进行注入", mmsTemplateSubscribeInfo, mmsTemplateConf, templateName);
                }
            }
        }
    }

    private MMSMessageListenerImpl buildListener(String consumerGroup) {
        MMSSubscribeInfo MMSSubscribeInfo = MMSContext.getConsumerInfo().get(consumerGroup);
        MMSMessageListenerImpl proxyObj;
        try {
            proxyObj = InterceptorUtil.getProxyObj(MMSMessageListenerImpl.class, new Class[]{String.class}, new Object[]{consumerGroup}, "MMS.Consumer");
        } catch (Exception e) {
            throw FrameworkException.getInstance(e, "懒加载消费组监听插件创建失败,errorMessage{0}", e);
        }
        proxyObj.setEasy(MMSSubscribeInfo.isEasy());
        return proxyObj;
    }

    private Map<MmsClientConfig.CONSUMER, Object> buildProperties(String consumerGroup) {
        MMSSubscribeInfo MMSSubscribeInfo = (MMSSubscribeInfo) MMSContext.getConsumerInfo().get(consumerGroup);
        Map<MmsClientConfig.CONSUMER, Object> properties = new HashMap();
        if (StringUtils.isNotEmpty(MMSSubscribeInfo.getConsumeThreadMax())) {
            properties.put(MmsClientConfig.CONSUMER.CONSUME_THREAD_MAX, MMSSubscribeInfo.getConsumeThreadMax());
        }
        if (StringUtils.isNotEmpty(MMSSubscribeInfo.getConsumeThreadMin())) {
            properties.put(MmsClientConfig.CONSUMER.CONSUME_THREAD_MIN, MMSSubscribeInfo.getConsumeThreadMin());
        }
        if (StringUtils.isNotEmpty(MMSSubscribeInfo.getOrderlyConsumePartitionParallelism())) {
            properties.put(MmsClientConfig.CONSUMER.ORDERLY_CONSUME_PARTITION_PARALLELISM, MMSSubscribeInfo.getOrderlyConsumePartitionParallelism());
        }
        if (StringUtils.isNotEmpty(MMSSubscribeInfo.getMaxBatchRecords())) {
            properties.put(MmsClientConfig.CONSUMER.MAX_BATCH_RECORDS, MMSSubscribeInfo.getMaxBatchRecords());
        }
        if (StringUtils.isNotEmpty(MMSSubscribeInfo.getIsOrderly())) {
            properties.put(MmsClientConfig.CONSUMER.CONSUME_ORDERLY, MMSSubscribeInfo.getIsOrderly());
        }
        if (StringUtils.isNotBlank(MMSSubscribeInfo.getConsumeTimeoutMs())) {
            properties.put(MmsClientConfig.CONSUMER.CONSUME_TIMEOUT_MS, MMSSubscribeInfo.getConsumeTimeoutMs());
        }
        if (StringUtils.isNotBlank(MMSSubscribeInfo.getMaxReconsumeTimes())) {
            properties.put(MmsClientConfig.CONSUMER.MAX_RECONSUME_TIMES, MMSSubscribeInfo.getMaxReconsumeTimes());
        }
        if (StringUtils.isNotBlank(MMSSubscribeInfo.getIsNewPush())) {
            properties.put(MmsClientConfig.CONSUMER.CONSUME_LITE_PUSH, MMSSubscribeInfo.getIsNewPush());
        }
        if (StringUtils.isNotBlank(MMSSubscribeInfo.getOrderlyConsumeThreadSize())) {
            properties.put(MmsClientConfig.CONSUMER.ORDERLY_CONSUME_THREAD_SIZE, MMSSubscribeInfo.getOrderlyConsumeThreadSize());
        }
        return properties;
    }

    public void disableSubscribe(String consumerGroup, String tag) {
        synchronized(this) {
            Set<String> consumerGroupSet = this.enabledConsumerGroupMap.keySet();
            if (!consumerGroupSet.contains(consumerGroup)) {
                throw FrameworkException.getInstance("该消费组[{}]不是通过模版启动，禁止停止", consumerGroup);
            } else {
                if (StringUtils.isEmpty(tag)) {
                    ConsumerFactory.shutdown(consumerGroup);
                    this.enabledConsumerGroupMap.remove(consumerGroup);
                } else {
                    Set<String> tagSet = this.enabledConsumerGroupMap.get(consumerGroup);
                    if (!tagSet.remove(tag)) {
                        throw FrameworkException.getInstance("该该标签[group:{},tag:{}]没有启动，禁止停止", consumerGroup, tag);
                    }
                    ConsumerFactory.shutdown(consumerGroup);
                    if (!tagSet.isEmpty()) {
                        Mms.subscribe(consumerGroup, tagSet, this.buildListener(consumerGroup), this.buildProperties(consumerGroup));
                        logger.info("tag标签已关闭，消费组已重启，ConsumerGroup={}, tag:{}", consumerGroup, tag);
                    } else {
                        this.enabledConsumerGroupMap.remove(consumerGroup);
                        logger.info("消费组已停止消费 ConsumerGroup={}, tag:{}", consumerGroup, tag);
                    }
                }
            }
        }
    }
}
