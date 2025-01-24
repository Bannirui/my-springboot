package com.github.bannirui.msb.mq.sdk.producer;

public interface MmsCallBack {
    void onException(Throwable exception);

    void onResult(SendResponse response);
}
