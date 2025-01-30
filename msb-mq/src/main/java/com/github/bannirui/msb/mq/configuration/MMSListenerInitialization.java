package com.github.bannirui.msb.mq.configuration;

import com.github.bannirui.msb.common.ex.FrameworkException;
import com.github.bannirui.msb.mq.annotation.MMSListener;
import com.github.bannirui.msb.mq.annotation.MMSListenerParameter;
import com.google.common.collect.Lists;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.Environment;

/**
 * {@link MMSListener}注解标识的监听器配置缓存起来.
 */
public class MMSListenerInitialization implements BeanPostProcessor, EnvironmentAware, PriorityOrdered {
    private static final Logger logger = LoggerFactory.getLogger(MMSListenerInitialization.class);

    private Map<String, MMSListenerProperties> mmsListenerPropertiesMap = new HashMap<>();

    public void setEnvironment(Environment environment) {
        // msb配置
        List<MMSListenerProperties> mmsListenerProperties = Binder.get(environment).bind("msb.mq.consumer", Bindable.listOf(MMSListenerProperties.class)).orElseGet(ArrayList::new);
        if(CollectionUtils.isNotEmpty(mmsListenerProperties)) {
            this.mmsListenerPropertiesMap = mmsListenerProperties.stream().filter((p) -> StringUtils.isNotBlank(p.getConsumerGroup())).collect(Collectors.toMap(MMSListenerProperties::getConsumerGroup, Function.identity()));
        }
    }

    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Method[] methods = bean.getClass().getMethods();
        // 找到mq的监听器
        for (Method method : methods) {
            if (!method.isAnnotationPresent(MMSListener.class)) continue;
            // 方法参数
            Parameter[] parameters = method.getParameters();
            if (parameters.length == 0) {
                throw FrameworkException.getInstance("MMSListener方法[{0}]参数为空", bean.getClass().getName() + "." + method.getName());
            }
            // 所有被注解表示的方法参数
            List<Map<String, Object>> paramList = new ArrayList<>();
            Arrays.stream(parameters).forEach((parameter) -> {
                // 监听器方法参数注解
                MMSListenerParameter listenerParameter = parameter.getAnnotation(MMSListenerParameter.class);
                if (listenerParameter != null) {
                    Map<String, Object> map = new HashMap<>();
                    // 映射mq的属性字段
                    map.put("name", listenerParameter.name().getValue());
                    // 序列化方式
                    map.put("serialize", listenerParameter.serialize().getValue());
                    // 方法参数类型
                    if (parameter.getType().equals(List.class)) {
                        try {
                            ParameterizedType parameterizedType = (ParameterizedType) parameter.getParameterizedType();
                            Class<?> clazz = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                            map.put("serializeType", clazz);
                        } catch (Exception e) {
                            logger.warn("该方法 [{}] List 类型参数 [{}] 未设置泛型类型", method.getName(), parameter.getName(), e);
                            map.put("serializeType", Object.class);
                        }
                    } else {
                        map.put("serializeType", parameter.getType());
                    }
                    paramList.add(map);
                }
            });
            // 监听器上注解指定的mq消费者信息
            MMSListener annotation = method.getAnnotation(MMSListener.class);
            String consumerGroup = annotation.consumerGroup();
            MMSListenerProperties zp = this.mmsListenerPropertiesMap.get(consumerGroup);
            boolean b = zp != null;
            String consumeThreadMax = b && StringUtils.isNotBlank(zp.getConsumeThreadMax()) ? zp.getConsumeThreadMax() : annotation.consumeThreadMax();
            String consumeThreadMin = b && StringUtils.isNotBlank(zp.getConsumeThreadMin()) ? zp.getConsumeThreadMin() : annotation.consumeThreadMin();
            String orderlyConsumePartitionParallelism = b && StringUtils.isNotBlank(zp.getOrderlyConsumePartitionParallelism()) ? zp.getOrderlyConsumePartitionParallelism() : annotation.orderlyConsumePartitionParallelism();
            String maxBatchRecords = b && StringUtils.isNotBlank(zp.getMaxBatchRecords()) ? zp.getMaxBatchRecords() : annotation.maxBatchRecords();
            String isOrderly = b && StringUtils.isNotBlank(zp.getIsOrderly()) ? zp.getIsOrderly() : annotation.isOrderly();
            String tags = b && StringUtils.isNotBlank(zp.getTags()) ? zp.getTags() : annotation.tags();
            String consumeTimeoutMs = b && StringUtils.isNotBlank(zp.getConsumeTimeoutMs()) ? zp.getConsumeTimeoutMs() : annotation.consumeTimeoutMs();
            String maxReconsumeTimes = b && StringUtils.isNotBlank(zp.getMaxReconsumeTimes()) ? zp.getMaxReconsumeTimes() : annotation.maxReconsumeTimes();
            String isNewPush = b && StringUtils.isNotBlank(zp.getIsNewPush()) ? zp.getIsNewPush() : annotation.isNewPush();
            String orderlyConsumeThreadSize = b && StringUtils.isNotBlank(zp.getOrderlyConsumeThreadSize()) ? zp.getOrderlyConsumeThreadSize() : annotation.orderlyConsumeThreadSize();
            MMSSubscribeInfo MMSSubscribeInfo = new MMSSubscribeInfo();
            MMSSubscribeInfo.setConsumeThreadMax(consumeThreadMax);
            MMSSubscribeInfo.setConsumeThreadMin(consumeThreadMin);
            MMSSubscribeInfo.setOrderlyConsumePartitionParallelism(orderlyConsumePartitionParallelism);
            MMSSubscribeInfo.setMaxBatchRecords(maxBatchRecords);
            MMSSubscribeInfo.setEasy(annotation.easy());
            MMSSubscribeInfo.setIsOrderly(isOrderly);
            MMSSubscribeInfo.setConsumeTimeoutMs(consumeTimeoutMs);
            MMSSubscribeInfo.setMaxReconsumeTimes(maxReconsumeTimes);
            MMSSubscribeInfo.setIsNewPush(isNewPush);
            MMSSubscribeInfo.setOrderlyConsumeThreadSize(orderlyConsumeThreadSize);
            Set<String> tagsSet = new HashSet<>();
            if ("*".equals(tags)) {
                if (MMSContext.checkTag(consumerGroup, tags)) {
                    MMSContext.getMmsConfMap().put(consumerGroup + "~" + tags, MMSContext.getMMSConf(consumerGroup, method, bean, paramList, tags));
                    tagsSet.add(tags);
                }
            } else {
                // tag 分隔符||
                List<String> tagList = Lists.newArrayList(tags.split("\\|\\|"));
                for (String tag : tagList) {
                    if(MMSContext.checkTag(consumerGroup, tags)) {
                        tagsSet.add(tag);
                        MMSContext.getMmsConfMap().put(consumerGroup + "~" + tag, MMSContext.getMMSConf(consumerGroup, method, bean, paramList, tag));
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(tagsSet)) {
                MMSSubscribeInfo.setTags(tagsSet);
                MMSContext.putConsumerInfo(consumerGroup, MMSSubscribeInfo);
            }
        }
        return bean;
    }

    public int getOrder() {
        return -2147483648;
    }
}