package com.github.bannirui.msb.mq.configuration;

import com.alibaba.fastjson.util.ParameterizedTypeImpl;
import com.github.bannirui.msb.common.ex.FrameworkException;
import com.github.bannirui.msb.mq.annotation.MMSBatchListener;
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

public class MMSBatchListenerInitialization implements BeanPostProcessor, EnvironmentAware, PriorityOrdered {
    private static final Logger logger = LoggerFactory.getLogger(MMSBatchListenerInitialization.class);

    private Map<String, MMSListenerProperties> mmsListenerPropertiesMap;

    public void setEnvironment(Environment environment) {
        List<MMSListenerProperties>
            MMSListenerPropertiesList = Binder.get(environment).bind("msb.mq.consumer", Bindable.listOf(MMSListenerProperties.class)).orElseGet(ArrayList::new);
        this.mmsListenerPropertiesMap = MMSListenerPropertiesList.stream().filter((p) -> StringUtils.isNotBlank(p.getConsumerGroup())).collect(Collectors.toMap(
            MMSListenerProperties::getConsumerGroup, Function.identity()));
    }

    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Method[] methods = bean.getClass().getMethods();
        Arrays.stream(methods).forEach((method) -> {
            if (method.isAnnotationPresent(MMSBatchListener.class)) {
                Parameter[] parameters = method.getParameters();
                if (parameters.length != 1) {
                    throw FrameworkException.getInstance("MMSBatchListener 方法入参数量不正确", bean.getClass().getName() + "." + method.getName());
                }
                Parameter parameter = parameters[0];
                if (!parameter.getType().equals(List.class)) {
                    throw FrameworkException.getInstance("MMSBatchListener 方法入参类型错误，需要使用 List.class 类型接收", bean.getClass().getName() + "." + method.getName());
                }
                ParameterizedType listType = (ParameterizedType)parameter.getParameterizedType();
                ParameterizedTypeImpl messageWrapType = (ParameterizedTypeImpl)listType.getActualTypeArguments()[0];
                Class<?> messageWrapClazz = (Class<?>) messageWrapType.getRawType();
                if (!messageWrapClazz.equals(MessageWrapper.class)) {
                    throw FrameworkException.getInstance("MMSBatchListener 方法集合参数类型错误，需要使用 MessageWrap.class 类型", bean.getClass().getName() + "." + method.getName());
                }
                Class<?> bodyClazz = (Class<?>) messageWrapType.getActualTypeArguments()[0];
                List<Map<String, Object>> paramList = new ArrayList<>();
                Map<String, Object> map = new HashMap<>();
                map.put("bodyType", bodyClazz);
                paramList.add(map);
                MMSBatchListener annotation = method.getAnnotation(MMSBatchListener.class);
                String consumerGroup = annotation.consumerGroup();
                MMSListenerProperties zp = this.mmsListenerPropertiesMap.get(consumerGroup);
                boolean b = zp != null;
                String consumeThreadMax = b && StringUtils.isNotBlank(zp.getConsumeThreadMax()) ? zp.getConsumeThreadMax() : Integer.toString(annotation.consumeThreadMax());
                String consumeThreadMin = b && StringUtils.isNotBlank(zp.getConsumeThreadMin()) ? zp.getConsumeThreadMin() : Integer.toString(annotation.consumeThreadMin());
                String orderlyConsumePartitionParallelism = b && StringUtils.isNotBlank(zp.getOrderlyConsumePartitionParallelism()) ? zp.getOrderlyConsumePartitionParallelism() : Integer.toString(annotation.orderlyConsumePartitionParallelism());
                String maxBatchRecords = b && StringUtils.isNotBlank(zp.getMaxBatchRecords()) ? zp.getMaxBatchRecords() : Integer.toString(annotation.maxBatchRecords());
                String isOrderly = b && StringUtils.isNotBlank(zp.getIsOrderly()) ? zp.getIsOrderly() : Boolean.toString(annotation.isOrderly());
                String tags = b && StringUtils.isNotBlank(zp.getTags()) ? zp.getTags() : annotation.tags();
                String consumeTimeoutMs = b && StringUtils.isNotBlank(zp.getConsumeTimeoutMs()) ? zp.getConsumeTimeoutMs() : Integer.toString(annotation.consumeTimeoutMs());
                String maxReconsumeTimes = b && StringUtils.isNotBlank(zp.getMaxReconsumeTimes()) ? zp.getMaxReconsumeTimes() : Integer.toString(annotation.maxReconsumeTimes());
                String consumeBatchSize = b && StringUtils.isNotBlank(zp.getConsumeBatchSize()) ? zp.getConsumeBatchSize() : Integer.toString(annotation.consumeBatchSize());
                String isNewPush = b && StringUtils.isNotBlank(zp.getIsNewPush()) ? zp.getIsNewPush() : annotation.isNewPush();
                MMSSubscribeInfo MMSSubscribeInfo = new MMSSubscribeInfo();
                MMSSubscribeInfo.setConsumeThreadMax(consumeThreadMax);
                MMSSubscribeInfo.setConsumeThreadMin(consumeThreadMin);
                MMSSubscribeInfo.setOrderlyConsumePartitionParallelism(orderlyConsumePartitionParallelism);
                MMSSubscribeInfo.setMaxBatchRecords(maxBatchRecords);
                MMSSubscribeInfo.setIsOrderly(isOrderly);
                MMSSubscribeInfo.setConsumeTimeoutMs(consumeTimeoutMs);
                MMSSubscribeInfo.setMaxReconsumeTimes(maxReconsumeTimes);
                MMSSubscribeInfo.setConsumeBatchSize(consumeBatchSize);
                MMSSubscribeInfo.setIsNewPush(isNewPush);
                // 合法的tag
                Set<String> tagsSet = new HashSet<>();
                if ("*".equals(tags)) {
                    if (MMSContext.checkTag(consumerGroup, tags)) {
                        // tag合法
                        MMSContext.getMmsConfMap().put(consumerGroup + "~" + tags, MMSContext.getMMSConf(consumerGroup, method, bean, paramList, tags));
                        tagsSet.add(tags);
                    }
                } else {
                    List<String> tagList = Lists.newArrayList(tags.split("\\|\\|"));
                    for (String tag : tagList) {
                        if (MMSContext.checkTag(consumerGroup, tags)) {
                            tagsSet.add(tag);
                            MMSContext.getMmsConfMap().put(consumerGroup + "~" + tag, MMSContext.getMMSConf(consumerGroup, method, bean, paramList, tag));
                        }
                    }
                }
                if (CollectionUtils.isNotEmpty(tagsSet)) {
                    MMSSubscribeInfo.setTags(tagsSet);
                    MMSContext.putBatchConsumerInfo(consumerGroup, MMSSubscribeInfo);
                }
            }
        });
        return bean;
    }

    public int getOrder() {
        return -2147483648;
    }
}
