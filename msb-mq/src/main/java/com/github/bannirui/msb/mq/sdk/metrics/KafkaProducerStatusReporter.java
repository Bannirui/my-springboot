package com.github.bannirui.msb.mq.sdk.metrics;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.bannirui.msb.mq.sdk.Mms;
import com.github.bannirui.msb.mq.sdk.common.SimpleMessage;
import com.github.bannirui.msb.mq.sdk.common.MmsEnv;
import com.github.bannirui.msb.mq.sdk.message.statistic.kafka.KafkaProducerInfo;
import com.github.bannirui.msb.mq.sdk.message.statistic.kafka.KafkaProducerStatistics;
import com.google.common.collect.Lists;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Set;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaProducerStatusReporter {
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerStatusReporter.class);
    private static final String PRORUDCER = "kafka.producer:type=producer-metrics,client-id=*";
    private static final String REQUEST_LATENCY_AVG = "request-latency-avg";
    private static final String RECORD_RETRY_RATE = "record-retry-rate";
    private static final String RECORD_ERROR_RATE = "record-error-rate";
    private static final String BATCH_SIZE_AVG = "batch-size-avg";
    private static final String BATCH_SIZE_MAX = "batch-size-max";
    private static final String BUFFER_AVAILABLE_BYTES = "buffer-available-bytes";
    private static final String BUFFERPOOL_WAIT_RATIO = "bufferpool-wait-ratio";
    private static final String BUFFERPOOL_WAIT_TIME_TOTAL = "bufferpool-wait-time-total";
    private static final String PRODUCE_THROTTLE_TIME_AVG = "produce-throttle-time-avg";
    private static final String PRODUCE_THROTTLE_TIME_MAX = "produce-throttle-time-max";
    private static final String IO_RATIO = "io-ratio";
    private static final String IO_TIME_NS_AVG = "io-time-ns-avg";
    private static final String IO_WAITTIME_TOTAL = "io-waittime-total";
    private static final String IOTIME_TOTAL = "iotime-total";
    private static final String NETWORK_IO_RATE = "network-io-rate";
    private static final String NETWORK_IO_TOTAL = "network-io-total";
    String[] attrs;

    public static KafkaProducerStatusReporter getInstance() {
        return KafkaProducerStatusReporter.Instance.reporter;
    }

    private KafkaProducerStatusReporter() {
        this.attrs = new String[]{"record-error-rate", "record-retry-rate", "request-latency-avg", "batch-size-avg", "batch-size-max", "buffer-available-bytes", "bufferpool-wait-ratio", "bufferpool-wait-time-total", "produce-throttle-time-avg", "produce-throttle-time-max", "io-ratio", "io-time-ns-avg", "io-waittime-total", "iotime-total", "network-io-rate", "network-io-total"};
    }

    public void reportProducerStatus() {
        try {
            Set<ObjectInstance> objectInstances = ManagementFactory.getPlatformMBeanServer().queryMBeans(new ObjectName("kafka.producer:type=producer-metrics,client-id=*"), null);
            KafkaProducerStatistics producerStatistics = new KafkaProducerStatistics();
            List<KafkaProducerInfo> producerInfos = Lists.newArrayList();
            producerStatistics.setKafkaProducerInfos(producerInfos);
            for (ObjectInstance instance : objectInstances) {
                KafkaProducerInfo kafkaProducerInfo = new KafkaProducerInfo();
                producerInfos.add(kafkaProducerInfo);
                String clientIdName = instance.getObjectName().getKeyProperty("client-id");
                if (!StringUtils.isBlank(clientIdName) && clientIdName.contains("--")) {
                    String topic = clientIdName.substring(0, clientIdName.indexOf("--"));
                    kafkaProducerInfo.setIp(MmsEnv.MMS_IP);
                    kafkaProducerInfo.setTopic(topic);
                    kafkaProducerInfo.setClientIdName(clientIdName);
                    AttributeList attributes = ManagementFactory.getPlatformMBeanServer().getAttributes(instance.getObjectName(), this.attrs);
                    attributes.forEach(attribute -> {
                        Attribute attr = (Attribute)attribute;
                        if (attr.getName().equalsIgnoreCase("record-error-rate")) {
                            kafkaProducerInfo.setErrorRate((Double)((Double)attr.getValue()));
                        } else if (attr.getName().equalsIgnoreCase("record-retry-rate")) {
                            kafkaProducerInfo.setRetryRate((Double)((Double)attr.getValue()));
                        } else if (attr.getName().equalsIgnoreCase("request-latency-avg")) {
                            kafkaProducerInfo.setSendLatency((Double)((Double)attr.getValue()));
                        } else if (attr.getName().equalsIgnoreCase("batch-size-avg")) {
                            kafkaProducerInfo.setBatchSizeAvg((Double)((Double)attr.getValue()));
                        } else if (attr.getName().equalsIgnoreCase("batch-size-max")) {
                            kafkaProducerInfo.setBatchSizeMax((Double)((Double)attr.getValue()));
                        } else if (attr.getName().equalsIgnoreCase("buffer-available-bytes")) {
                            kafkaProducerInfo.setBufferAvailableBytes((Double)((Double)attr.getValue()));
                        } else if (attr.getName().equalsIgnoreCase("bufferpool-wait-ratio")) {
                            kafkaProducerInfo.setBufferpoolWaitRatio((Double)((Double)attr.getValue()));
                        } else if (attr.getName().equalsIgnoreCase("bufferpool-wait-time-total")) {
                            kafkaProducerInfo.setBufferpoolWaitTimeTotal((Double)((Double)attr.getValue()));
                        } else if (attr.getName().equalsIgnoreCase("produce-throttle-time-avg")) {
                            kafkaProducerInfo.setProduceThrottleTimeAvg((Double)((Double)attr.getValue()));
                        } else if (attr.getName().equalsIgnoreCase("produce-throttle-time-max")) {
                            kafkaProducerInfo.setProduceThrottleTimeMax((Double)((Double)attr.getValue()));
                        } else if (attr.getName().equalsIgnoreCase("io-ratio")) {
                            kafkaProducerInfo.setIoRatio((Double)((Double)attr.getValue()));
                        } else if (attr.getName().equalsIgnoreCase("io-time-ns-avg")) {
                            kafkaProducerInfo.setIoTimeNsAvg((Double)((Double)attr.getValue()));
                        } else if (attr.getName().equalsIgnoreCase("io-waittime-total")) {
                            kafkaProducerInfo.setIoWaittimeTotal((Double)((Double)attr.getValue()));
                        } else if (attr.getName().equalsIgnoreCase("iotime-total")) {
                            kafkaProducerInfo.setIotimeTotal((Double)((Double)attr.getValue()));
                        } else if (attr.getName().equalsIgnoreCase("network-io-rate")) {
                            kafkaProducerInfo.setNetworkIoRate((Double)((Double)attr.getValue()));
                        } else if (attr.getName().equalsIgnoreCase("network-io-total")) {
                            kafkaProducerInfo.setNetworkIoTotal((Double)((Double)attr.getValue()));
                        }
                    });
                } else {
                    logger.error("reportProducerStatus error:  client-id {} maybe wrong ", clientIdName);
                }
            }
            Mms.sendOneway("statistic_topic_kafka_producerinfo", new SimpleMessage(JSON.toJSONBytes(producerStatistics, new SerializerFeature[0])));
        } catch (Exception e) {
            logger.error("report kafka consumer status error", e);
        }
    }

    private static class Instance {
        public static KafkaProducerStatusReporter reporter = new KafkaProducerStatusReporter();

        private Instance() {
        }
    }
}
