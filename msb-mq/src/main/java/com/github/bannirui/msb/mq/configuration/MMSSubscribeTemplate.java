package com.github.bannirui.msb.mq.configuration;

import com.github.bannirui.mms.client.Mms;
import com.github.bannirui.mms.client.config.MmsClientConfig;
import com.github.bannirui.mms.client.consumer.MessageListener;
import com.github.bannirui.msb.mq.sdk.MmsMsbImpl;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.springframework.beans.factory.DisposableBean;

/**
 * 消费者.
 */
public class MMSSubscribeTemplate implements DisposableBean {
    private MmsMsbImpl mmsMsb;

    /**
     * 有参数构造 {@link org.springframework.cglib.proxy.Enhancer}代理使用
     */
    public MMSSubscribeTemplate(MmsMsbImpl mmsMsb) {
        this.mmsMsb = mmsMsb;
    }

    public void subscribe(String consumerGroup, MessageListener listener) {
        Mms.subscribe(consumerGroup, listener);
    }

    public void subscribe(String consumerGroup, String tags, MessageListener listener) {
        this.mmsMsb.subscribe(consumerGroup, Sets.newHashSet(tags), listener);
    }

    public void subscribe(String consumerGroup, Set<String> tags, MessageListener listener) {
        this.mmsMsb.subscribe(consumerGroup, tags, listener);
    }

    /** @deprecated */
    @Deprecated
    public void subscribe(String consumerGroup, Set<String> tags, MessageListener listener, Properties properties) {
        this.mmsMsb.subscribe(consumerGroup, tags, listener, properties);
    }

    /**
     * @param listener {@link MMSMessageListenerImpl}代理对象
     */
    public void subscribe(String consumerGroup, Set<String> tags, MessageListener listener, Map<MmsClientConfig.CONSUMER, Object> properties) {
        this.mmsMsb.subscribe(consumerGroup, tags, listener, properties);
    }

    /** @deprecated */
    @Deprecated
    public void subscribe(String consumerGroup, MessageListener listener, Properties properties) {
        this.mmsMsb.subscribe(consumerGroup, Sets.newHashSet(), listener, properties);
    }

    public void subscribe(String consumerGroup, MessageListener listener, Map<MmsClientConfig.CONSUMER, Object> properties) {
        this.mmsMsb.subscribe(consumerGroup, Sets.newHashSet(), listener, properties);
    }

    public static boolean isWrapClass(Class<?> clz) {
        try {
            return ((Class<?>)clz.getField("TYPE").get(null)).isPrimitive();
        } catch (Exception var2) {
            return false;
        }
    }

    public void destroy() throws Exception {
        if (this.mmsMsb != null) {
            this.mmsMsb.stop();
        }
    }
}
