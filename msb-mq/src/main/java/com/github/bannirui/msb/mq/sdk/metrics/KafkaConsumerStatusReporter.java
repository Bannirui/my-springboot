package com.github.bannirui.msb.mq.sdk.metrics;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.bannirui.msb.mq.sdk.Mms;
import com.github.bannirui.msb.mq.sdk.common.SimpleMessage;
import com.github.bannirui.msb.mq.sdk.common.MmsEnv;
import com.github.bannirui.msb.mq.sdk.message.statistic.kafka.KafkaConsumerInfo;
import com.github.bannirui.msb.mq.sdk.message.statistic.kafka.KafkaConsumerStatistics;
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

public class KafkaConsumerStatusReporter {
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerStatusReporter.class);
    private static final String CONSUMER = "kafka.consumer:type=consumer-metrics,client-id=*";
    private static final String INCOMING_BYTE_RATE = "incoming-byte-rate";
    private static final String IO_RATIO = "io-ratio";
    private static final String IO_TIME_NS_AVG = "io-time-ns-avg";
    private static final String IO_WAIT_RATIO = "io-wait-ratio";
    private static final String IO_WAIT_TIME_NS_AVG = "io-wait-time-ns-avg";
    private static final String IO_WAITTIME_TOTAL = "io-waittime-total";
    private static final String IOTIME_TOTAL = "iotime-total";
    private static final String NETWORK_IO_RATE = "network-io-rate";
    private static final String REQUEST_RATE = "request-rate";
    private static final String REQUEST_SIZE_AVG = "request-size-avg";
    private static final String REQUEST_SIZE_MAX = "request-size-max";
    private static final String RESPONSE_RATE = "response-rate";
    private static final String SELECT_RATE = "select-rate";
    String[] attrs;

    public static KafkaConsumerStatusReporter getInstance() {
        return KafkaConsumerStatusReporter.Instance.reporter;
    }

    private KafkaConsumerStatusReporter() {
        this.attrs = new String[]{"incoming-byte-rate", "io-ratio", "io-time-ns-avg", "io-wait-ratio", "io-wait-time-ns-avg", "io-waittime-total", "iotime-total", "network-io-rate", "request-rate", "request-size-avg", "request-size-max", "response-rate", "select-rate"};
    }

    public void reportConsumerStatus() {
        try {
            Set<ObjectInstance> objectInstances = ManagementFactory.getPlatformMBeanServer().queryMBeans(new ObjectName("kafka.consumer:type=consumer-metrics,client-id=*"), null);
            KafkaConsumerStatistics consumerStatistics = new KafkaConsumerStatistics();
            List<KafkaConsumerInfo> consumerInfos = Lists.newArrayList();
            consumerStatistics.setKafkaConsumerInfos(consumerInfos);
            for (ObjectInstance instance : objectInstances) {
                KafkaConsumerInfo kafkaConsumerInfo = new KafkaConsumerInfo();
                consumerInfos.add(kafkaConsumerInfo);
                String clientIdName = instance.getObjectName().getKeyProperty("client-id");
                if (!StringUtils.isBlank(clientIdName) && clientIdName.contains("--")) {
                    String cg = clientIdName.substring(0, clientIdName.indexOf("--"));
                    kafkaConsumerInfo.setIp(MmsEnv.MMS_IP);
                    kafkaConsumerInfo.setConsumerGroup(cg);
                    kafkaConsumerInfo.setClientIdName(clientIdName);
                    AttributeList attributes = ManagementFactory.getPlatformMBeanServer().getAttributes(instance.getObjectName(), this.attrs);
                    attributes.forEach(attribute -> {
                        Attribute attr = (Attribute) attribute;
                        if (attr.getName().equalsIgnoreCase("incoming-byte-rate")) {
                            kafkaConsumerInfo.setIncomingByteRate((Double) attr.getValue());
                        } else if (attr.getName().equalsIgnoreCase("io-ratio")) {
                            kafkaConsumerInfo.setIoRatio((Double) attr.getValue());
                        } else if (attr.getName().equalsIgnoreCase("io-time-ns-avg")) {
                            kafkaConsumerInfo.setIoTimeNsAvg((Double) attr.getValue());
                        } else if (attr.getName().equalsIgnoreCase("io-wait-ratio")) {
                            kafkaConsumerInfo.setIoWaitRatio((Double) attr.getValue());
                        } else if (attr.getName().equalsIgnoreCase("io-wait-time-ns-avg")) {
                            kafkaConsumerInfo.setIoWaitTimeNsAvg((Double) attr.getValue());
                        } else if (attr.getName().equalsIgnoreCase("io-waittime-total")) {
                            kafkaConsumerInfo.setIoWaittimeTotal((Double) attr.getValue());
                        } else if (attr.getName().equalsIgnoreCase("iotime-total")) {
                            kafkaConsumerInfo.setIotimeTotal((Double) attr.getValue());
                        } else if (attr.getName().equalsIgnoreCase("network-io-rate")) {
                            kafkaConsumerInfo.setNetworkIoRate((Double) attr.getValue());
                        } else if (attr.getName().equalsIgnoreCase("request-rate")) {
                            kafkaConsumerInfo.setRequestRate((Double) attr.getValue());
                        } else if (attr.getName().equalsIgnoreCase("request-size-avg")) {
                            kafkaConsumerInfo.setRequestSizeAvg((Double) attr.getValue());
                        } else if (attr.getName().equalsIgnoreCase("request-size-max")) {
                            kafkaConsumerInfo.setRequestSizeMax((Double) attr.getValue());
                        } else if (attr.getName().equalsIgnoreCase("response-rate")) {
                            kafkaConsumerInfo.setResponseRate((Double) attr.getValue());
                        } else if (attr.getName().equalsIgnoreCase("select-rate")) {
                            kafkaConsumerInfo.setSelectRate((Double) attr.getValue());
                        }
                    });
                } else {
                    logger.error("reportConsumerStatus error:  client-id {} maybe wrong ", clientIdName);
                }
            }
            Mms.sendOneway("statistic_topic_kafka_consumerinfo", new SimpleMessage(JSON.toJSONBytes(consumerStatistics, new SerializerFeature[0])));
        } catch (Exception var13) {
            logger.error("report kafka consumer status error", var13);
        }

    }

    private static class Instance {
        public static KafkaConsumerStatusReporter reporter = new KafkaConsumerStatusReporter();

        private Instance() {
        }
    }
}
