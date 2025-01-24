package com.github.bannirui.msb.mq.sdk.producer;

import com.github.bannirui.msb.mq.sdk.common.SLA;
import com.github.bannirui.msb.mq.sdk.common.MmsEnv;
import com.github.bannirui.msb.mq.sdk.common.MmsMessage;
import com.github.bannirui.msb.mq.sdk.config.DefaultMmsClientConfig;
import com.github.bannirui.msb.mq.sdk.config.MmsClientConfig;
import com.github.bannirui.msb.mq.sdk.crypto.MMSCryptoManager;
import com.github.bannirui.msb.mq.sdk.metadata.MmsMetadata;
import com.github.bannirui.msb.mq.sdk.metrics.KafkaProducerStatusReporter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaProducerProxy extends MmsProducerProxy {
    private static final Charset utf_8 = StandardCharsets.UTF_8;
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerProxy.class);
    private KafkaProducer<String, byte[]> producer;
    private int sendTimeOut = 3000;

    public KafkaProducerProxy(MmsMetadata metadata, SLA sla, String instanceName) {
        super(metadata, sla, instanceName);
        this.instanceName = instanceName;
        this.start();
    }

    public KafkaProducerProxy(MmsMetadata metadata, SLA sla, String instanceName, Properties properties) {
        super(metadata, sla, instanceName, properties);
        this.instanceName = instanceName;
        this.start();
    }

    public void statistics() {
        super.statistics();
        KafkaProducerStatusReporter.getInstance().reportProducerStatus();
    }

    public void startProducer() {
        Properties kafkaProperties = new Properties();
        kafkaProperties.putAll(DefaultMmsClientConfig.DEFAULT_KAFKA_PRODUCER_CONFIG);
        if (this.metadata.isGatedLaunch()) {
            kafkaProperties.put("bootstrap.servers", this.metadata.getGatedCluster().getBootAddr());
        } else {
            kafkaProperties.put("bootstrap.servers", this.metadata.getClusterMetadata().getBootAddr());
        }
        kafkaProperties.put("client.id", this.metadata.getName() + "--" + MmsEnv.MMS_IP + "--" + ThreadLocalRandom.current().nextInt(100_000));
        if (this.customizedProperties != null) {
            kafkaProperties.putAll(this.customizedProperties);
        }
        this.reviseKafkaConfig(kafkaProperties);
        this.producer = new KafkaProducer<>(kafkaProperties);
    }

    private void reviseKafkaConfig(Properties properties) {
        if (properties.containsKey(MmsClientConfig.PRODUCER.SEND_TIMEOUT_MS.getKey())) {
            this.sendTimeOut = Integer.parseInt(String.valueOf(properties.get(MmsClientConfig.PRODUCER.SEND_TIMEOUT_MS.getKey())));
            properties.remove(MmsClientConfig.PRODUCER.SEND_TIMEOUT_MS.getKey());
        }
        properties.remove(MmsClientConfig.PRODUCER.RETRIES.getKey());
    }

    public void shutdownProducer() {
        this.producer.close(Duration.ofSeconds(3L));
    }

    public SendResponse syncSend(MmsMessage mmsMessage) {
        if (!this.running) {
            return SendResponse.FAILURE_NOTRUNNING;
        } else {
            ProducerRecord<String, byte[]> record = this.buildMessage(mmsMessage);
            long startTime = System.currentTimeMillis();
            boolean succeed = false;
            SendResponse ans=null;
            try {
                Future<RecordMetadata> send = this.producer.send(record);
                this.mmsMetrics.msgBody().markSize((long)((byte[])record.value()).length);
                RecordMetadata metadata = send.get((long)this.sendTimeOut, TimeUnit.MILLISECONDS);
                long duration = System.currentTimeMillis() - startTime;
                this.mmsMetrics.sendCostRate().update(duration, TimeUnit.MILLISECONDS);
                succeed = true;
                this.mmsMetrics.getDistribution().markTime(duration);
                return SendResponse.buildSuccessResult(metadata.offset(), "", metadata.topic(), metadata.partition());
            } catch (InterruptedException e) {
                logger.error("produce syncSend and wait interuptted", e);
                return SendResponse.FAILURE_INTERUPRION;
            } catch (ExecutionException e) {
                logger.error("produce syncSend and wait got exception", e);
                if (e.getCause() instanceof TimeoutException) {
                    return SendResponse.FAILURE_TIMEOUT;
                }
                String errMsg = "execution got exception when syncSend and wait message: ";
                if (e.getCause() != null && StringUtils.isNoneBlank(new CharSequence[]{e.getCause().getMessage()})) {
                    errMsg = errMsg + e.getCause().getMessage();
                }
                return SendResponse.buildErrorResult(errMsg);
            } catch (java.util.concurrent.TimeoutException e) {
                logger.error("produce syncSend and wait timeout", e);
                ans = SendResponse.FAILURE_TIMEOUT;
            } finally {
                if (succeed) {
                    this.mmsMetrics.messageSuccessRate().mark();
                } else {
                    this.mmsMetrics.messageFailureRate().mark();
                }
            }
            return ans;
        }
    }

    public void asyncSend(MmsMessage mmsMessage, MmsCallBack callBack) {
        ProducerRecord<String, byte[]> record = this.buildMessage(mmsMessage);
        long startTime = System.currentTimeMillis();
        this.mmsMetrics.msgBody().markSize(record.value().length);
        this.producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                this.mmsMetrics.messageFailureRate().mark();
                callBack.onException(exception);
            } else {
                long duration = System.currentTimeMillis() - startTime;
                SendResponse sendResponse = SendResponse.buildSuccessResult(metadata.offset(), "", metadata.topic(), metadata.partition());
                this.mmsMetrics.sendCostRate().update(duration, TimeUnit.MILLISECONDS);
                this.mmsMetrics.messageSuccessRate().mark();
                this.mmsMetrics.getDistribution().markTime(duration);
                callBack.onResult(sendResponse);
            }
        });
    }

    public void oneway(MmsMessage mmsMessage) {
        this.mmsMetrics.msgBody().markSize(mmsMessage.getPayload().length);
        ProducerRecord<String, byte[]> producerRecord = this.buildMessage(mmsMessage);
        this.producer.send(producerRecord);
    }

    private ProducerRecord<String, byte[]> buildMessage(MmsMessage mmsMessage) {
        Headers headers = new RecordHeaders();
        if (mmsMessage.getProperties() != null && !mmsMessage.getProperties().isEmpty()) {
            mmsMessage.getProperties().forEach((k, v) -> {
                Header header = new RecordHeader(k, v.getBytes(utf_8));
                headers.add(header);
            });
        }
        RecordHeader header;
        if (this.metadata.getIsEncrypt()) {
            header = new RecordHeader("encrypt_mark", "#%$==".getBytes(utf_8));
            headers.add(header);
            mmsMessage.setPayload(MMSCryptoManager.encrypt(this.metadata.getName(), mmsMessage.getPayload()));
        }
        if (StringUtils.isNotBlank(MQ_TAG)) {
            header = new RecordHeader("mqTag", MQ_TAG.getBytes(utf_8));
            headers.add(header);
        }
        if (StringUtils.isNotBlank(MQ_COLOR)) {
            header = new RecordHeader("mqColor", MQ_COLOR.getBytes(utf_8));
            headers.add(header);
        }
        return new ProducerRecord<>(this.metadata.getName(), null, mmsMessage.getKey(), mmsMessage.getPayload(), headers);
    }
}
