package com.github.bannirui.msb.mq.sdk.producer;

import com.github.bannirui.msb.mq.sdk.common.SLA;
import com.github.bannirui.msb.mq.sdk.common.MmsEnv;
import com.github.bannirui.msb.mq.sdk.common.MmsException;
import com.github.bannirui.msb.mq.sdk.common.MmsMessage;
import com.github.bannirui.msb.mq.sdk.config.MmsClientConfig;
import com.github.bannirui.msb.mq.sdk.consumer.MsgConsumedStatus;
import com.github.bannirui.msb.mq.sdk.crypto.MMSCryptoManager;
import com.github.bannirui.msb.mq.sdk.metadata.MmsMetadata;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.remoting.exception.RemotingTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RocketmqProducerProxy extends MmsProducerProxy {
    private static final Logger logger = LoggerFactory.getLogger(RocketmqProducerProxy.class);
    private static final MessageQueueSelector hashSelector;
    DefaultMQProducer producer;

    static {
        hashSelector = (mqs, msg, arg) -> {
            int id = msg.getKeys().hashCode() % mqs.size();
            return id < 0 ? (MessageQueue) mqs.get(-id) : (MessageQueue) mqs.get(id);
        };
    }

    public RocketmqProducerProxy(MmsMetadata metadata, SLA sla, String name) {
        super(metadata, sla, name);
        this.instanceName = name;
        this.start();
    }

    public RocketmqProducerProxy(MmsMetadata metadata, SLA sla, String name, Properties properties) {
        super(metadata, sla, name, properties);
        this.instanceName = name;
        this.start();
    }

    public void startProducer() {
        this.producer = new DefaultMQProducer("mms_" + System.currentTimeMillis());
        long now = System.currentTimeMillis();
        if (this.metadata.isGatedLaunch()) {
            this.producer.setNamesrvAddr(this.metadata.getGatedCluster().getBootAddr());
            this.producer.setClientIP("producer-client-id-" + this.metadata.getGatedCluster().getClusterName() + "-" + MmsEnv.MMS_IP + "-" + now);
        } else {
            this.producer.setNamesrvAddr(this.metadata.getClusterMetadata().getBootAddr());
            this.producer.setClientIP("producer-client-id-" + this.metadata.getClusterMetadata().getClusterName() + "-" + MmsEnv.MMS_IP + "-" + now);
        }
        int retries = 2;
        int timeout = 3000;
        if (this.customizedProperties != null) {
            if (this.customizedProperties.containsKey(MmsClientConfig.PRODUCER.RETRIES.getKey())) {
                retries = Integer.parseInt(String.valueOf(this.customizedProperties.get(MmsClientConfig.PRODUCER.RETRIES.getKey())));
            }
            if (this.customizedProperties.containsKey(MmsClientConfig.PRODUCER.SEND_TIMEOUT_MS.getKey())) {
                timeout = Integer.parseInt(String.valueOf(this.customizedProperties.get(MmsClientConfig.PRODUCER.SEND_TIMEOUT_MS.getKey())));
            }
        }
        this.producer.setRetryTimesWhenSendFailed(retries);
        this.producer.setRetryTimesWhenSendAsyncFailed(retries);
        this.producer.setSendMsgTimeout(timeout);
        this.producer.setVipChannelEnabled(false);
        ResetTimeoutAndRetries resetTimeoutAndRetries = super.resetTimeoutAndRetries(timeout, retries);
        if (resetTimeoutAndRetries != null) {
            this.producer.setSendMsgTimeout(resetTimeoutAndRetries.getResetTimeout());
            this.producer.setRetryTimesWhenSendFailed(resetTimeoutAndRetries.getResetRetries());
            this.producer.setRetryTimesWhenSendAsyncFailed(resetTimeoutAndRetries.getResetRetries());
        }
        try {
            this.producer.start();
        } catch (MQClientException e) {
            logger.error("producer {} start failed", this.metadata.getName(), e);
            throw MmsException.PRODUCER_START_EXCEPTION;
        }
    }

    public void shutdownProducer() {
        this.producer.shutdown();
    }

    public SendResponse syncSend(MmsMessage mmsMessage) {
        if (!this.running) {
            return SendResponse.FAILURE_NOTRUNNING;
        }
        boolean succeed = false;
        SendResponse ans;
        try {
            Message message = this.buildMessage(mmsMessage);
            if (mmsMessage.getDelayLevel() >= 1 && Arrays.stream(MsgConsumedStatus.values()).anyMatch((l) ->
                l.getLevel() == mmsMessage.getDelayLevel())) {
                message.setDelayTimeLevel(mmsMessage.getDelayLevel());
            }
            long startTime = System.currentTimeMillis();
            SendResult send;
            if (StringUtils.isEmpty(message.getKeys())) {
                send = this.producer.send(message);
            } else {
                send = this.producer.send(message, hashSelector, message.getKeys());
            }
            if (send.getSendStatus().equals(SendStatus.SEND_OK)) {
                long duration = System.currentTimeMillis() - startTime;
                this.mmsMetrics.sendCostRate().update(duration, TimeUnit.MILLISECONDS);
                succeed = true;
                this.mmsMetrics.getDistribution().markTime(duration);
                return SendResponse.buildSuccessResult(send.getQueueOffset(), send.getOffsetMsgId(), send.getMessageQueue().getTopic(), send.getMessageQueue().getQueueId());
            }
            if (!send.getSendStatus().equals(SendStatus.FLUSH_DISK_TIMEOUT) && !send.getSendStatus().equals(SendStatus.FLUSH_SLAVE_TIMEOUT)) {
                this.mmsMetrics.messageFailureRate().mark();
                logger.error("syncSend topic {} failed slave not exist ", this.metadata.getName());
                return SendResponse.FAILURE_TIMEOUT;
            }
            this.mmsMetrics.messageFailureRate().mark();
            logger.error("syncSend topic {} timeout for {} ", this.metadata.getName(), send.getSendStatus().name());
            return SendResponse.FAILURE_TIMEOUT;
        } catch (MQClientException e) {
            logger.error("send failed for ", e);
            ans = SendResponse.buildErrorResult("syncSend message MQClientException: " + e.getMessage());
            return ans;
        } catch (RemotingTimeoutException e) {
            logger.error("send failed for ", e);
            ans = SendResponse.FAILURE_TIMEOUT;
        } catch (RemotingException e) {
            logger.error("send failed for ", e);
            ans = SendResponse.buildErrorResult("syncSend message RemotingException: " + e.getMessage());
            return ans;
        } catch (MQBrokerException e) {
            logger.error("send failed for ", e);
            ans = SendResponse.buildErrorResult("syncSend message MQBrokerException: " + e.getMessage());
            return ans;
        } catch (InterruptedException e) {
            logger.error("send failed for ", e);
            logger.error("produce syncSend and wait interuptted", e);
            ans = SendResponse.FAILURE_INTERUPRION;
            return ans;
        } finally {
            if (succeed) {
                this.mmsMetrics.messageSuccessRate().mark();
            } else {
                this.mmsMetrics.messageFailureRate().mark();
            }
        }
        return ans;
    }

    public void asyncSend(MmsMessage mmsMessage, MmsCallBack mmsCallBack) {
        Message message = this.buildMessage(mmsMessage);
        if (mmsMessage.getDelayLevel() >= 1 && Arrays.stream(MsgConsumedStatus.values()).anyMatch((l) -> l.getLevel() == mmsMessage.getDelayLevel())) {
            message.setDelayTimeLevel(mmsMessage.getDelayLevel());
        }
        final long startTime = System.currentTimeMillis();
        this.mmsMetrics.msgBody().markSize((long) mmsMessage.getPayload().length);
        try {
            SendCallback sendCallback = new SendCallback() {
                public void onSuccess(SendResult send) {
                    long duration = System.currentTimeMillis() - startTime;
                    RocketmqProducerProxy.this.mmsMetrics.sendCostRate().update(duration, TimeUnit.MILLISECONDS);
                    RocketmqProducerProxy.this.mmsMetrics.messageSuccessRate().mark();
                    RocketmqProducerProxy.this.mmsMetrics.getDistribution().markTime(duration);
                    mmsCallBack.onResult(SendResponse.buildSuccessResult(send.getQueueOffset(), send.getOffsetMsgId(), send.getMessageQueue().getTopic(), send.getMessageQueue().getQueueId()));
                }

                public void onException(Throwable e) {
                    RocketmqProducerProxy.this.mmsMetrics.messageFailureRate().mark();
                    RocketmqProducerProxy.logger.error("aysnc send failed for ", e);
                    mmsCallBack.onException(e);
                }
            };
            if (StringUtils.isEmpty(message.getKeys())) {
                this.producer.send(message, sendCallback);
            } else {
                this.producer.send(message, hashSelector, message.getKeys(), sendCallback);
            }
        } catch (RemotingException | InterruptedException | MQClientException e) {
            logger.error("aysnc send failed for ", e);
        }
    }

    public void oneway(MmsMessage mmsMessage) {
        Message message = this.buildMessage(mmsMessage);
        message.setFlag(0);
        message.setWaitStoreMsgOK(false);
        this.mmsMetrics.msgBody().markSize((long) mmsMessage.getPayload().length);
        try {
            this.producer.send(message);
        } catch (RemotingException | MQBrokerException | InterruptedException | MQClientException e) {
            logger.warn("exception was ignored for oneway", e);
        }
    }

    private Message buildMessage(MmsMessage mmsMessage) {
        Map<String, String> properties = new HashMap<>();
        if (mmsMessage.getProperties() != null && !mmsMessage.getProperties().isEmpty()) {
            properties.putAll(mmsMessage.getProperties());
        }
        if (this.metadata.getIsEncrypt()) {
            properties.put("encrypt_mark", "#%$==");
            mmsMessage.setPayload(MMSCryptoManager.encrypt(this.metadata.getName(), mmsMessage.getPayload()));
        }
        if (StringUtils.isNotBlank(MQ_TAG)) {
            properties.put("mqTag", MQ_TAG);
        }
        if (StringUtils.isNotBlank(MQ_COLOR)) {
            properties.put("mqColor", MQ_COLOR);
        }
        Message message = new Message(this.metadata.getName(), mmsMessage.getTags(), mmsMessage.getKey(), mmsMessage.getPayload());
        message.getProperties().putAll(properties);
        return message;
    }
}
