package com.github.bannirui.msb.dubbo.filter;

import com.github.bannirui.msb.dubbo.config.AuthConfigChangeEventListener;
import com.github.bannirui.msb.dubbo.config.ConsumerAuthProperties;
import com.github.bannirui.msb.env.MsbEnvironmentMgr;
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
public class AuthConsumerFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(AuthConsumerFilter.class);

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String serviceTargetAddress = invoker.getInterface().getName() + "." + invocation.getMethodName();
        int index = -1;
        ConsumerAuthProperties consumerAuthProperties = AuthConfigChangeEventListener.getConsumerDubboAuthProperties();
        if (consumerAuthProperties != null) {
            List<String[]> consumerDubboAuthPackage = consumerAuthProperties.getDubboAuthPackage();
            for(int i = 0; i < consumerDubboAuthPackage.size(); ++i) {
                if (this.isMatch((String[])consumerDubboAuthPackage.get(i), serviceTargetAddress)) {
                    index = i;
                    break;
                }
            }
            if (index != -1) {
                try {
                    String key = consumerAuthProperties.getDubboAuthKey().get(index);
                    String appid = MsbEnvironmentMgr.getAppName();
                    RpcContext.getContext().setAttachment("dubbo.auth.sign.key", key);
                    RpcContext.getContext().setAttachment("dubbo.auth.sign.appid", appid);
                } catch (Exception e) {
                    logger.error("Consumer 获取key异常 请检查dubbo鉴权配置 errorMsg=", e);
                    return invoker.invoke(invocation);
                }
            }
        }

        return invoker.invoke(invocation);
    }

    private boolean isMatch(String[] packages, String serviceTargetAddress) {
        for (String address : packages) {
            if (StringUtils.startsWith(serviceTargetAddress, address)) {
                return true;
            }
        }
        return false;
    }
}
