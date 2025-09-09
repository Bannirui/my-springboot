package com.github.bannirui.msb.mq.configuration;

import com.github.bannirui.msb.ex.FrameworkException;
import com.github.bannirui.msb.mq.annotation.MMSListener;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MMSContext {
    /**
     * {@link MMSListener}注解指定的消费者配置信息
     * <ul>key consumer group</ul>
     * <ul>val 消费者配置</ul>
     */
    private static Map<String, SubscribeInfo> consumerInfo = new ConcurrentHashMap<>();
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
    private static Map<String, Conf> confMap = new ConcurrentHashMap<>();
    private static final Map<String, Conf> templateConfMap = new ConcurrentHashMap<>();

    public static Map<String, Conf> getConfMap() {
        return confMap;
    }

    public static Map<String, Conf> getTemplateConfMap() {
        return templateConfMap;
    }

    public static void setConfMap(Map<String, Conf> confMap) {
        MMSContext.confMap = confMap;
    }

    /**
     * 封装mq监听器信息 用于回调
     * @param consumerGroup mq consumer group
     * @param method {@link MMSListener}注解标识的方法
     * @param obj {@link MMSListener}注解标识的是方法 该方法所在的实例 Spring容器的Bean
     * @param params 方法中映射mq属性的参数
     * @param tag 监听的消息tag
     */
    public static Conf getConf(String consumerGroup, Method method, Object obj, List<Map<String, Object>> params, String tag) {
        Conf conf = new Conf();
        conf.setConsumerGroup(consumerGroup);
        conf.setMethod(method);
        conf.setObj(obj);
        conf.setParams(params);
        conf.setTag(tag);
        return conf;
    }

    /**
     * 以consumer group为维度 消费者信息缓存起来
     * @param consumerGroup mq consumer group
     * @param info 消费者配置信息
     */
    public static void putConsumerInfo(String consumerGroup, SubscribeInfo info) {
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
        Map<String, SubscribeInfo> allConsumerInfo = new ConcurrentHashMap<>();
        allConsumerInfo.putAll(consumerInfo);
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

    public static Map<String, SubscribeInfo> getConsumerInfo() {
        return consumerInfo;
    }

    public static void setConsumerInfo(Map<String, SubscribeInfo> consumerInfo) {
        MMSContext.consumerInfo = consumerInfo;
    }
}
