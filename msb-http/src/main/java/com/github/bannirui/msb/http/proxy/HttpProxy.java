package com.github.bannirui.msb.http.proxy;

import com.alibaba.fastjson.JSON;
import com.github.bannirui.msb.enums.ExceptionEnum;
import com.github.bannirui.msb.ex.ErrorCodeException;
import com.github.bannirui.msb.http.annotation.HeaderParam;
import com.github.bannirui.msb.http.annotation.HttpExecute;
import com.github.bannirui.msb.http.annotation.HttpHeader;
import com.github.bannirui.msb.http.annotation.HttpJsonExecute;
import com.github.bannirui.msb.http.annotation.HttpParam;
import com.github.bannirui.msb.http.annotation.HttpService;
import com.github.bannirui.msb.http.constructor.HttpRequestBodyConstructor;
import com.github.bannirui.msb.http.constructor.RequestEntity;
import com.github.bannirui.msb.http.enums.ParamDataType;
import com.github.bannirui.msb.http.enums.ResponseType;
import com.github.bannirui.msb.http.util.HttpClientUtils;
import com.github.bannirui.msb.util.XmlUtil;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpProxy implements InvocationHandler {
    private static final Logger logger = LoggerFactory.getLogger(HttpProxy.class);
    private static Map<String, HttpRequestBodyConstructor> constructorInstanceMap = new ConcurrentHashMap<>();
    private HttpPlaceholderResolver httpPlaceholderResolver = new HttpPlaceholderResolver();

    public HttpRequestBodyConstructor getConstructorInstance(Class<?> className) {
        if (!constructorInstanceMap.containsKey(className.getSimpleName())) {
            synchronized(this) {
                if (!constructorInstanceMap.containsKey(className.getSimpleName())) {
                    try {
                        HttpRequestBodyConstructor httpRequestBodyConstructor = (HttpRequestBodyConstructor)className.newInstance();
                        constructorInstanceMap.put(className.getSimpleName(), httpRequestBodyConstructor);
                    } catch (Exception e) {
                        throw new ErrorCodeException(ExceptionEnum.HTTP_CONSTRUCTOR_CREATE_ERROR);
                    }
                }
            }
        }
        return constructorInstanceMap.get(className.getSimpleName());
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return Object.class.equals(method.getDeclaringClass()) ? method.invoke(this, args) : this.run(method, args);
    }

    private int checkAnnotation(HttpExecute httpExecute, HttpJsonExecute httpJsonExecute) {
        if (httpExecute == null && httpJsonExecute == null) {
            throw new ErrorCodeException(ExceptionEnum.HTTP_NNOTATION_LACK_ERROR, new Object[0]);
        } else if (httpExecute != null && httpJsonExecute != null) {
            throw new ErrorCodeException(ExceptionEnum.HTTP_ANNOTATION_ERROR, new Object[0]);
        } else {
            return httpExecute != null ? 0 : 1;
        }
    }

    private Map<String, String> getHeaderMap(HttpHeader httpHeader, Properties properties) {
        Map<String, String> headersMap = new HashMap<>();
        Set<String> keys = properties.stringPropertyNames();
        if (httpHeader != null) {
            HeaderParam[] headers = httpHeader.headers();
            if (headers.length != 0) {
                for (HeaderParam param : headers) {
                    if (keys.contains(param.key())) {
                        headersMap.put(param.key(), this.httpPlaceholderResolver.replacePlaceholders(param.value(), properties));
                    } else {
                        headersMap.put(param.key(), param.value());
                    }
                }
            }
        }
        return headersMap;
    }

    public Object run(Method method, Object[] args) throws IOException {
        HttpClientUtils.setHttpServiceName(method.getDeclaringClass().getName() + "#" + method.getName());
        HttpExecute httpExecute = method.getAnnotation(HttpExecute.class);
        HttpHeader httpHeader = method.getAnnotation(HttpHeader.class);
        HttpJsonExecute httpJsonExecute = method.getAnnotation(HttpJsonExecute.class);
        int flag = this.checkAnnotation(httpExecute, httpJsonExecute);
        Properties httpMethodPlaceholderParamValue = this.httpPlaceholderResolver.getHttpMethodPlaceholderParamValue(method, args);
        HttpService annotation = method.getDeclaringClass().getAnnotation(HttpService.class);
        Object ret=null;
        try {
            HttpClientUtils.setThrow404Exception(!annotation.decode4xx5xx());
            String result;
            if (flag == 0) {
                result = this.sendHttpUrlEncodedRequest(httpExecute, this.getRequestParam(method, args), this.getHeaderMap(httpHeader, httpMethodPlaceholderParamValue), httpMethodPlaceholderParamValue);
                ret = this.parseStringReturn(httpExecute.type(), result, method);
                return ret;
            }
            if (flag != 1) {
                throw new ErrorCodeException(ExceptionEnum.HTTP_UNKNOWN_ERROR);
            }
            result = this.sendHttpJsonRequest(httpJsonExecute, this.getJsonRequestParam(method, args), this.getHeaderMap(httpHeader, httpMethodPlaceholderParamValue), httpMethodPlaceholderParamValue);
            ret = this.parseStringReturn(httpJsonExecute.type(), result, method);
        } finally {
            HttpClientUtils.setThrow404Exception(false);
        }
        return ret;
    }

    private Object getJsonRequestParam(Method method, Object[] args) {
        if (args != null && args.length > 0) {
            Parameter[] parameters = method.getParameters();
            for(int i = 0, sz=parameters.length; i < sz; ++i) {
                Annotation[] annotations = parameters[i].getAnnotations();
                if (annotations.length == 0) {
                    return args[i];
                }
            }
        }
        return null;
    }

    public Object parseStringReturn(ResponseType type, String parseStr, Method method) {
        Class<?> returnType = method.getReturnType();
        if (returnType.isAssignableFrom(String.class)) {
            return parseStr;
        } else if (type.equals(ResponseType.JSON)) {
            return JSON.parseObject(parseStr, method.getGenericReturnType());
        } else if (type.equals(ResponseType.XML)) {
            return XmlUtil.toBean(parseStr, returnType);
        } else {
            throw new ErrorCodeException(ExceptionEnum.HTTP_UNKNOWN_ERROR);
        }
    }

    public String sendHttpJsonRequest(HttpJsonExecute httpJsonExecute, Object param, Map<String, String> headersParam, Properties properties) throws IOException {
        String result = "";
        HttpRequestBodyConstructor httpRequestBodyConstructor;
        try {
            Class<?> constructor = httpJsonExecute.constructor();
            httpRequestBodyConstructor = this.getConstructorInstance(constructor);
        } catch (Exception e) {
            throw new ErrorCodeException(ExceptionEnum.HTTP_REQUEST_CONSTRUCTOR_ERROR);
        }
        if (httpRequestBodyConstructor != null) {
            RequestEntity requestEntity = httpRequestBodyConstructor.constructor(httpJsonExecute, param, headersParam);
            if (requestEntity != null) {
                try {
                    Object requestParam = requestEntity.getParam();
                    String requestParamStr;
                    if (param != null) {
                        if (param instanceof String) {
                            requestParamStr = requestParam.toString();
                        } else if (isWrapClass(param.getClass())) {
                            requestParamStr = requestParam.toString();
                        } else {
                            requestParamStr = JSON.toJSONString(requestParam);
                        }
                    } else {
                        requestParamStr = "";
                    }
                    String url = this.httpPlaceholderResolver.replacePlaceholders(requestEntity.getUrl(), properties);
                    result = switch (requestEntity.getJsonRequestMethod()) {
                        case POST -> HttpClientUtils.sendJsonPost(url, httpJsonExecute.connectTime(), httpJsonExecute.socketConnectTime(),
                            httpJsonExecute.connectionRequestTimeout(), requestEntity.getHeadersParam(), requestParamStr);
                        case PUT -> HttpClientUtils.sendJsonPut(url, httpJsonExecute.connectTime(), httpJsonExecute.socketConnectTime(),
                            httpJsonExecute.connectionRequestTimeout(), requestEntity.getHeadersParam(), requestParamStr);
                        default -> throw new ErrorCodeException(ExceptionEnum.HTTP_UNKNOWN_ERROR);
                    };
                    return result;
                } catch (Exception e) {
                    HttpProxy.logger.error(e.getMessage(), e);
                    throw e;
                }
            } else {
                throw new ErrorCodeException(ExceptionEnum.HTTP_REQUEST_ENTITY_ERROR);
            }
        } else {
            throw new ErrorCodeException(ExceptionEnum.HTTP_UNKNOWN_ERROR);
        }
    }

    public String sendHttpUrlEncodedRequest(HttpExecute httpExecute, Map<String, String> requestParam, Map<String, String> headersParam, Properties properties) throws IOException {
        String url = httpExecute.value();
        url = this.httpPlaceholderResolver.replacePlaceholders(url, properties);
        String result = switch (httpExecute.method()) {
            case GET ->
                HttpClientUtils.sendHttpGet(url, httpExecute.connectTime(), httpExecute.socketConnectTime(), httpExecute.connectionRequestTimeout(),
                    headersParam, requestParam);
            case PUT -> HttpClientUtils.sendHttpUrlEncodedPut(url, httpExecute.connectTime(), httpExecute.socketConnectTime(),
                httpExecute.connectionRequestTimeout(), headersParam, requestParam);
            case POST -> HttpClientUtils.sendHttpUrlEncodedPost(url, httpExecute.connectTime(), httpExecute.socketConnectTime(),
                httpExecute.connectionRequestTimeout(), headersParam, requestParam);
            case DELETE -> HttpClientUtils.sendHttpDelete(url, httpExecute.connectTime(), httpExecute.socketConnectTime(),
                httpExecute.connectionRequestTimeout(), headersParam, requestParam);
            default -> throw new ErrorCodeException(ExceptionEnum.HTTP_UNKNOWN_ERROR);
        };
        return result;
    }

    private Map<String, String> getRequestParam(Method method, Object[] args) {
        Map<String, String> paramMap = new HashMap<>();
        if (args != null) {
            Parameter[] parameters = method.getParameters();
            if (args.length != parameters.length) {
                throw new ErrorCodeException(ExceptionEnum.HTTP_ANNOTATION_PARAM_ERROR);
            }
            for(int i = 0; i < parameters.length; ++i) {
                HttpParam httpParam = parameters[i].getAnnotation(HttpParam.class);
                if (httpParam != null) {
                    if (args[i] != null) {
                        if (httpParam.dataType().equals(ParamDataType.JSON)) {
                            paramMap.put(httpParam.name(), JSON.toJSONString(args[i]));
                        } else if (httpParam.dataType().equals(ParamDataType.STRING)) {
                            paramMap.put(httpParam.name(), args[i].toString());
                        } else if (httpParam.dataType().equals(ParamDataType.XML)) {
                            paramMap.put(httpParam.name(), XmlUtil.toXml(args[i]));
                        }
                    } else {
                        paramMap.put(httpParam.name(), "");
                    }
                }
            }
        }
        return paramMap;
    }

    public static boolean isWrapClass(Class clz) {
        try {
            return ((Class)clz.getField("TYPE").get(null)).isPrimitive();
        } catch (Exception e) {
            return false;
        }
    }
}
