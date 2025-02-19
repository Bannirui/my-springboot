package com.github.bannirui.msb.es.client;

import com.github.bannirui.msb.plugin.InterceptorUtil;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.net.ssl.SSLContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.support.HttpHeaders;
import org.springframework.util.Assert;

public class RestClients {
    private static final String LOG_ID_ATTRIBUTE = RestClients.class.getName() + ".LOG_ID";
    private static RestClientBuilder builder;

    public static RestClientBuilder getRestClientBuilder() {
        return builder;
    }

    public static RestClients.ElasticsearchRestClient create(ClientConfiguration clientConfiguration, int maxConnPerRoute, int maxConnTotal, HttpHost proxy, String connectionKeepAliveStrategyClass) throws Exception {
        Assert.notNull(clientConfiguration, "ClientConfiguration must not be null!");
        HttpHost[] httpHosts = formattedHosts(clientConfiguration.getEndpoints(), clientConfiguration.useSsl()).stream().map(HttpHost::create).toArray(HttpHost[]::new);
        builder = RestClient.builder(httpHosts);
        HttpHeaders headers = clientConfiguration.getDefaultHeaders();
        if (!headers.isEmpty()) {
            Header[] httpHeaders = headers.toSingleValueMap().entrySet().stream().map((it) -> new BasicHeader(it.getKey(), it.getValue())).toArray(Header[]::new);
            builder.setDefaultHeaders(httpHeaders);
        }
        builder.setHttpClientConfigCallback((clientBuilder) -> {
            Optional<SSLContext> sslContext = clientConfiguration.getSslContext();
            sslContext.ifPresent(clientBuilder::setSSLContext);
            if (ClientLogger.isEnabled()) {
                RestClients.HttpLoggingInterceptor interceptor = new RestClients.HttpLoggingInterceptor();
                clientBuilder.addInterceptorLast(interceptor);
                clientBuilder.addInterceptorLast(interceptor);
            }
            Duration connectTimeout = clientConfiguration.getConnectTimeout();
            Duration timeout = clientConfiguration.getSocketTimeout();
            RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
            if (!connectTimeout.isNegative()) {
                requestConfigBuilder.setConnectTimeout(Math.toIntExact(connectTimeout.toMillis()));
                requestConfigBuilder.setConnectionRequestTimeout(Math.toIntExact(connectTimeout.toMillis()));
            }
            if (!timeout.isNegative()) {
                requestConfigBuilder.setSocketTimeout(Math.toIntExact(timeout.toMillis()));
            }
            clientBuilder.setDefaultRequestConfig(requestConfigBuilder.build());
            clientBuilder.setMaxConnPerRoute(maxConnPerRoute).setMaxConnTotal(maxConnTotal);
            if (proxy != null) {
                clientBuilder.setProxy(proxy);
            }
            if (!StringUtils.isEmpty(connectionKeepAliveStrategyClass)) {
                try {
                    Class<?> aClass = Class.forName(connectionKeepAliveStrategyClass);
                    if (aClass != null && ConnectionKeepAliveStrategy.class.isAssignableFrom(aClass)) {
                        Object o = aClass.newInstance();
                        clientBuilder.setKeepAliveStrategy((ConnectionKeepAliveStrategy)o);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("初始化keepAlive策略失败！", e);
                }
            }
            return clientBuilder;
        });
        RestHighLevelClient client = InterceptorUtil.getProxyObj(RestHighLevelClient.class, new Class[]{RestClientBuilder.class}, new Object[]{builder}, "Es.Client");
        return () -> client;
    }

    private static List<String> formattedHosts(List<InetSocketAddress> hosts, boolean useSsl) {
        return hosts.stream().map((it) -> (useSsl ? "https" : "http") + "://" + it).collect(Collectors.toList());
    }

    private static class HttpLoggingInterceptor implements HttpResponseInterceptor, HttpRequestInterceptor {
        private HttpLoggingInterceptor() {
        }

        public void process(HttpRequest request, HttpContext context) throws IOException {
            String logId = (String)context.getAttribute(RestClients.LOG_ID_ATTRIBUTE);
            if (logId == null) {
                logId = ClientLogger.newLogId();
                context.setAttribute(RestClients.LOG_ID_ATTRIBUTE, logId);
            }
            if (request instanceof HttpEntityEnclosingRequest entityRequest && entityRequest.getEntity() != null) {
                HttpEntity entity = entityRequest.getEntity();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                entity.writeTo(buffer);
                if (!entity.isRepeatable()) {
                    entityRequest.setEntity(new ByteArrayEntity(buffer.toByteArray()));
                }
                ClientLogger.logRequest(logId, request.getRequestLine().getMethod(), request.getRequestLine().getUri(), "", () -> {
                    return new String(buffer.toByteArray());
                });
            } else {
                ClientLogger.logRequest(logId, request.getRequestLine().getMethod(), request.getRequestLine().getUri(), "");
            }
        }

        public void process(HttpResponse response, HttpContext context) {
            String logId = (String)context.getAttribute(RestClients.LOG_ID_ATTRIBUTE);
            ClientLogger.logRawResponse(logId, HttpStatus.resolve(response.getStatusLine().getStatusCode()));
        }
    }

    @FunctionalInterface
    public interface ElasticsearchRestClient extends Closeable {
        RestHighLevelClient rest();

        default RestClient lowLevelRest() {
            return this.rest().getLowLevelClient();
        }

        default void close() throws IOException {
            this.rest().close();
        }
    }
}
