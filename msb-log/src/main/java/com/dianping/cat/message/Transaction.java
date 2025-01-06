package com.dianping.cat.message;

import java.util.List;

public interface Transaction extends Message {

    Transaction addChild(Message message);

    List<Message> getChildren();

    long getDurationInMicros();

    long getDurationInMillis();

    boolean hasChildren();

    boolean isStandalone();
}
