package com.dianping.cat.status;

import java.util.Map;

public interface StatusExtension {
    String getId();

    String getDescription();

    Map<String, String> getProperties();
}
