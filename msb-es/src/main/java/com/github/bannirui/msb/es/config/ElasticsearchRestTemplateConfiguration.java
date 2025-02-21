package com.github.bannirui.msb.es.config;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.config.ElasticsearchConfigurationSupport;

@Configuration
public class ElasticsearchRestTemplateConfiguration extends ElasticsearchConfigurationSupport {
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    public RestHighLevelClient elasticsearchClient() {
        return this.restHighLevelClient;
    }
}
