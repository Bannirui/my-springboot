package com.github.bannirui.msb.dubbo.filter;

import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;

/** @deprecated */
@Deprecated
@Activate(
    group = {"consumer"},
    order = -7000
)
public class DubboEnvFilter implements Filter {
    private String currentEnv = null;

    public DubboEnvFilter() {
        this.currentEnv = System.getProperty("dubbo.env.name");
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        if (RpcContext.getContext().getAttachment("dubbo.env.name") == null) {
            String env = invocation.getAttachment("dubbo.env.name");
            if (env == null && this.currentEnv != null) {
                env = this.currentEnv;
            }
            if (env != null) {
                RpcContext.getContext().setAttachment("dubbo.env.name", env);
            }
        }
        return invoker.invoke(invocation);
    }
}
