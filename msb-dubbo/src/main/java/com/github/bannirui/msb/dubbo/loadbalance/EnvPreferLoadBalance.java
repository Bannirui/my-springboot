package com.github.bannirui.msb.dubbo.loadbalance;

import java.util.ArrayList;
import java.util.List;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.cluster.loadbalance.RandomLoadBalance;

/** @deprecated */
@Deprecated
public class EnvPreferLoadBalance extends RandomLoadBalance {
    private String currentEnv = null;

    public EnvPreferLoadBalance() {
        this.currentEnv = System.getProperty("dubbo.env.name");
    }

    @Override
    protected <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation) {
        String env = null;
        if (invocation.getAttachment("dubbo.env.name") != null) {
            env = invocation.getAttachment("dubbo.env.name");
        } else if (this.currentEnv != null) {
            env = this.currentEnv;
        } else if (RpcContext.getContext().getAttachment("dubbo.env.name") != null) {
            env = RpcContext.getContext().getAttachment("dubbo.env.name");
        }
        if (env != null) {
            RpcContext.getContext().setAttachment("dubbo.env.name", env);
            List<Invoker<T>> envInvokers = new ArrayList<>();
            for (Invoker<T> invoker : invokers) {
                if (env.equals(invoker.getUrl().getParameter("dubbo.env.name"))) {
                    envInvokers.add(invoker);
                }
            }
            if (!envInvokers.isEmpty()) {
                return super.doSelect(envInvokers, url, invocation);
            }
        }
        return super.doSelect(invokers, url, invocation);
    }
}
