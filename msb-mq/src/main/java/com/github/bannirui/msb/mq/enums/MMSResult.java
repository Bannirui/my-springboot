package com.github.bannirui.msb.mq.enums;

import com.github.bannirui.msb.mq.sdk.consumer.MsgConsumedStatus;

public class MMSResult {
    private MsgConsumedStatus consumedStatus;

    public MsgConsumedStatus getConsumedStatus() {
        return this.consumedStatus;
    }

    private void setConsumedStatus(MsgConsumedStatus consumedStatus) {
        this.consumedStatus = consumedStatus;
    }

    public static MMSResult status(MsgConsumedStatus consumedStatus) {
        MMSResult MMSResult = new MMSResult();
        MMSResult.setConsumedStatus(consumedStatus);
        return MMSResult;
    }
}
