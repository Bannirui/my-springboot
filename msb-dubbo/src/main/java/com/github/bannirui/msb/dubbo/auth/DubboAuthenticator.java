package com.github.bannirui.msb.dubbo.auth;

import com.github.bannirui.msb.dubbo.config.AuthConfig;
import com.github.bannirui.msb.dubbo.config.AuthConfigChangeEventListener;
import com.github.bannirui.msb.dubbo.exception.RpcAuthenticationException;
import com.github.bannirui.msb.dubbo.util.SignatureUtils;
import com.github.bannirui.msb.env.MsbEnvironmentMgr;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.service.GenericService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DubboAuthenticator {
    private static final Logger logger = LoggerFactory.getLogger(DubboAuthenticator.class);

    public static void sign(Invocation invocation, Invoker<?> invoker, URL url, String secret) {
        String currentTime = String.valueOf(System.currentTimeMillis());
        RpcContext.getContext().setAttachment("signature", getSignature(url, invoker, invocation, secret, currentTime));
        RpcContext.getContext().setAttachment("timestamp", currentTime);
        RpcContext.getContext().setAttachment("app.id", MsbEnvironmentMgr.getAppName());
    }

    public static void authenticate(Invocation invocation, URL url, Invoker<?> invoker) throws RpcAuthenticationException {
        String consumerAppSecret = null;
        Map<String, AuthConfig> authMap = AuthConfigChangeEventListener.getAuthMap();
        Pair<String, AuthConfig> globalAuth = AuthConfigChangeEventListener.getGlobalAuth();
        if (authMap != null && authMap.size() != 0 || globalAuth != null) {
            String consumerAppId = invocation.getAttachment("app.id");
            String requestTimestamp = String.valueOf(invocation.getAttachment("timestamp"));
            String originSignature = String.valueOf(invocation.getAttachment("signature"));
            if (!StringUtils.isEmpty(consumerAppId) && !StringUtils.isEmpty(requestTimestamp) && !StringUtils.isEmpty(originSignature)) {
                String serviceTargetAddress = invoker.getInterface().getName() + "." + invocation.getMethodName();
                boolean isPreciseMatch = false;
                for (AuthConfig value : authMap.values()) {
                    String[] dubboAuthPackage = value.getDubboAuthPackage();
                    if (isMatch(dubboAuthPackage, serviceTargetAddress)) {
                        isPreciseMatch = true;
                        if (value.getAppSecret().containsKey(consumerAppId)) {
                            consumerAppSecret = value.getAppSecret().get(consumerAppId);
                            break;
                        }
                    }
                }
                if (consumerAppSecret == null && !isPreciseMatch) {
                    if (globalAuth == null) {
                        return;
                    }
                    AuthConfig globalRule = globalAuth.getRight();
                    if (globalRule == null) {
                        return;
                    }
                    String[] dubboAuthPackage = globalRule.getDubboAuthPackage();
                    if (isMatch(dubboAuthPackage, serviceTargetAddress) && globalRule.getAppSecret().containsKey(consumerAppId)) {
                        consumerAppSecret = globalRule.getAppSecret().get(consumerAppId);
                    }
                }
                if (consumerAppSecret == null) {
                    throw new RpcAuthenticationException("Failed to authenticate, consumer app secret not exist");
                } else {
                    String computeSignature = getSignature(url, invoker, invocation, consumerAppSecret, requestTimestamp);
                    boolean success = computeSignature.equals(originSignature);
                    if (!success) {
                        logger.error("appId {} originSignature {} computeSignature {}", consumerAppId, originSignature, computeSignature);
                        throw new RpcAuthenticationException("Failed to authenticate, signature is not correct");
                    }
                }
            } else {
                throw new RpcAuthenticationException("Failed to authenticate, maybe consumer not enable the auth");
            }
        }
    }

    private static boolean isMatch(String[] packages, String serviceTargetAddress) {
        for (String address : packages) {
            if (StringUtils.startsWith(serviceTargetAddress, address)) {
                return true;
            }
            if ("*".equals(address)) {
                return true;
            }
        }
        return false;
    }

    static String getSignature(URL url, Invoker<?> invoker, Invocation invocation, String secretKey, String time) {
        String methodName;
        if (!invocation.getMethodName().equals("$invoke")
            || invocation.getArguments() == null
            || invocation.getArguments().length != 3
            || (!invoker.getInterface().equals(GenericService.class) || !"consumer".equals(invoker.getUrl().getParameter("side")))
            && (invoker.getInterface().equals(GenericService.class) || !"provider".equals(invoker.getUrl().getParameter("side")))) {
            methodName = invocation.getMethodName();
        } else {
            methodName = ((String)invocation.getArguments()[0]).trim();
        }
        String requestString = String.format("%s#%s#%s#%s", getColonSeparatedKey(url), methodName, secretKey, time);
        return SignatureUtils.sign(requestString, secretKey);
    }

    static String getSignature(URL url, Invocation invocation, String secretKey, String time) {
        String requestString = String.format("%s#%s#%s#%s", getColonSeparatedKey(url), invocation.getMethodName(), secretKey, time);
        return SignatureUtils.sign(requestString, secretKey);
    }

    public static String getColonSeparatedKey(URL url) {
        StringBuilder serviceNameBuilder = new StringBuilder();
        serviceNameBuilder.append(url.getServiceInterface());
        append(serviceNameBuilder, "version", false, url);
        append(serviceNameBuilder, "group", false, url);
        return serviceNameBuilder.toString();
    }

    private static void append(StringBuilder target, String parameterName, boolean first, URL url) {
        String parameterValue = url.getParameter(parameterName);
        if (!StringUtils.isBlank(parameterValue)) {
            if (!first) {
                target.append(":");
            }
            target.append(parameterValue);
        } else {
            target.append(":");
        }
    }
}
