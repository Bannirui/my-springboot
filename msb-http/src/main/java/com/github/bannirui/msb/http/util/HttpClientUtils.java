package com.github.bannirui.msb.http.util;

import com.github.bannirui.msb.ex.FrameworkException;
import com.github.bannirui.msb.http.config.HttpConfigPropertiesProvider;
import com.github.bannirui.msb.http.ex.SoaHttpException;
import com.github.bannirui.msb.http.filter.HttpClientFilter;
import com.github.bannirui.msb.http.filter.HttpClientFilterChain;
import com.github.bannirui.msb.http.filter.HttpReqAndRsp;
import com.github.bannirui.msb.http.interceptor.HttpInterceptor;
import com.github.bannirui.msb.plugin.PluginConfigManager;
import com.github.bannirui.msb.plugin.PluginDecorator;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientUtils {
    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);
    private static final CloseableHttpClient HTTP_CLIENT;
    public static final String CHAR_SET = "UTF-8";
    public static final String CHAR_SET_HEADER_KEY = "charset";
    public static final String HTTP_SERVICE_NAME_KEY = "httpServieName";
    private static ThreadLocal<String> httpServiceNameTL = new ThreadLocal<>();
    private static ThreadLocal<Boolean> throw404Exception = new ThreadLocal<>();
    private static final List<HttpClientFilter> filterList = new ArrayList<>();
    private static ThreadLocal<HttpClientFilterChain> httpClientFilterChainThreadLocal = new ThreadLocal<>();
    public static final Integer DEFAULT_CONNECTION_REQUEST_TIMEOUT = 60000;
    private static PoolingHttpClientConnectionManager cm = null;
    private static final String CONTENT_VALUE_FORM = "application/x-www-form-urlencoded";
    private static final String CONTENT_VALUE_JSON = "application/json";
    private static final String CONTENT_TYPE = "Content-type";
    public static final ContentType TEXT_PLAIN_UTF_8;

    static {
        cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(HttpConfigPropertiesProvider.getHttpConfigProperties().getMaxConnectionSize());
        cm.setDefaultMaxPerRoute(HttpConfigPropertiesProvider.getHttpConfigProperties().getMaxPerRouteSize());
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create().setConnectionManager(cm).evictIdleConnections(HttpConfigPropertiesProvider.getHttpConfigProperties().getMaxIdleSecond(), TimeUnit.SECONDS);
        Set<String> httpInterceptors = PluginConfigManager.getPropertyValueSet("com.github.bannirui.msb.http.interceptor.HttpInterceptor");
        if (CollectionUtils.isNotEmpty(httpInterceptors)) {
            for (String interceptor : httpInterceptors) {
                try {
                    HttpInterceptor httpInterceptor = (HttpInterceptor)Class.forName(interceptor).newInstance();
                    httpClientBuilder.addInterceptorFirst((HttpRequestInterceptor)httpInterceptor);
                    httpClientBuilder.addInterceptorFirst((HttpResponseInterceptor)httpInterceptor);
                    httpClientBuilder.setRetryHandler(httpInterceptor);
                } catch (Exception e) {
                    throw FrameworkException.getInstance(e, "Http加载插件异常,请检查配置文件[{0}]", "com.github.bannirui.msb.http.interceptor.HttpInterceptor");
                }
            }
        }
        HTTP_CLIENT = httpClientBuilder.build();
        List<PluginDecorator<Class<?>>> orderedPluginClasses = PluginConfigManager.getOrderedPluginClasses("com.github.bannirui.msb.http.filter.HttpClientFilter", true);
        if (orderedPluginClasses != null) {
            orderedPluginClasses.forEach((o) -> {
                try {
                    HttpClientFilter filter = (HttpClientFilter)Class.forName(o.getPlugin().getName()).newInstance();
                    filterList.add(filter);
                } catch (Exception e) {
                    throw FrameworkException.getInstance(e, "Http加载插件异常,请检查配置文件[{0}]", "com.github.bannirui.msb.http.filter.HttpClientFilter");
                }
            });
        }
        TEXT_PLAIN_UTF_8 = ContentType.create("text/plain", Consts.UTF_8);
    }

    private HttpClientUtils() {
        throw new IllegalStateException("Utility class");
    }

    private static String getCharSet(Map<String, String> header) {
        if(Objects.isNull(header)) {
            return CHAR_SET;
        }
        String ret = header.get(CHAR_SET_HEADER_KEY);
        if(Objects.isNull(ret)) {
            return CHAR_SET;
        }
        return ret;
    }

    private static String getContentType(String contentType, String charSet) {
        return MessageFormat.format("{0};charset={1}", contentType, charSet);
    }

    public static void setHttpServiceName(String httpServiceName) {
        httpServiceNameTL.set(httpServiceName);
    }

    public static void setThrow404Exception(Boolean decode404) {
        throw404Exception.set(decode404);
    }

    public static boolean getThrow404Exception() {
        Boolean ret=null;
        if(Objects.isNull(ret=throw404Exception.get())) {
            return false;
        }
        return ret;
    }

    public static RequestConfig getRequestConfig(int connectTime, int socketConnectTime) {
        return getRequestConfig(connectTime, socketConnectTime, 600_000);
    }

    public static RequestConfig getRequestConfig(int connectTimeout, int socketTimeout, int connectionRequestTimeout) {
        return RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(connectTimeout).setConnectionRequestTimeout(connectionRequestTimeout).build();
    }

    public static String sendHttpGet(String url, int connectTime, int socketConnectTime, Map<String, String> header, Map<String, String> params) throws IOException {
        return sendHttpGet(url, connectTime, socketConnectTime, DEFAULT_CONNECTION_REQUEST_TIMEOUT, header, params);
    }

    public static void initHeader(HttpUriRequest request, Map<String, String> header) {
        if(Objects.isNull(header)) return;
        header.forEach(request::setHeader);
    }

    private static String execute(HttpUriRequest request, Map<String, String> header) throws IOException {
        if (header != null) {
            initHeader(request, header);
        }
        String body = null;
        HttpResponse response = null;
        HttpEntity entity = null;
        String ret=null;
        try {
            HttpReqAndRsp httpReqAndRsp = new HttpReqAndRsp(request, response);
            beforeExecute(httpReqAndRsp);
            response = httpReqAndRsp.getHttpResponse();
            if (response == null) {
                response = HTTP_CLIENT.execute(request);
            }
            entity = response.getEntity();
            if (response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() < 300) {
                body = EntityUtils.toString(entity, getCharSet(header));
            } else {
                if (getThrow404Exception()) {
                    throw SoaHttpException.errorStatus(httpServiceNameTL.get(), request, response);
                }
                body = EntityUtils.toString(entity, getCharSet(header));
            }
            ret = body;
        } finally {
            EntityUtils.consume(entity);
            if (response != null && response instanceof CloseableHttpResponse resp) {
                resp.close();
            }
        }
        return ret;
    }

    private static void beforeExecute(HttpReqAndRsp httpReqAndRsp) {
        HttpClientFilterChain httpClientFilterChain = httpClientFilterChainThreadLocal.get();
        if (httpClientFilterChain == null && CollectionUtils.isNotEmpty(filterList)) {
            httpClientFilterChain = HttpClientFilterChain.build(filterList);
            httpClientFilterChainThreadLocal.set(httpClientFilterChain);
        }
        if (httpClientFilterChain != null) {
            httpClientFilterChain.reset();
            httpClientFilterChain.setAttachment("httpServiceName", httpServiceNameTL.get());
            httpClientFilterChain.doFilter(httpReqAndRsp);
        }
    }

    public static String sendHttpGet(String url, int connectTime, int socketConnectTime, int connectionRequestTimeout, Map<String, String> header, Map<String, String> params) throws IOException {
        RequestConfig requestConfig = getRequestConfig(connectTime, socketConnectTime, connectionRequestTimeout);
        String charSet = getCharSet(header);
        url = urlParamJoint(url, params, charSet);
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(requestConfig);
        httpGet.setHeader("Content-type", getContentType("application/x-www-form-urlencoded", charSet));
        return execute(httpGet, header);
    }

    public static String sendHttpDelete(String url, int connectTime, int socketConnectTime, Map<String, String> header, Map<String, String> params) throws IOException {
        return sendHttpDelete(url, connectTime, socketConnectTime, DEFAULT_CONNECTION_REQUEST_TIMEOUT, header, params);
    }

    public static String sendHttpDelete(String url, int connectTime, int socketConnectTime, int connectionRequestTimeout, Map<String, String> header, Map<String, String> params) throws IOException {
        String body = null;
        RequestConfig requestConfig = getRequestConfig(connectTime, socketConnectTime, connectionRequestTimeout);
        String charSet = getCharSet(header);
        url = urlParamJoint(url, params, charSet);
        HttpDelete httpDelete = new HttpDelete(url);
        httpDelete.setConfig(requestConfig);
        httpDelete.setHeader("Content-type", getContentType("application/x-www-form-urlencoded", charSet));
        return execute(httpDelete, header);
    }

    public static String sendHttpUrlEncodedPut(String url, int connectTime, int socketConnectTime, Map<String, String> header, Map<String, String> param) throws IOException {
        return sendHttpUrlEncodedPut(url, connectTime, socketConnectTime, DEFAULT_CONNECTION_REQUEST_TIMEOUT, header, param);
    }

    public static String sendHttpUrlEncodedPut(String url, int connectTime, int socketConnectTime, int connectionRequestTimeout, Map<String, String> header, Map<String, String> param) throws IOException {
        String body = null;
        RequestConfig requestConfig = getRequestConfig(connectTime, socketConnectTime, connectionRequestTimeout);
        HttpPut httpPut = new HttpPut(url);
        httpPut.setConfig(requestConfig);
        List<NameValuePair> nvps = new ArrayList<>();
        if (param != null) {
            param.forEach((key, value) -> nvps.add(new BasicNameValuePair(key, value)));
        }
        String charSet = getCharSet(header);
        httpPut.setEntity(new UrlEncodedFormEntity(nvps, charSet));
        httpPut.setHeader("Content-type", getContentType("application/x-www-form-urlencoded", charSet));
        return execute(httpPut, header);
    }

    public static String sendHttpUrlEncodedPost(String url, int connectTime, int socketConnectTime, Map<String, String> header, Map<String, String> param) throws IOException {
        return sendHttpUrlEncodedPost(url, connectTime, socketConnectTime, DEFAULT_CONNECTION_REQUEST_TIMEOUT, header, param);
    }

    public static String sendHttpUrlEncodedPost(String url, int connectTime, int socketConnectTime, int connectionRequestTimeout, Map<String, String> header, Map<String, String> param) throws IOException {
        String body = null;
        RequestConfig requestConfig = getRequestConfig(connectTime, socketConnectTime, connectionRequestTimeout);
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(requestConfig);
        List<NameValuePair> nvps = new ArrayList<>();
        if (param != null) {
            param.forEach((key, value) -> nvps.add(new BasicNameValuePair(key, value)));
        }
        String charSet = getCharSet(header);
        httpPost.setEntity(new UrlEncodedFormEntity(nvps, charSet));
        httpPost.setHeader("Content-type", getContentType("application/x-www-form-urlencoded", charSet));
        return execute(httpPost, header);
    }

    public static String sendJsonPost(String url, int connectTime, int socketConnectTime, Map<String, String> header, String param) throws IOException {
        return sendJsonPost(url, connectTime, socketConnectTime, DEFAULT_CONNECTION_REQUEST_TIMEOUT, header, param);
    }

    public static String sendJsonPost(String url, int connectTime, int socketConnectTime, int connectionRequestTimeout, Map<String, String> header, String param) throws
        IOException {
        String body = null;
        RequestConfig requestConfig = getRequestConfig(connectTime, socketConnectTime, connectionRequestTimeout);
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(requestConfig);
        StringEntity stringEntity = new StringEntity(param, getCharSet(header));
        stringEntity.setContentEncoding(getCharSet(header));
        stringEntity.setContentType("application/json");
        httpPost.setEntity(stringEntity);
        return execute(httpPost, header);
    }

    public static String sendJsonPut(String url, int connectTime, int socketConnectTime, Map<String, String> header, String param) throws IOException {
        return sendJsonPut(url, connectTime, socketConnectTime, DEFAULT_CONNECTION_REQUEST_TIMEOUT, header, param);
    }

    public static String sendJsonPut(String url, int connectTime, int socketConnectTime, int connectionRequestTimeout, Map<String, String> header, String param) throws IOException {
        String body = null;
        RequestConfig requestConfig = getRequestConfig(connectTime, socketConnectTime, connectionRequestTimeout);
        HttpPut httpPut = new HttpPut(url);
        httpPut.setConfig(requestConfig);
        StringEntity stringEntity = new StringEntity(param, getCharSet(header));
        stringEntity.setContentEncoding(getCharSet(header));
        stringEntity.setContentType("application/json");
        httpPut.setEntity(stringEntity);
        return execute(httpPut, header);
    }

    public static String urlParamJoint(String url, Map<String, String> params) throws IOException {
        return urlParamJoint(url, params, "UTF-8");
    }

    public static String urlParamJoint(String url, Map<String, String> params, String charset) throws IOException {
        if (params != null && !params.isEmpty()) {
            List<NameValuePair> pairs = new ArrayList<>(params.size());
            params.forEach((key, value) -> pairs.add(new BasicNameValuePair(key, value)));
            url = url + "?" + EntityUtils.toString(new UrlEncodedFormEntity(pairs, charset));
        }
        return url;
    }

    public static String sendFileUpload(String url, Map<String, String> param, String name, Object file, int connectTime, int socketConnectTime) throws IOException {
        return sendFileUpload(url, param, name, file, connectTime, socketConnectTime, DEFAULT_CONNECTION_REQUEST_TIMEOUT);
    }

    public static String sendFileUpload(String url, Map<String, String> param, String name, Object file, int connectTime, int socketConnectTime, int connectionRequestTimeout) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        RequestConfig requestConfig = getRequestConfig(connectTime, socketConnectTime, connectionRequestTimeout);
        httpPost.setConfig(requestConfig);
        MultipartEntityBuilder mEntityBuilder = MultipartEntityBuilder.create();
        mEntityBuilder.setCharset(StandardCharsets.UTF_8);
        mEntityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        String filename = param.get("filename");
        if (filename != null && !filename.contains(".")) {
            String ext = param.get("ext");
            if (ext != null) {
                filename = filename + "." + ext;
            }
        }
        if (file instanceof byte[]) {
            mEntityBuilder.addBinaryBody(name, (byte[])file, ContentType.MULTIPART_FORM_DATA, filename);
        } else if (file instanceof InputStream f) {
            mEntityBuilder.addBinaryBody(name, f, ContentType.MULTIPART_FORM_DATA, filename);
        }
        param.forEach((key, value) -> {
            mEntityBuilder.addTextBody(key, value, TEXT_PLAIN_UTF_8);
        });
        httpPost.setEntity(mEntityBuilder.build());
        return execute(httpPost, null);
    }

    public static byte[] sendFileDownload(String url, Map<String, String> param, int connectTime, int socketConnectTime) throws IOException {
        return sendFileDownload(url, param, connectTime, socketConnectTime, DEFAULT_CONNECTION_REQUEST_TIMEOUT);
    }

    public static byte[] sendFileDownload(String url, Map<String, String> param, int connectTime, int socketConnectTime, int connectionRequestTimeout) throws IOException {
        RequestConfig requestConfig = getRequestConfig(connectTime, socketConnectTime, connectionRequestTimeout);
        url = urlParamJoint(url, param, getCharSet(null));
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(requestConfig);
        CloseableHttpResponse response = HTTP_CLIENT.execute(httpGet);
        HttpEntity httpEntity = response.getEntity();
        if(Objects.isNull(httpEntity)) return null;
        byte[] ret = null;
        try {
            InputStream inputStream = httpEntity.getContent();
            ret = inputStreamToByte(inputStream);
        } finally {
            EntityUtils.consume(httpEntity);
            response.close();
        }
        return ret;
    }

    private static byte[] inputStreamToByte(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return null;
        }
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[1024];
        byte[] ret = null;
        try {
            int len = 0;
            while((len = inputStream.read(buff, 0, buff.length)) != -1) {
                swapStream.write(buff, 0, len);
                swapStream.flush();
            }
            ret = swapStream.toByteArray();
        } finally {
            swapStream.close();
            inputStream.close();
        }
        return ret;
    }
}
