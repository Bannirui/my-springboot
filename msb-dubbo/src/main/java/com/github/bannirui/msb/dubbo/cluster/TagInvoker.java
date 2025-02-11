package com.github.bannirui.msb.dubbo.cluster;

import com.github.bannirui.msb.dubbo.config.DubboProperties;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TagInvoker<T> implements Invoker<T> {
    private static final Logger logger = LoggerFactory.getLogger(TagInvoker.class);
    private Invoker<T> invoker;

    public TagInvoker(Invoker<T> invoker) {
        this.invoker = invoker;
    }

    @Override
    public Class<T> getInterface() {
        return this.invoker.getInterface();
    }

    @Override
    public Result invoke(Invocation invocation) throws RpcException {
        RpcContext context;
        if (StringUtils.isNotBlank(DubboProperties.consumerTag)) {
            context = RpcContext.getContext();
            if (!context.getAttachments().containsKey("dubbo.tag")) {
                context.setAttachment("dubbo.tag", DubboProperties.consumerTag);
            }
        }
        if (DubboProperties.consumerTagforce != null && DubboProperties.consumerTagforce) {
            context = RpcContext.getContext();
            if (!context.getAttachments().containsKey("dubbo.force.tag")) {
                context.setAttachment("dubbo.force.tag", "true");
            }
        }
        try {
            return this.invoker.invoke(invocation);
        } catch (RpcException e) {
            String message = e.getMessage();
            if (message != null && message.contains("No provider available for")) {
                context = RpcContext.getContext();
                String consumerTag = context.getAttachments().get("dubbo.tag");
                if (consumerTag != null) {
                    logger.error("No provider available with Tag : {}", consumerTag);
                } else if ("true".equals(context.getAttachments().get("dubbo.force.tag"))) {
                    logger.error("'tag-force' equals true, you must specify a tag name");
                }
            }
            throw e;
        }
    }

    @Override
    public URL getUrl() {
        return this.invoker.getUrl();
    }

    @Override
    public boolean isAvailable() {
        return this.invoker.isAvailable();
    }

    @Override
    public void destroy() {
        this.invoker.destroy();
    }

    @Override
    public String toString() {
        return this.getInterface() + " -> " + this.getUrl().toString();
    }
}
