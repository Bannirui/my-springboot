package com.github.bannirui.msb.mq.sdk.utils;

import com.alibaba.fastjson2.JSON;
import com.google.common.base.Joiner;
import java.io.IOException;
import java.util.Map;
import javax.annotation.PreDestroy;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientHelper {
    private static final Logger logger = LoggerFactory.getLogger(HttpClientHelper.class);
    private static final int CONNECTION_REQUEST_TIMEOUT = 5000;
    private static final int CONNECT_TIMEOUT = 20000;
    private static final int SOCKET_TIMEOUT = 20000;
    private static final String CHAR_SET = "utf-8";
    private CloseableHttpClient httpClient;

    public HttpClientHelper() {
        this.init();
    }

    private void init() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(200);
        cm.setDefaultMaxPerRoute(20);
        this.httpClient = HttpClients.custom().setConnectionManager(cm).build();
    }

    public String post(String url, String jsonStr, int connectTimeout, int socketTimeout) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", "application/json; charset=utf-8");
        RequestConfig config = RequestConfig.custom().setConnectionRequestTimeout(5000).setConnectTimeout(connectTimeout).setSocketTimeout(socketTimeout).build();
        httpPost.setConfig(config);
        StringEntity se = new StringEntity(jsonStr, "utf-8");
        httpPost.setEntity(se);
        CloseableHttpResponse response = null;
        label: {
            String ans=null;
            try {
                response = this.httpClient.execute(httpPost);
                if (response.getStatusLine().getStatusCode() != 200) {
                    break label;
                }
                ans = EntityUtils.toString(response.getEntity(), "utf-8");
            } finally {
                if (null != response) {
                    IOUtils.close(response.getEntity().getContent());
                }
            }
            return ans;
        }
        logger.warn("request is failure,url:{},body:{},statusCode:{}", new Object[]{url, jsonStr, response.getStatusLine().getStatusCode()});
        throw new RuntimeException("request is failure,code:" + response.getStatusLine().getStatusCode());
    }

    public <T> T post(String url, Object dto, Class<T> clazz) throws IOException {
        String responseJson = this.post(url, JSON.toJSONString(dto), 20000, 20000);
        return JSON.parseObject(responseJson, clazz);
    }

    public <T> T get(String url, Map<String, String> params, Class<T> clazz) throws IOException {
        String responseJson = this.getWithString(url, params);
        return JSON.parseObject(responseJson, clazz);
    }

    public HttpResponse getWithHttpResponse(String url, Map<String, String> params) throws IOException {
        if (null != params && !params.isEmpty()) {
            String paramStr = Joiner.on("&").withKeyValueSeparator("=").useForNull("").join(params);
            url = url + "?" + paramStr;
        }
        return this.get(url, 20_000, 20_000);
    }

    public String getWithString(String url, Map<String, String> params) throws IOException {
        HttpResponse response = null;
        String ans=null;
        try {
            response = this.getWithHttpResponse(url, params);
            ans = EntityUtils.toString(response.getEntity(), "utf-8");
        } finally {
            if (response != null) {
                IOUtils.close(response.getEntity().getContent());
            }
        }
        return ans;
    }

    public HttpResponse get(String url, int connectTimeout, int socketTimeout) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        RequestConfig config = RequestConfig.custom().setConnectionRequestTimeout(5000).setConnectTimeout(connectTimeout).setSocketTimeout(socketTimeout).build();
        httpGet.setConfig(config);
        HttpResponse response = this.httpClient.execute(httpGet);
        if (response.getStatusLine().getStatusCode() != 200) {
            logger.error("request is failure,url:{},statusCode:{},{}", new Object[]{url, response.getStatusLine().getStatusCode(), EntityUtils.toString(response.getEntity(), "utf-8")});
            IOUtils.close(response.getEntity().getContent());
            throw new RuntimeException("request is failure,code:" + response.getStatusLine().getStatusCode());
        } else {
            return response;
        }
    }

    @PreDestroy
    public void destroy() {
        try {
            this.httpClient.close();
        } catch (IOException var2) {
            logger.error("httpClient destroy error ", var2);
        }
    }
}
