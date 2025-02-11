package com.github.bannirui.msb.dubbo.filter;

import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;

@Activate(
    group = {"consumer"}
)
public class ConsumerPenetrateFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String application = invoker.getUrl().getParameter("application");
        if (application != null) {
            RpcContext.getContext().setAttachment("application", application);
        }
        return invoker.invoke(invocation);
    }
}
