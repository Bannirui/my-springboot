package com.github.bannirui.msb.mq.configuration;

import com.github.bannirui.msb.common.ex.FrameworkException;
import com.github.bannirui.msb.mq.annotation.MMSListenerParameter;
import com.github.bannirui.msb.mq.annotation.MMSListenerTemplate;
import com.github.bannirui.msb.mq.enums.Serialize;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
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

public class MMSListenerTemplateInitialization implements BeanPostProcessor, EnvironmentAware, PriorityOrdered {
    private static final Logger LOGGER = LoggerFactory.getLogger(MMSListenerTemplateInitialization.class);
    private Map<String, MMSListenerProperties> mmsListenerPropertiesMap;

    public void setEnvironment(Environment environment) {
        // msb配置
        List<MMSListenerProperties>
            MMSListenerPropertiesList = Binder.get(environment).bind("msb.mq.consumer", Bindable.listOf(MMSListenerProperties.class)).orElseGet(ArrayList::new);
        this.mmsListenerPropertiesMap = MMSListenerPropertiesList.stream().filter((p) -> StringUtils.isNotBlank(p.getTemplateName())).collect(Collectors.toMap(
            MMSListenerProperties::getTemplateName, Function.identity()));
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Method[] methods = bean.getClass().getMethods();
        Arrays.stream(methods).forEach((method) -> {
            if (method.isAnnotationPresent(MMSListenerTemplate.class)) {
                Parameter[] parameters = method.getParameters();
                if (parameters.length <= 0) {
                    throw FrameworkException.getInstance("MMSListenerTemplate方法[{0}]入参为空", bean.getClass().getName() + "." + method.getName());
                }
                List<Map<String, Object>> paramList = new ArrayList<>();
                Arrays.stream(parameters).forEach((parameter) -> {
                    MMSListenerParameter param = parameter.getAnnotation(MMSListenerParameter.class);
                    Map<String, Object> map = new HashMap<>();
                    if (param != null) {
                        map.put("name", param.name());
                        map.put("serialize", param.serialize());
                        if (parameter.getType().equals(List.class)) {
                            try {
                                ParameterizedType parameterizedType = (ParameterizedType)parameter.getParameterizedType();
                                Class<?> clazz = (Class<?>)parameterizedType.getActualTypeArguments()[0];
                                map.put("serializeType", clazz);
                            } catch (Exception e) {
                                LOGGER.warn("该方法 [{}] List 类型参数 [{}] 未设置泛型类型", method.getName(), parameter.getName(), e);
                                map.put("serializeType", Object.class);
                            }
                        } else {
                            map.put("serializeType", parameter.getType());
                        }
                    } else {
                        map.put("name", parameter.getName());
                        map.put("serialize", Serialize.STRING.getValue());
                    }
                    paramList.add(map);
                });
                MMSListenerTemplate annotation = method.getAnnotation(MMSListenerTemplate.class);
                String templateName = annotation.templateName();
                MMSListenerProperties zp = this.mmsListenerPropertiesMap.get(templateName);
                boolean b = zp != null;
                String consumeThreadMax = b && StringUtils.isNotBlank(zp.getConsumeThreadMax()) ? zp.getConsumeThreadMax() : annotation.consumeThreadMax();
                String consumeThreadMin = b && StringUtils.isNotBlank(zp.getConsumeThreadMin()) ? zp.getConsumeThreadMin() : annotation.consumeThreadMin();
                String orderlyConsumePartitionParallelism = b && StringUtils.isNotBlank(zp.getOrderlyConsumePartitionParallelism()) ? zp.getOrderlyConsumePartitionParallelism() : annotation.orderlyConsumePartitionParallelism();
                String maxBatchRecords = b && StringUtils.isNotBlank(zp.getMaxBatchRecords()) ? zp.getMaxBatchRecords() : annotation.maxBatchRecords();
                String isOrderly = b && StringUtils.isNotBlank(zp.getIsOrderly()) ? zp.getIsOrderly() : annotation.isOrderly();
                String consumeTimeoutMs = b && StringUtils.isNotBlank(zp.getConsumeTimeoutMs()) ? zp.getConsumeTimeoutMs() : annotation.consumeTimeoutMs();
                String maxReconsumeTimes = b && StringUtils.isNotBlank(zp.getMaxReconsumeTimes()) ? zp.getMaxReconsumeTimes() : annotation.maxReconsumeTimes();
                String isNewPush = b && StringUtils.isNotBlank(zp.getIsNewPush()) ? zp.getIsNewPush() : annotation.isNewPush();
                String orderlyConsumeThreadSize = b && StringUtils.isNotBlank(zp.getOrderlyConsumeThreadSize()) ? zp.getOrderlyConsumeThreadSize() : annotation.orderlyConsumeThreadSize();
                MMSContext.checkTagForTemplateId(templateName);
                MMSSubscribeInfo MMSSubscribeInfo = new MMSSubscribeInfo();
                MMSSubscribeInfo.setConsumeThreadMax(consumeThreadMax);
                MMSSubscribeInfo.setConsumeThreadMin(consumeThreadMin);
                MMSSubscribeInfo.setOrderlyConsumePartitionParallelism(orderlyConsumePartitionParallelism);
                MMSSubscribeInfo.setMaxBatchRecords(maxBatchRecords);
                MMSSubscribeInfo.setIsOrderly(isOrderly);
                MMSSubscribeInfo.setEasy(annotation.easy());
                MMSSubscribeInfo.setConsumeTimeoutMs(consumeTimeoutMs);
                MMSSubscribeInfo.setMaxReconsumeTimes(maxReconsumeTimes);
                MMSSubscribeInfo.setIsNewPush(isNewPush);
                MMSSubscribeInfo.setOrderlyConsumeThreadSize(orderlyConsumeThreadSize);
                MMSContext.getTemplateMmsConfMap().put(templateName, MMSContext.getMMSConf(templateName, method, bean, paramList, (String)null));
                MMSContext.putTemplateConsumerInfo(templateName, MMSSubscribeInfo);
            }
        });
        return bean;
    }

    public int getOrder() {
        return -2147483648;
    }
}
