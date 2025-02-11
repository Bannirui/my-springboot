package com.github.bannirui.msb.dubbo.filter;

import com.github.bannirui.msb.dubbo.auth.DubboAuthenticator;
import com.github.bannirui.msb.util.DigestUtil;
import java.nio.charset.StandardCharsets;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Activate(
    group = {"consumer"},
    order = -10000
)
public class ConsumerSignFilter implements Filter {
    private Logger logger = LoggerFactory.getLogger(ConsumerSignFilter.class);
    private String secret = System.getProperty("app.secret");

    public ConsumerSignFilter() {
        try {
            if (this.secret != null) {
                this.secret = DigestUtil.encryptBASE64(DigestUtil.encryptMD5(this.secret.getBytes(StandardCharsets.UTF_8)));
            }
        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
        }
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        URL url = invoker.getUrl();
        if (this.secret != null) {
            DubboAuthenticator.sign(invocation, invoker, url, this.secret);
        }
        return invoker.invoke(invocation);
    }
}
