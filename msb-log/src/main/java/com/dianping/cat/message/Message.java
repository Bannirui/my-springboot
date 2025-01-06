package com.dianping.cat.message;

public interface Message {

    String SUCCESS = "0";

    void addData(String keyValuePairs);

    void addData(String key, Object value);

    void complete();

    Object getData();

    String getName();

    String getStatus();

    long getTimestamp();

    String getType();

    boolean isCompleted();

    boolean isSuccess();

    void setStatus(String status);

    void setStatus(Throwable e);
}
