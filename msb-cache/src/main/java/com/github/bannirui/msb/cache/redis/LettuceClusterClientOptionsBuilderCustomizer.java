package com.github.bannirui.msb.cache.redis;

import io.lettuce.core.ClientOptions;

@FunctionalInterface
public interface LettuceClusterClientOptionsBuilderCustomizer {
    void customize(ClientOptions.Builder clusterClientOptionsBuilder, io.lettuce.core.cluster.ClusterTopologyRefreshOptions.Builder topologyRefreshBuilder);
}
