package com.github.bannirui.msb.es.config;

import com.github.bannirui.msb.es.client.EsClientFactory;
import com.github.bannirui.msb.es.property.EsProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchRestClientConfiguration {

    @Bean
    @ConfigurationProperties(
        prefix = "es"
    )
    public EsProperties esProperties() {
        return new EsProperties();
    }

    @Bean(
        initMethod = "init",
        destroyMethod = "close"
    )
    public EsClientFactory getFactory(EsProperties esProperties) {
        return EsClientFactory.build(esProperties);
    }

    @Bean
    public RestHighLevelClient getRestHighLevelClient(EsClientFactory esClientFactory) {
        return esClientFactory.getRestHighLevelClient();
    }

    @Bean
    public RestClientBuilder restClientBuilder(EsClientFactory esClientFactory) {
        return esClientFactory.getRestClientBuilder();
    }

    @Bean
    public RestClient restClient(EsClientFactory esClientFactory) {
        return esClientFactory.getRestClient();
    }
}
