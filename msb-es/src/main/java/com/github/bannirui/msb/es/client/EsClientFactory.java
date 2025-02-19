package com.github.bannirui.msb.es.client;

import com.github.bannirui.msb.es.property.EsProperties;
import java.io.IOException;
import java.time.Duration;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.support.HttpHeaders;

public class EsClientFactory {
    private static Logger logger = LoggerFactory.getLogger(EsClientFactory.class);
    private EsProperties esProperties;
    private RestClient restClient;
    private RestClientBuilder restClientBuilder;
    private RestHighLevelClient restHighLevelClient;

    public static EsClientFactory build(EsProperties esProperties) {
        EsClientFactory esClientFactory = new EsClientFactory();
        esClientFactory.esProperties = esProperties;
        return esClientFactory;
    }

    public void init() throws Exception {
        String[] hostAndPorts = this.esProperties.getClusterNodes().split(",");
        ClientConfiguration.MaybeSecureClientConfigurationBuilder maybeSecureClientConfigurationBuilder = ClientConfiguration.builder().connectedTo(hostAndPorts);
        if (!StringUtils.isEmpty(this.esProperties.getUserName())) {
            HttpHeaders defaultHeaders = new HttpHeaders();
            defaultHeaders.setBasicAuth(this.esProperties.getUserName(), this.esProperties.getUserPass());
            maybeSecureClientConfigurationBuilder.withDefaultHeaders(defaultHeaders).withBasicAuth(this.esProperties.getUserName(), this.esProperties.getUserPass());
        }
        ClientConfiguration clientConfiguration = maybeSecureClientConfigurationBuilder
            .withConnectTimeout(Duration.ofSeconds(this.esProperties.getConnectTimeOutSeconds()))
            .withSocketTimeout(Duration.ofSeconds(this.esProperties.getSocketTimeOutSeconds()))
            .build();
        HttpHost proxy = null;
        if (!StringUtils.isEmpty(this.esProperties.getProxyHost())) {
            logger.info("proxyHost value: {}", this.esProperties.getProxyHost());
            String[] proxyHostAndPorts = this.esProperties.getProxyHost().split(":");
            if (proxyHostAndPorts.length != 2) {
                throw new IllegalArgumentException("proxyHost's value should like ip:port");
            }
            proxy = new HttpHost(proxyHostAndPorts[0], Integer.parseInt(proxyHostAndPorts[1]));
        }
        this.restHighLevelClient = RestClients.create(clientConfiguration, this.esProperties.getMaxConnectPerRoute(), this.esProperties.getMaxConnectTotal(), proxy, this.esProperties.getConnectionKeepAliveStrategyClass()).rest();
        RestClient
        this.restClientBuilder = RestClients.getRestClientBuilder();
        this.restClient = this.restHighLevelClient.getLowLevelClient();
        logger.info("create restHighLevelClient successful!");
    }

    public RestHighLevelClient getRestHighLevelClient() {
        return this.restHighLevelClient;
    }

    public RestClient getRestClient() {
        return this.restClient;
    }

    public RestClientBuilder getRestClientBuilder() {
        return this.restClientBuilder;
    }

    public void close() {
        if (this.restClient != null) {
            try {
                this.restClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        logger.info("close client!");
    }
}
