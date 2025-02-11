package com.github.bannirui.msb.dubbo.filter;

import com.github.bannirui.msb.dubbo.auth.DubboAuthenticator;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;

@Activate(
    group = {"provider"},
    order = -10000
)
public class ProviderAuthFilter implements Filter {
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        URL url = invoker.getUrl();
        DubboAuthenticator.authenticate(invocation, url, invoker);
        return invoker.invoke(invocation);
    }
}
