package com.github.bannirui.msb.mq.sdk.producer;

import com.github.bannirui.msb.mq.sdk.MmsService;
import com.github.bannirui.msb.mq.sdk.common.MmsMessage;

public interface Producer extends MmsService {
    SendResponse syncSend(MmsMessage mmsMessage);

    void asyncSend(MmsMessage mmsMessage, MmsCallBack mmsCallBack);

    void oneway(MmsMessage mmsMessage);

    void statistics();
}
