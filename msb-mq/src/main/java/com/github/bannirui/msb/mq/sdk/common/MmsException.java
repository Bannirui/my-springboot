package com.github.bannirui.msb.mq.sdk.common;

public class MmsException extends RuntimeException {
    public static final MmsException PRODUCER_START_EXCEPTION = new MmsException("MMS producer start failed", 1001);
    public static final MmsException INVALID_NAME_EXCEPTION = new MmsException("MMS Producer name can't be empty or DefaultName", 1002);
    public static final MmsException EMPTY_TOPIC_EXCEPTION = new MmsException("Topic cant'be empty ", 1003);
    public static final MmsException NO_ZK_EXCEPTION = new MmsException("nor env param, MMS_ZK or startup param mms_zk exists ", 1004);
    public static final MmsException NOT_RUNNNING_EXCEPTION = new MmsException("Proxy is not running ", 1005);
    public static final MmsException CONSUMER_START_EXCEPTION = new MmsException("MMS consumer start failed", 1006);
    public static final MmsException CLUSTER_INFO_EXCEPTION = new MmsException("Cluster metadata is empty or can't be parsed", 1005);
    public static final MmsException METAINFO_EXCEPTION = new MmsException("metadata is empty or can't be parsed", 1006);
    public static final MmsException NO_MMS_PROFILE_EXCEPTION = new MmsException("Unable to read the MMS configuration file", 1007);
    public static final MmsException CONSUMER_NOT_EXISTS_EXCEPTION = new MmsException("Proxy is not running ", 1008);
    public static final MmsException TOPIC_ENV_NOT_EXISTS_EXCEPTION = new MmsException("Proxy is not running ", 1009);
    public static final MmsException CHECK_TOPIC_STATUS_EXCEPTION = new MmsException("请先审批topic", 1009);
    public static final MmsException CHECK_TOPIC_PARTITIONS_EXCEPTION = new MmsException("check topic partitions error", 2001);
    public static final MmsException NO_PERMISSION_EXCEPTION = new MmsException("no permission error", 403);
    public static final MmsException ILLEGAL_CLUSTER_TYPE = new MmsException("Illegal cluster type", 2100);
    public static final MmsException BROADCAST_MUST_BE_FALSE = new MmsException("broadcast must be false when cluster type is kafka", 1008);
    public static final MmsException ROCKET_MQ_NOT_SUPPORT_RESET_TO_OFFSET = new MmsException("RocketMq does not support resetting to the specified offset", 1009);
    public static final MmsException ROCKET_MQ_NOT_SUPPORT_RESET_TO_LATEST = new MmsException("RocketMq does not support resetting to the latest offset", 1010);
    public static final MmsException ROCKET_MQ_NOT_SUPPORT_RESET_TO_EARLIEST = new MmsException("RocketMq does not support resetting to the earliest offset", 1011);
    public static final MmsException NO_PERMISSION_MODIFY = new MmsException("There is no permission to modify the resource", 2101);
    public static final MmsException FUTURE_GET_EXCEPTION = new MmsException("query influxDB failure", 1012);
    public static final MmsException CONSUMER_CAN_NOT_EXISTS_CHANGE = new MmsException("consumer name can not change ", 1008);
    public static final MmsException MetainfoException = new MmsException("metadata is empty or can't be parsed", 1006);
    public static final MmsException GET_KEY_EXCEPTION = new MmsException("Failed to obtain the key, please check if the subject has applied for the key", 2201);
    public static final MmsException CREATE_KEY_FAILURE_EXCEPTION = new MmsException("create key failure", 2202);
    public static final MmsException CRYPTO_TYPE_NOT_EXITS_EXCEPTION = new MmsException("crypto type not exits", 2203);
    public static final MmsException INIT_CIPHER_EXCEPTION = new MmsException("init cipher failure", 2204);
    public static final MmsException GENERATE_KEY_EXCEPTION = new MmsException("generate key failure", 2205);
    public static final MmsException DECRYPT_EXCEPTION = new MmsException("decrypt failure", 2206);
    public static final MmsException ENCRYPT_EXCEPTION = new MmsException("encrypt failure", 2207);
    public static final MmsException MQ_TAG_EXCEPTION = new MmsException("If mqTag is set, tt must be set mmsRewrite.", 2207);
    public static final MmsException SERIALIZER_EXCEPTION = new MmsException("Prohibit setting serialization class.", 2208);
    public static final MmsException KAFKA_CONSUMER_START_FAILURE = new MmsException("kafka consumer start failure", 2209);
    public static final MmsException KAFKA_CLIENT_VERSION_TOO_LOW = new MmsException("Kafka client 版本太低，一般情况下是由于项目本身所依赖的 Kafka client 版本过低导致，排除项目依赖的 Kafka client 即可", 2210);
    public static final MmsException ZAMP_IOEXCEPTION = new MmsException("回调ZAMP网络异常", 2210);
    public static final MmsException ZAMP_PRODUCTINFO_EXCEPTION = new MmsException("查询ZAMP产品异常", 2211);
    public static final MmsException DEPLOY_EXCEPTION = new MmsException("配置错误，路由标签和蓝绿发布不能共用", 2212);
    public static final MmsException RELEASE_EXCEPTION = new MmsException("未配置蓝绿标签属性", 2213);
    public static final MmsException ENVIRONMENT_EXCEPTION = new MmsException("消费组环境信息异常", 2214);
    public static final MmsException RELEASE_NULL_EXCEPTION = new MmsException("发布状态不能为空", 2214);

    private final int code;

    public MmsException(String message, int code) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }
}
