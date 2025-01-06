package com.dianping.cat.configuration;

import com.dianping.cat.configuration.client.entity.Domain;
import com.dianping.cat.configuration.client.entity.Server;
import java.io.File;
import java.util.List;

public interface ClientConfigManager {

    Domain getDomain();

    int getMaxMessageLength();

    String getServerConfigUrl();

    List<Server> getServers();

    int getTaggedTransactionCacheSize();

    void initialize(File f) throws Exception;

    boolean isCatEnabled();

    boolean isDumpLocked();
}
