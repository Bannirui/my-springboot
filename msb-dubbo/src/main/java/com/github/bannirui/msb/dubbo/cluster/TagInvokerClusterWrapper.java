package com.github.bannirui.msb.dubbo.cluster;

import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.Cluster;
import org.apache.dubbo.rpc.cluster.Directory;

public class TagInvokerClusterWrapper implements Cluster {
    private Cluster cluster;

    public TagInvokerClusterWrapper(Cluster cluster) {
        this.cluster = cluster;
    }

    @Override
    public <T> Invoker<T> join(Directory<T> directory, boolean buildFilterChain) throws RpcException {
        Invoker<T> targetInvoker = this.cluster.join(directory, buildFilterChain);
        return new TagInvoker(targetInvoker);
    }
}
