package com.github.bannirui.msb.dubbo.registry;

import com.alipay.sofa.registry.client.api.Subscriber;
import com.alipay.sofa.registry.client.api.model.ConfigData;
import com.alipay.sofa.registry.client.api.model.RegistryType;
import com.alipay.sofa.registry.client.api.model.UserData;
import com.alipay.sofa.registry.client.api.registration.ConfiguratorRegistration;
import com.alipay.sofa.registry.client.api.registration.PublisherRegistration;
import com.alipay.sofa.registry.client.api.registration.SubscriberRegistration;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.github.bannirui.msb.dubbo.util.DecryptUtil;
import com.github.bannirui.msb.env.MsbEnvironmentMgr;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.utils.UrlUtils;
import org.apache.dubbo.registry.NotifyListener;
import org.apache.dubbo.registry.support.FailbackRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DsrRegistry extends FailbackRegistry {
    public static final Logger LOGGER = LoggerFactory.getLogger(DsrRegistry.class);
    private static final String CONFREG_GROUP = "SOFA";
    private final Map<String, Subscriber> subscribers = new ConcurrentHashMap<>();
    private static final int wait_address_time;

    static {
        String val = MsbEnvironmentMgr.getProperty("rpc.reference.address.wait.time");
        if(StringUtils.isEmpty(val)) {
            val = "5000";
        }
        wait_address_time = Integer.parseInt(val);
    }

    public DsrRegistry(URL url) {
        super(url);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Build dsr registry by url: {}", url);
        }
    }

    @Override
    public void doRegister(URL url) {
        if (url.getParameter("register", true)) {
            String serviceName = this.buildServiceName(url);
            String serviceData = url.toFullString();
            if ("consumer".equals(url.getProtocol())) {
                PublisherRegistration dsrRegistration = new PublisherRegistration(serviceName + "@consumer");
                this.addAttributesForPub(dsrRegistration);
                RegistryClientFactory.getRegistryClient(this.getUrl()).register(dsrRegistration, serviceData);
            } else {
                String appName = url.getParameter("application");
                PublisherRegistration dsrRegistration = new PublisherRegistration(serviceName);
                this.addAttributesForPub(dsrRegistration);
                RegistryClientFactory.getRegistryClient(this.getUrl()).register(dsrRegistration, serviceData);
            }
        }
    }

    @Override
    public void doUnregister(URL url) {
        if (url.getParameter("register", true)) {
            String serviceName = this.buildServiceName(url);
            RegistryClientFactory.getRegistryClient(this.getUrl()).unregister(serviceName, "SOFA", RegistryType.PUBLISHER);
        }
    }

    @Override
    public void doSubscribe(URL url, NotifyListener listener) {
        if (url.getParameter("subscribe", true)) {
            String serviceName = this.buildServiceName(url);
            String[] categories;
            if ("*".equals(url.getParameter("category"))) {
                categories = new String[]{"providers", "consumers", "routers", "configurators"};
            } else {
                categories = url.getParameter("category", new String[]{"providers"});
            }
            for (String category : categories) {
                if ("providers".equals(category)) {
                    Subscriber listSubscriber = this.subscribers.get(serviceName);
                    final CountDownLatch countDownLatch;
                    if (listSubscriber != null) {
                        LOGGER.warn("Service name [{}] have bean registered in Confreg.", serviceName);
                        countDownLatch = new CountDownLatch(1);
                        this.handleRegistryData(url, listSubscriber.peekData(), listener, countDownLatch);
                        this.waitAddress(serviceName, countDownLatch);
                    } else {
                        countDownLatch = new CountDownLatch(1);
                        SubscriberRegistration subscriberRegistration = new SubscriberRegistration(serviceName, (dataId, data) -> {
                            DsrRegistry.this.printAddressData(dataId, data);
                            DsrRegistry.this.handleRegistryData(url, data, listener, countDownLatch);
                        });
                        this.addAttributesForSub(subscriberRegistration);
                        listSubscriber = RegistryClientFactory.getRegistryClient(this.getUrl()).register(subscriberRegistration);
                        this.subscribers.put(serviceName, listSubscriber);
                        this.waitAddress(serviceName, countDownLatch);
                    }
                } else {
                    ConfiguratorRegistration configRegistration;
                    if ("routers".equals(category)) {
                        configRegistration = new ConfiguratorRegistration(serviceName + "@router", (dataId, configData) -> DsrRegistry.this.handleConfigData(url, configData, "routers", listener));
                        configRegistration.setGroup("SOFA");
                        RegistryClientFactory.getRegistryClient(this.getUrl()).register(configRegistration);
                    } else if ("configurators".equals(category)) {
                        configRegistration = new ConfiguratorRegistration(serviceName + "@config", (dataId, configData) -> DsrRegistry.this.handleConfigData(url, configData, "configurators", listener));
                        configRegistration.setGroup("SOFA");
                        RegistryClientFactory.getRegistryClient(this.getUrl()).register(configRegistration);
                    }
                }
            }
        }
    }

    @Override
    public void doUnsubscribe(URL url, NotifyListener listener) {
        if (url.getParameter("subscribe", true)) {
            String serviceName = this.buildServiceName(url);
            RegistryClientFactory.getRegistryClient(this.getUrl()).unregister(serviceName, "SOFA", RegistryType.SUBSCRIBER);
        }
    }

    private void handleConfigData(URL url, ConfigData data, String category, NotifyListener notifyListener) {
        List<URL> urls;
        if (null != data) {
            String data1 = data.getData();
            try {
                data1 = DecryptUtil.decrypt(data1);
            } catch (NoSuchPaddingException | NoSuchAlgorithmException | UnsupportedEncodingException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException e) {
                e.printStackTrace();
            }
            if (!StringUtils.isEmpty(data1)) {
                String[] split = data1.split("#@#");
                List<String> providers = Arrays.stream(split).collect(Collectors.toList());
                urls = this.toUrlsWithEmpty(url, category, providers);
            } else {
                urls = this.toUrlsWithEmpty(url, category, null);
            }
        } else {
            urls = this.toUrlsWithEmpty(url, category, null);
        }
        if (notifyListener != null) {
            this.notify(url, notifyListener, urls);
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    private void waitAddress(String serviceName, CountDownLatch countDownLatch) {
        try {
            boolean isWaitedAddress = countDownLatch.await(this.getWaitAddressTimeout(), TimeUnit.MILLISECONDS);
            if (!isWaitedAddress) {
                LOGGER.warn("Subscribe data failed by dataId {}", serviceName);
            }
        } catch (Exception e) {
            LOGGER.error("Error ", e);
        }
    }

    private void handleRegistryData(URL url, UserData data, NotifyListener notifyListener, CountDownLatch latch) {
        try {
            List<URL> urls;
            if (null != data) {
                List<String> datas = this.flatUserData(data);
                urls = this.toUrlsWithEmpty(url, "providers", datas);
            } else {
                urls = this.toUrlsWithEmpty(url, "providers", null);
            }
            if (notifyListener != null) {
                this.notify(url, notifyListener, urls);
            }
        } finally {
            latch.countDown();
        }
    }

    private List<URL> toUrlsWithoutEmpty(URL consumer, List<String> providers) {
        if(CollectionUtils.isEmpty(providers)) return Collections.emptyList();
        List<URL> urls = new ArrayList<>();
        for (String provider : providers) {
            if (provider.contains("://")) {
                URL url = URL.valueOf(provider);
                if (UrlUtils.isMatch(consumer, url)) {
                    urls.add(url);
                }
            }
        }
        return urls;
    }

    private List<URL> toUrlsWithEmpty(URL consumer, String category, List<String> providers) {
        List<URL> urls = this.toUrlsWithoutEmpty(consumer, providers);
        if(CollectionUtils.isEmpty(urls)) {
            URL empty = consumer.setProtocol("empty").addParameter("category", category);
            urls.add(empty);
        }
        return urls;
    }

    private String buildServiceName(URL url) {
        StringBuilder buf = new StringBuilder();
        buf.append(url.getServiceInterface());
        String version = url.getParameter("version");
        buf.append(":");
        if (StringUtils.isNotEmpty(version) && !"0.0.0".equals(version)) {
            buf.append(version);
        }
        String group = url.getParameter("group");
        buf.append(":");
        if (StringUtils.isNotEmpty(group)) {
            buf.append(group);
        }
        buf.append("@dubbo");
        return buf.toString();
    }

    protected void printAddressData(String dataId, UserData userData) {
        List<String> datas;
        if (userData == null) {
            datas = new ArrayList<>(0);
        } else {
            datas = this.flatUserData(userData);
        }
        StringBuilder sb = new StringBuilder();
        for (String provider : datas) {
            sb.append("  >>> ").append(provider).append("\n");
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Receive updated RPC service addresses: service[{}]\n  .Available target addresses size [{}]\n {}", dataId, datas.size(), sb);
        }
    }

    private void addAttributesForPub(PublisherRegistration dsrRegistration) {
        dsrRegistration.setGroup("SOFA");
    }

    private void addAttributesForSub(SubscriberRegistration dsrRegistration) {
        dsrRegistration.setGroup("SOFA");
        dsrRegistration.setScopeEnum(ScopeEnum.global);
    }

    protected List<String> flatUserData(UserData userData) {
        List<String> result = new ArrayList<>();
        Map<String, List<String>> zoneData = userData.getZoneData();
        for (Map.Entry<String, List<String>> stringListEntry : zoneData.entrySet()) {
            for (String s : stringListEntry.getValue()) {
                try {
                    String decrypt = DecryptUtil.decrypt(s);
                    result.add(decrypt);
                } catch (NoSuchPaddingException | NoSuchAlgorithmException | UnsupportedEncodingException | BadPaddingException |
                         IllegalBlockSizeException | InvalidKeyException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    protected long getWaitAddressTimeout() {
        return wait_address_time;
    }

    @Override
    public String toString() {
        return "ConfigregRegistry{}";
    }
}
