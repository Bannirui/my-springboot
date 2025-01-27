package com.github.bannirui.msb.mq.configuration;

import com.github.bannirui.msb.common.ex.FrameworkException;
import com.github.bannirui.msb.mq.annotation.MMSBatchListener;
import com.github.bannirui.msb.mq.annotation.MMSListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MMSContext {
    private static final Logger logger = LoggerFactory.getLogger(MMSContext.class);
    /**
     * {@link MMSListener}指定的消费者配置信息
     * <ul>key consumer group</ul>
     * <ul>val 消费者配置</ul>
     */
    private static Map<String, MMSSubscribeInfo> consumerInfo = new ConcurrentHashMap<>();
    /**
     * {@link MMSBatchListener}指定的消费者配置信息
     */
    private static Map<String, MMSSubscribeInfo> batchConsumerInfo = new ConcurrentHashMap<>();
    /**
     * {@link MMSTemplate}指定的消费者配置信息
     */
    private static Map<String, MMSSubscribeInfo> templateConsumerInfo = new ConcurrentHashMap<>();
    /**
     * <ul>
     *     <li>key consumer group+tag</li>
     *     <li>val 监听器配置 哪个Bean实例哪个方法什么参数 用于回调</li>
     * </ul>
     * <ul>
     *     <li>当tag是*时 key就是group~*</li>
     *     <li>当tag有多个时 缓存的维度就是tag 每个tag都缓存了key是group~tag</li>
     * </ul>
     */
    private static Map<String, MMSConf> mmsConfMap = new ConcurrentHashMap<>();
    private static final Map<String, MMSConf> templateMmsConfMap = new ConcurrentHashMap<>();

    public static Map<String, MMSConf> getMmsConfMap() {
        return mmsConfMap;
    }

    public static Map<String, MMSConf> getTemplateMmsConfMap() {
        return templateMmsConfMap;
    }

    public static void setMmsConfMap(Map<String, MMSConf> mmsConfMap) {
        MMSContext.mmsConfMap = mmsConfMap;
    }

    /**
     * 封装mq监听器信息 用于回调
     * @param consumerGroup mq consumer group
     * @param method {@link MMSListener}注解标识的方法
     * @param obj {@link MMSListener}注解标识的是方法 该方法所在的实例 Spring容器的Bean
     * @param params 方法中映射mq属性的参数
     * @param tag 监听的消息tag
     */
    public static MMSConf getMMSConf(String consumerGroup, Method method, Object obj, List<Map<String, Object>> params, String tag) {
        MMSConf MMSConf = new MMSConf();
        MMSConf.setConsumerGroup(consumerGroup);
        MMSConf.setMethod(method);
        MMSConf.setObj(obj);
        MMSConf.setParams(params);
        MMSConf.setTag(tag);
        return MMSConf;
    }

    /**
     * 以consumer group为维度 消费者信息缓存起来
     * @param consumerGroup mq consumer group
     * @param info 消费者配置信息
     */
    public static void putConsumerInfo(String consumerGroup, MMSSubscribeInfo info) {
        consumerInfo.compute(consumerGroup, (k, v) -> {
            if (v == null) {
                return info;
            } else {
                info.getTags().forEach((tag) -> v.getTags().add(tag));
                v.setConsumeThreadMax(info.getConsumeThreadMax());
                v.setConsumeThreadMin(info.getConsumeThreadMin());
                v.setMaxBatchRecords(info.getMaxBatchRecords());
                v.setIsOrderly(info.getIsOrderly());
                v.setIsNewPush(info.getIsNewPush());
                return v;
            }
        });
    }

    public static void putBatchConsumerInfo(String consumerGroup, MMSSubscribeInfo info) {
        if (batchConsumerInfo.containsKey(consumerGroup)) {
            MMSSubscribeInfo MMSSubscribeInfo = batchConsumerInfo.get(consumerGroup);
            info.getTags().forEach((tag) -> {
                MMSSubscribeInfo.getTags().add(tag);
            });
            MMSSubscribeInfo.setConsumeThreadMax(info.getConsumeThreadMax());
            MMSSubscribeInfo.setConsumeThreadMin(info.getConsumeThreadMin());
            MMSSubscribeInfo.setMaxBatchRecords(info.getMaxBatchRecords());
            MMSSubscribeInfo.setIsOrderly(info.getIsOrderly());
            MMSSubscribeInfo.setIsNewPush(info.getIsNewPush());
        } else {
            batchConsumerInfo.put(consumerGroup, info);
        }
    }

    public static void putTemplateConsumerInfo(String consumerGroup, MMSSubscribeInfo info) {
        if (templateConsumerInfo.containsKey(consumerGroup)) {
            MMSSubscribeInfo MMSSubscribeInfo = templateConsumerInfo.get(consumerGroup);
            info.getTags().forEach((tag) -> {
                MMSSubscribeInfo.getTags().add(tag);
            });
            MMSSubscribeInfo.setConsumeThreadMax(info.getConsumeThreadMax());
            MMSSubscribeInfo.setConsumeThreadMin(info.getConsumeThreadMin());
            MMSSubscribeInfo.setMaxBatchRecords(info.getMaxBatchRecords());
            MMSSubscribeInfo.setIsOrderly(info.getIsOrderly());
            MMSSubscribeInfo.setIsNewPush(info.getIsNewPush());
        } else {
            templateConsumerInfo.put(consumerGroup, info);
        }
    }

    /**
     * 准备往监听器的消费者配置中增加tag 前置校验tag是否合法
     * <ul>
     *     <li>*表示监听所有tag *和具体tag互斥 不能既配置* 又配置具体tag</li>
     *     <li>tag不能重复</li>
     * </ul>
     * @param tag 待检查的tag
     * @return <t>TRUE</t>表示tag合法 可以添加到配置中 <t>FALSE</t>表示tag不合法没有通过校验
     */
    public static boolean checkTag(String consumerGroup, String tag) {
        Map<String, MMSSubscribeInfo> allConsumerInfo = new ConcurrentHashMap<>();
        allConsumerInfo.putAll(consumerInfo);
        allConsumerInfo.putAll(batchConsumerInfo);
        if (allConsumerInfo.containsKey(consumerGroup)) {
            if ("*".equals(tag) && !allConsumerInfo.get(consumerGroup).getTags().isEmpty()) {
                throw FrameworkException.getInstance("MMS订阅的消息Tag不合法，不能在consumerGroup=" + consumerGroup + "设置了具体的tag又设置*");
            }
            if (allConsumerInfo.get(consumerGroup).getTags().contains(tag)) {
                throw FrameworkException.getInstance("MMS订阅的消息Tag不合法，不能在consumerGroup=" + consumerGroup + "设置相同tag=" + tag + "的监听");
            }
        }
        return true;
    }

    public static void checkTagForTemplateId(String templateId) {
        if (templateConsumerInfo.containsKey(templateId)) {
            throw FrameworkException.getInstance("模版消费组[{}]不可重复订阅", templateId);
        }
    }

    public static Map<String, MMSSubscribeInfo> getConsumerInfo() {
        return consumerInfo;
    }

    public static void setConsumerInfo(Map<String, MMSSubscribeInfo> consumerInfo) {
        MMSContext.consumerInfo = consumerInfo;
    }

    public static Map<String, MMSSubscribeInfo> getTemplateConsumerInfo() {
        return templateConsumerInfo;
    }

    public static void setTemplateConsumerInfo(Map<String, MMSSubscribeInfo> templateConsumerInfo) {
        MMSContext.templateConsumerInfo = templateConsumerInfo;
    }

    public static Map<String, MMSSubscribeInfo> getBatchConsumerInfo() {
        return batchConsumerInfo;
    }

    public static void setBatchConsumerInfo(Map<String, MMSSubscribeInfo> batchConsumerInfo) {
        MMSContext.batchConsumerInfo = batchConsumerInfo;
    }
}
