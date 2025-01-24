package com.github.bannirui.msb.mq.sdk.producer;

public class SendResponse {
    private int code;
    private Object info;
    private long offset;
    private String msgId;
    private String topic;
    private int queueOrPartition;
    private String msg;
    public static SendResponse SUCCESS = new SendResponse(200, (String)null);
    public static SendResponse FAILURE_NOTRUNNING = new SendResponse(401, "client状态不是running");
    public static SendResponse FAILURE_TIMEOUT = new SendResponse(402, "客户端发送超时");
    public static SendResponse FAILURE_INTERUPRION = new SendResponse(403, "等待线程被中断");
    public static SendResponse FAILURE_SLAVE = new SendResponse(405, "slave节点不存在");

    public SendResponse(int code, long offset, String msgId, String topic, int queueOrPartition) {
        this.code = code;
        this.offset = offset;
        this.msgId = msgId;
        this.topic = topic;
        this.queueOrPartition = queueOrPartition;
    }

    public SendResponse(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return this.code;
    }

    public Object getInfo() {
        return this.info;
    }

    public long getOffset() {
        return this.offset;
    }

    public String getMsgId() {
        return this.msgId;
    }

    public String getTopic() {
        return this.topic;
    }

    public int getQueueOrPartition() {
        return this.queueOrPartition;
    }

    public String getMsg() {
        return this.msg;
    }

    public static SendResponse buildSuccessResult(long offset, String msgId, String topic, int queueOrPartition) {
        return new SendResponse(200, offset, msgId, topic, queueOrPartition);
    }

    public static SendResponse buildErrorResult(String msg) {
        return new SendResponse(404, msg);
    }

    public static SendResponse buildErrorResult(int code, String msg) {
        return new SendResponse(code, msg);
    }

    public boolean isSucceed() {
        return this.code == 200;
    }
}
