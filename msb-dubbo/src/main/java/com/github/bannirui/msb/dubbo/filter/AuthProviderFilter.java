package com.github.bannirui.msb.dubbo.filter;

import com.github.bannirui.msb.dubbo.config.AuthConfigChangeEventListener;
import com.github.bannirui.msb.dubbo.config.ProviderAuthProperties;
import com.github.bannirui.msb.enums.ExceptionEnum;
import com.github.bannirui.msb.ex.ErrorCodeException;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class AuthProviderFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(AuthProviderFilter.class);

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String serviceTargetAddress = invoker.getInterface().getName() + "." + invocation.getMethodName();
        ProviderAuthProperties providerAuthProperties = AuthConfigChangeEventListener.getProviderDubboAuthProperties();
        if (providerAuthProperties != null) {
            String[] providerDubboAuthPackage = providerAuthProperties.getDubboAuthPackage();
            if (providerDubboAuthPackage != null && providerDubboAuthPackage.length > 0) {
                if (this.isMatch(providerDubboAuthPackage, serviceTargetAddress)) {
                    String consumerAppid = String.valueOf(RpcContext.getContext().getAttachment("dubbo.auth.sign.appid"));
                    String consumerKey = String.valueOf(RpcContext.getContext().getAttachment("dubbo.auth.sign.key"));
                    if (StringUtils.isNotEmpty(consumerAppid) && StringUtils.isNotEmpty(consumerKey)) {
                        if (providerAuthProperties.getDubboAuthSignAppId() != null && providerAuthProperties.getDubboAuthSignAppId().size() > 0 && providerAuthProperties.getDubboAuthSignKey() != null && providerAuthProperties.getDubboAuthSignKey().size() > 0) {
                            if (providerAuthProperties.getDubboAuthSignAppId().contains(consumerAppid)) {
                                int index = this.getKeyByAppid(consumerAppid, providerAuthProperties.getDubboAuthSignAppId());
                                String key = providerAuthProperties.getDubboAuthSignKey().get(index);
                                if (StringUtils.isNotEmpty(key) && key.equals(consumerKey)) {
                                    return invoker.invoke(invocation);
                                } else {
                                    logger.error("dubbo服务提供者鉴权失败您没有权限调用该服务");
                                    throw new ErrorCodeException(ExceptionEnum.CONFIG_DUBBO_AUTH_FAILURE, new Object[0]);
                                }
                            } else {
                                logger.error("dubbo服务提供者鉴权失败服务调用者提供的appid不在服务配置中");
                                throw new ErrorCodeException(ExceptionEnum.CONFIG_DUBBO_AUTH_FAILURE, new Object[0]);
                            }
                        } else {
                            logger.error("dubbo服务提供者鉴权,{}、{}配置错误", "dubbo.auth.provider.sign[%s].appid", "dubbo.auth.provider.sign[%s].key");
                            throw new ErrorCodeException(ExceptionEnum.CONFIG_DUBBO_AUTH_CONFIG_ERROR, new Object[0]);
                        }
                    } else {
                        logger.error("dubbo服务提供者鉴权错误，服务调用者未在RpcContext中提供consumerAppid获取consumerKey");
                        throw new ErrorCodeException(ExceptionEnum.CONFIG_DUBBO_AUTH_CONFIG_ERROR, new Object[0]);
                    }
                } else {
                    return invoker.invoke(invocation);
                }
            } else {
                logger.error("dubbo服务提供者鉴权,{}配置错误", "dubbo.auth.provider.package");
                throw new ErrorCodeException(ExceptionEnum.CONFIG_DUBBO_AUTH_CONFIG_ERROR, new Object[0]);
            }
        } else {
            return invoker.invoke(invocation);
        }
    }

    private boolean isMatch(String[] packages, String serviceTargetAddress) {
        for (String address : packages) {
            if (StringUtils.startsWith(serviceTargetAddress, address)) {
                return true;
            }
        }
        return false;
    }

    private int getKeyByAppid(String appid, List<String> appids) {
        for(int i = 0; i < appids.size(); ++i) {
            if (appids.get(i).equals(appid)) {
                return i;
            }
        }
        logger.error("dubbo服务调用者appid没有进行配置鉴权异常");
        throw new ErrorCodeException(ExceptionEnum.CONFIG_DUBBO_AUTH_FAILURE);
    }
}
