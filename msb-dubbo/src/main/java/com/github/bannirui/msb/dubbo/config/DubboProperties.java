package com.github.bannirui.msb.dubbo.config;

import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.dubbo.config.ConsumerConfig;
import org.apache.dubbo.config.ModuleConfig;
import org.apache.dubbo.config.MonitorConfig;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.ProviderConfig;
import org.apache.dubbo.config.RegistryConfig;

public class DubboProperties {
    public static final String DUBBO_ANTH_SIGN_KEY = "dubbo.auth.sign.key";
    public static final String DUBBO_AUTH_SIGN_APPID = "dubbo.auth.sign.appid";
    public static final String DUBBO_AUTH_PROVIDER_PACKAGE = "dubbo.auth.provider.package";
    public static final String DUBBO_AUTH_PROVIDER_SIGN_APPID = "dubbo.auth.provider.sign[%s].appid";
    public static final String DUBBO_AUTH_PROVIDER_SIGN_KEY = "dubbo.auth.provider.sign[%s].key";
    public static final String DUBBO_AUTH_CONSUMER_PACKAGE = "dubbo.auth.consumer[%s].package";
    public static final String DUBBO_AUTJ_CONSUMER_KET = "dubbo.auth.consumer[%s].sign";
    public static final String DUBBO_SCAN_PACKAGE_NAME = "dubbo.scanPackageName";
    public static final String CONSUMER_TAG = "dubbo.consumer.tag";
    public static String consumerTag;
    public static Boolean consumerTagforce;
    private String tag;
    private Boolean tagforce;
    private String protocol;
    private Integer port;
    private String host;
    private String logger;
    private Integer timeout;
    private Integer retries;
    private Integer delay;
    private String registryProtocol;
    private String registryAddress;
    private String registryType;
    private String msbRegistryAddress;
    private MultipleProperties multiple = new MultipleProperties();
    private Map<String, String> deserializeWhites;
    private Map<String, String> deserializePackageWhites;
    private Map<String, String> deserializeBlacks;
    private Map<String, String> deserializePackageBlacks;
    private Map<String, ModuleConfig> modules = new LinkedHashMap<>();
    private Map<String, RegistryConfig> registries = new LinkedHashMap<>();
    private Map<String, ProtocolConfig> protocols = new LinkedHashMap<>();
    private Map<String, MonitorConfig> monitors = new LinkedHashMap<>();
    private Map<String, ProviderConfig> providers = new LinkedHashMap<>();
    private Map<String, ConsumerConfig> consumers = new LinkedHashMap<>();

    public Map<String, ModuleConfig> getModules() {
        return this.modules;
    }

    public void setModules(Map<String, ModuleConfig> modules) {
        this.modules = modules;
    }

    public Map<String, RegistryConfig> getRegistries() {
        return this.registries;
    }

    public void setRegistries(Map<String, RegistryConfig> registries) {
        this.registries = registries;
    }

    public Map<String, ProtocolConfig> getProtocols() {
        return this.protocols;
    }

    public void setProtocols(Map<String, ProtocolConfig> protocols) {
        this.protocols = protocols;
    }

    public Map<String, MonitorConfig> getMonitors() {
        return this.monitors;
    }

    public void setMonitors(Map<String, MonitorConfig> monitors) {
        this.monitors = monitors;
    }

    public Map<String, ProviderConfig> getProviders() {
        return this.providers;
    }

    public void setProviders(Map<String, ProviderConfig> providers) {
        this.providers = providers;
    }

    public Map<String, ConsumerConfig> getConsumers() {
        return this.consumers;
    }

    public void setConsumers(Map<String, ConsumerConfig> consumers) {
        this.consumers = consumers;
    }

    public String getTag() {
        return this.tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Boolean getTagforce() {
        return this.tagforce;
    }

    public void setTagforce(Boolean tagforce) {
        this.tagforce = tagforce;
    }

    public String getProtocol() {
        return this.protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Integer getPort() {
        return this.port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getLogger() {
        return this.logger;
    }

    public void setLogger(String logger) {
        this.logger = logger;
    }

    public Integer getTimeout() {
        return this.timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Integer getRetries() {
        return this.retries;
    }

    public void setRetries(Integer retries) {
        this.retries = retries;
    }

    public Integer getDelay() {
        return this.delay;
    }

    public void setDelay(Integer delay) {
        this.delay = delay;
    }

    public String getRegistryProtocol() {
        return this.registryProtocol;
    }

    public void setRegistryProtocol(String registryProtocol) {
        this.registryProtocol = registryProtocol;
    }

    public String getRegistryAddress() {
        return this.registryAddress;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public String getRegistryType() {
        return this.registryType;
    }

    public void setRegistryType(String registryType) {
        this.registryType = registryType;
    }

    public String getMsbRegistryAddress() {
        return this.msbRegistryAddress;
    }

    public void setMsbRegistryAddress(String msbRegistryAddress) {
        this.msbRegistryAddress = msbRegistryAddress;
    }

    public MultipleProperties getMultiple() {
        return this.multiple;
    }

    public void setMultiple(MultipleProperties multiple) {
        this.multiple = multiple;
    }

    public Map<String, String> getDeserializeWhites() {
        return this.deserializeWhites;
    }

    public void setDeserializeWhites(Map<String, String> deserializeWhites) {
        this.deserializeWhites = deserializeWhites;
    }

    public Map<String, String> getDeserializePackageWhites() {
        return this.deserializePackageWhites;
    }

    public void setDeserializePackageWhites(Map<String, String> deserializePackageWhites) {
        this.deserializePackageWhites = deserializePackageWhites;
    }

    public Map<String, String> getDeserializeBlacks() {
        return this.deserializeBlacks;
    }

    public void setDeserializeBlacks(Map<String, String> deserializeBlacks) {
        this.deserializeBlacks = deserializeBlacks;
    }

    public Map<String, String> getDeserializePackageBlacks() {
        return this.deserializePackageBlacks;
    }

    public void setDeserializePackageBlacks(Map<String, String> deserializePackageBlacks) {
        this.deserializePackageBlacks = deserializePackageBlacks;
    }

    public static void setConsumerTag(String consumerTag) {
        DubboProperties.consumerTag = consumerTag;
    }

    public static void setConsumerTagforce(Boolean tagforce) {
        consumerTagforce = tagforce;
    }
}
