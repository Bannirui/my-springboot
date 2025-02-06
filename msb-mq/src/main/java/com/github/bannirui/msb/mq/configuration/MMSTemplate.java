package com.github.bannirui.msb.mq.configuration;

import com.alibaba.fastjson.JSON;
import com.github.bannirui.msb.ex.FrameworkException;
import com.github.bannirui.msb.mq.sdk.MmsMsbImpl;
import com.github.bannirui.msb.mq.sdk.common.SimpleMessage;
import com.github.bannirui.msb.mq.sdk.config.MmsClientConfig;
import com.github.bannirui.msb.mq.sdk.producer.SendResponse;
import com.github.bannirui.msb.mq.sdk.producer.MmsCallBack;
import com.google.common.collect.Lists;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import org.springframework.beans.factory.DisposableBean;

/**
 * 生产者.
 */
public class MMSTemplate implements DisposableBean {
    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    private MmsMsbImpl mmsMsb;
    private List<CompleteMessageListener> listeners = Lists.newArrayList();

    /**
     * 有参构造 Enhancer代理时使用
     */
    public MMSTemplate(MmsMsbImpl mmsMsb) {
        this.mmsMsb = mmsMsb;
    }

    public void addListener(CompleteMessageListener listener) {
        this.listeners.add(listener);
    }

    private void invokerListeners(SimpleMessage simpleMessage) {
        Optional.ofNullable(this.listeners).ifPresent((listeners) -> {
            listeners.forEach((listener) -> {
                listener.complete(simpleMessage);
            });
        });
    }

    public String send(String topic, Object obj) {
        return this.doSend(topic, null, null, obj, 0, null);
    }

    public String send(String topic, String keys, Object obj) {
        return this.doSend(topic, null, keys, obj, 0, null);
    }

    public String send(String topic, String tags, String keys, Object obj) {
        return this.doSend(topic, tags, keys, obj, 0, null);
    }

    public String send(String topic, String tags, String keys, Object obj, Map<MmsClientConfig.PRODUCER, Object> properties) {
        return this.doSend(topic, tags, keys, obj, 0, properties);
    }

    public String send(String topic, String tags, String keys, Object obj, int delayLevel, Map<MmsClientConfig.PRODUCER, Object> properties) {
        return this.doSend(topic, tags, keys, obj, delayLevel, properties);
    }

    /** @deprecated */
    @Deprecated
    public String send(String topic, String tags, String keys, Object obj, Properties properties) {
        SimpleMessage simpleMessage = new SimpleMessage();
        simpleMessage.setKey(keys);
        simpleMessage.setTags(tags);
        if (null != properties && null != properties.get("delayLevel")) {
            if (properties.get("delayLevel") instanceof Integer) {
                simpleMessage.setDelayLevel((Integer)properties.get("delayLevel"));
            }
            properties.remove("delayLevel");
        }
        if (isWrapClass(obj.getClass())) {
            simpleMessage.setPayload(obj.toString().getBytes(UTF_8));
        } else if (obj instanceof String) {
            simpleMessage.setPayload(((String)obj).getBytes(UTF_8));
        } else {
            simpleMessage.setPayload(JSON.toJSONString(obj).getBytes(UTF_8));
        }
        SendResponse sendResponse = null;
        this.invokerListeners(simpleMessage);
        try {
            sendResponse = this.mmsMsb.send(topic, simpleMessage, properties);
        } catch (Exception e) {
            throw FrameworkException.getInstance(e, "MQ发送消息错误{0}", simpleMessage);
        }
        if (SendResponse.SUCCESS.getCode() == sendResponse.getCode()) {
            return sendResponse.getMsgId();
        } else {
            String errMsg = sendResponse.getMsg();
            throw FrameworkException.getInstance("MQ发送消息异常{0}", errMsg);
        }
    }

    private String doSend(String topic, String tags, String keys, Object obj, int delayLevel, Map<MmsClientConfig.PRODUCER, Object> properties) {
        SimpleMessage simpleMessage = this.buildSimpleMessage(tags, keys, obj, delayLevel);
        this.invokerListeners(simpleMessage);
        SendResponse sendResponse;
        try {
            sendResponse = this.mmsMsb.send(topic, simpleMessage, properties);
        } catch (Exception e) {
            throw FrameworkException.getInstance(e, "MQ发送消息错误{0}", simpleMessage);
        }
        if (SendResponse.SUCCESS.getCode() == sendResponse.getCode()) {
            return sendResponse.getMsgId();
        } else {
            String errMsg = sendResponse.getMsg();
            throw FrameworkException.getInstance("MQ发送消息异常{0}", errMsg);
        }
    }

    public void asyncSend(String topic, Object obj, MmsCallBack callBack) {
        this.doAsyncSend(topic, null, null, obj, 0, null, callBack);
    }

    public void asyncSend(String topic, String keys, Object obj, MmsCallBack callBack) {
        this.doAsyncSend(topic, null, keys, obj, 0, null, callBack);
    }

    public void asyncSend(String topic, String tags, String keys, Object obj, MmsCallBack callBack) {
        this.doAsyncSend(topic, tags, keys, obj, 0, null, callBack);
    }

    public void asyncSend(String topic, String tags, String keys, Object obj, int delayLevel, Map<MmsClientConfig.PRODUCER, Object> properties, MmsCallBack callBack) {
        this.doAsyncSend(topic, tags, keys, obj, delayLevel, properties, callBack);
    }

    /** @deprecated */
    @Deprecated
    public void asyncSend(String topic, String tags, String keys, Object obj, Properties properties, MmsCallBack callBack) {
        SimpleMessage simpleMessage = new SimpleMessage();
        simpleMessage.setKey(keys);
        simpleMessage.setTags(tags);
        if (null != properties && null != properties.get("delayLevel")) {
            if (properties.get("delayLevel") instanceof Integer) {
                simpleMessage.setDelayLevel((Integer)properties.get("delayLevel"));
            }
            properties.remove("delayLevel");
        }
        if (isWrapClass(obj.getClass())) {
            simpleMessage.setPayload(obj.toString().getBytes(UTF_8));
        } else if (obj instanceof String) {
            simpleMessage.setPayload(((String)obj).getBytes(UTF_8));
        } else {
            simpleMessage.setPayload(JSON.toJSONString(obj).getBytes(UTF_8));
        }
        this.invokerListeners(simpleMessage);
        try {
            this.mmsMsb.asyncSend(topic, simpleMessage, properties, callBack);
        } catch (Exception e) {
            throw FrameworkException.getInstance(e, "MQ发送消息错误{0}", simpleMessage);
        }
    }

    private void doAsyncSend(String topic, String tags, String keys, Object obj, int delayLevel, Map<MmsClientConfig.PRODUCER, Object> properties, MmsCallBack callBack) {
        SimpleMessage simpleMessage = this.buildSimpleMessage(tags, keys, obj, delayLevel);
        this.invokerListeners(simpleMessage);
        try {
            this.mmsMsb.asyncSend(topic, simpleMessage, properties, callBack);
        } catch (Exception e) {
            throw FrameworkException.getInstance(e, "MQ发送消息错误{0}", simpleMessage);
        }
    }

    public void onewaySend(String topic, Object obj) {
        this.doOnewaySend(topic, null, null, obj);
    }

    public void onewaySend(String topic, String keys, Object obj) {
        this.doOnewaySend(topic, null, keys, obj);
    }

    public void doOnewaySend(String topic, String tags, String keys, Object obj) {
        SimpleMessage simpleMessage = new SimpleMessage();
        simpleMessage.setKey(keys);
        simpleMessage.setTags(tags);
        if (isWrapClass(obj.getClass())) {
            simpleMessage.setPayload(obj.toString().getBytes(UTF_8));
        } else if (obj instanceof String) {
            simpleMessage.setPayload(((String)obj).getBytes(UTF_8));
        } else {
            simpleMessage.setPayload(JSON.toJSONString(obj).getBytes(UTF_8));
        }
        this.invokerListeners(simpleMessage);
        try {
            this.mmsMsb.onewaySend(topic, simpleMessage);
        } catch (Exception e) {
            throw FrameworkException.getInstance(e, "MQ发送消息错误{0}", simpleMessage);
        }
    }

    private SimpleMessage buildSimpleMessage(String tags, String keys, Object obj, int delayLevel) {
        SimpleMessage simpleMessage = new SimpleMessage();
        simpleMessage.setKey(keys);
        simpleMessage.setTags(tags);
        if (delayLevel > 0) {
            simpleMessage.setDelayLevel(delayLevel);
        }
        if (isWrapClass(obj.getClass())) {
            simpleMessage.setPayload(obj.toString().getBytes(UTF_8));
        } else if (obj instanceof String) {
            simpleMessage.setPayload(((String)obj).getBytes(UTF_8));
        } else {
            simpleMessage.setPayload(JSON.toJSONString(obj).getBytes(UTF_8));
        }
        return simpleMessage;
    }

    public static boolean isWrapClass(Class clz) {
        try {
            return ((Class<?>)clz.getField("TYPE").get(null)).isPrimitive();
        } catch (Exception e) {
            return false;
        }
    }

    public void destroy() throws Exception {
        this.stop();
    }

    public void stop() {
        if (this.mmsMsb != null) {
            this.mmsMsb.stop();
        }
    }
}
