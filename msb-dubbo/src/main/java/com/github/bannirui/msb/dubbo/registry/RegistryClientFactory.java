package com.github.bannirui.msb.dubbo.registry;

import com.alipay.sofa.registry.client.api.RegistryClient;
import com.alipay.sofa.registry.client.api.RegistryClientConfig;
import com.alipay.sofa.registry.client.provider.DefaultRegistryClient;
import com.alipay.sofa.registry.client.provider.DefaultRegistryClientConfig;
import com.alipay.sofa.registry.client.provider.DefaultRegistryClientConfigBuilder;
import org.apache.dubbo.common.URL;

public class RegistryClientFactory {
    public static final String LOCAL_DATACENTER = "DefaultDataCenter";
    public static final String LOCAL_REGION = "DEFAULT_ZONE";
    private static RegistryClient registryClient;

    public static synchronized RegistryClient getRegistryClient(URL url) {
        if (registryClient == null) {
            DefaultRegistryClientConfig config = DefaultRegistryClientConfigBuilder.start().setRegistryEndpoint(url.getHost()).setRegistryEndpointPort(url.getPort()).setDataCenter("DefaultDataCenter").setZone("DEFAULT_ZONE").build();
            registryClient = getRegistryClient((RegistryClientConfig)config);
        }
        return registryClient;
    }

    public static synchronized RegistryClient getRegistryClient(RegistryClientConfig config) {
        if (null == config) {
            throw new IllegalArgumentException("config can not be null");
        } else if (null != registryClient) {
            return registryClient;
        } else {
            registryClient = new DefaultRegistryClient(config);
            ((DefaultRegistryClient)registryClient).init();
            return registryClient;
        }
    }
}
