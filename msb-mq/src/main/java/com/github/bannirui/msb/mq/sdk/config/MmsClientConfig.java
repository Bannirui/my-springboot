package com.github.bannirui.msb.mq.sdk.config;

public class MmsClientConfig {
    public static enum CONSUMER {
        CONSUME_THREAD_MIN("consumeThreadMin"),
        CONSUME_THREAD_MAX("consumeThreadMax"),
        ORDERLY_CONSUME_PARTITION_PARALLELISM("orderlyConsumePartitionParallelism"),
        ORDERLY_CONSUME_THREAD_SIZE("orderlyConsumeThreadSize"),
        /** @deprecated */
        @Deprecated
        CONSUME_MESSAGES_SIZE("consumeMessagesSize"),
        MAX_BATCH_RECORDS("MaxBatchRecords"),
        CONSUME_ORDERLY("isOrderly"),
        CONSUME_LITE_PUSH("isNewPush"),
        CONSUME_TIMEOUT_MS("consumeTimeoutMs"),
        CONSUME_BATCH_SIZE("consumeBatchSize"),
        MAX_RECONSUME_TIMES("maxReconsumeTimes");

        private final String key;

        private CONSUMER(String key) {
            this.key = key;
        }

        public String getKey() {
            return this.key;
        }
    }

    public static enum PRODUCER {
        SEND_TIMEOUT_MS("timeout"),
        RETRIES("retries"),
        BATCH_SIZE("batch.size"),
        LINGER_MS("linger.ms"),
        ACKS("acks"),
        MAX_REQUEST_SIZE("max.request.size"),
        BUFFER_MEMORY("buffer.memory"),
        COMPRESSION_TYPE("compression.type"),
        MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION("max.in.flight.requests.per.connection");

        private final String key;

        private PRODUCER(String key) {
            this.key = key;
        }

        public String getKey() {
            return this.key;
        }
    }
}
