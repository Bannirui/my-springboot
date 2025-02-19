package com.github.bannirui.msb.hbase.config;

import com.github.bannirui.msb.ex.FrameworkException;
import com.github.bannirui.msb.plugin.InterceptorUtil;
import com.github.bannirui.msb.register.AbstractBeanRegistrar;
import io.leopard.javahost.JavaHost;
import org.apache.commons.lang3.StringUtils;
import org.hbase.async.Config;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.Arrays;
import java.util.Properties;

public class HbaseConfiguration extends AbstractBeanRegistrar {

    /**
     * 缓存hbase配置 从msb配置文件中读取出来进行缓存
     * <ul>
     *     <li>key host</li>
     *     <li>val ip</li>
     * </ul>
     */
    private static final Properties props = new Properties();

    @Override
    public void registerBeans() {
        // 缓存hbase host配置
        this.initHbaseHost();
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        try {
            HBaseTemplate proxyObj = InterceptorUtil.getProxyObj(HBaseTemplate.class, new Class[] {Config.class, HbaseAnnotationParse.class}, new Object[] {this.getConfig(), new HbaseAnnotationParse()}, "HBase.Command");
            configurableListableBeanFactory.registerSingleton("hBaseTemplate", proxyObj);
        } catch (Exception e) {
            throw FrameworkException.getInstance(e, "加载HBase-Command拦截插件失败 errorMessage{0}", e);
        }
    }

    protected Config getConfig() {
        Config config = new Config();
        if (StringUtils.isNotEmpty(super.getProperty("hbase.client.retries.number"))) {
            config.overrideConfig("hbase.client.retries.number", super.getProperty("hbase.client.retries.number"));
        }
        if (StringUtils.isNotEmpty(super.getProperty("hbase.increments.buffer_size"))) {
            config.overrideConfig("hbase.increments.buffer_size", super.getProperty("hbase.increments.buffer_size"));
        }
        if (StringUtils.isNotEmpty(super.getProperty("hbase.increments.durable"))) {
            config.overrideConfig("hbase.increments.durable", super.getProperty("hbase.increments.durable"));
        }
        if (StringUtils.isNotEmpty(super.getProperty("hbase.ipc.client.connection.idle_timeout"))) {
            config.overrideConfig("hbase.ipc.client.connection.idle_timeout", super.getProperty("hbase.ipc.client.connection.idle_timeout"));
        }
        if (StringUtils.isNotEmpty(super.getProperty("hbase.ipc.client.socket.receiveBufferSize"))) {
            config.overrideConfig("hbase.ipc.client.socket.receiveBufferSize", super.getProperty("hbase.ipc.client.socket.receiveBufferSize"));
        }
        if (StringUtils.isNotEmpty(super.getProperty("hbase.ipc.client.socket.sendBufferSize"))) {
            config.overrideConfig("hbase.ipc.client.socket.sendBufferSize", super.getProperty("hbase.ipc.client.socket.sendBufferSize"));
        }
        if (StringUtils.isNotEmpty(super.getProperty("hbase.ipc.client.socket.timeout.connect"))) {
            config.overrideConfig("hbase.ipc.client.socket.timeout.connect", super.getProperty("hbase.ipc.client.socket.timeout.connect"));
        }
        if (StringUtils.isNotEmpty(super.getProperty("hbase.ipc.client.tcpkeepalive"))) {
            config.overrideConfig("hbase.ipc.client.tcpkeepalive", super.getProperty("hbase.ipc.client.tcpkeepalive"));
        }
        if (StringUtils.isNotEmpty(super.getProperty("hbase.ipc.client.tcpnodelay"))) {
            config.overrideConfig("hbase.ipc.client.tcpnodelay", super.getProperty("hbase.ipc.client.tcpnodelay"));
        }
        if (StringUtils.isNotEmpty(super.getProperty("hbase.kerberos.regionserver.principal"))) {
            config.overrideConfig("hbase.kerberos.regionserver.principal", super.getProperty("hbase.kerberos.regionserver.principal"));
        }
        if (StringUtils.isNotEmpty(super.getProperty("hbase.nsre.high_watermark"))) {
            config.overrideConfig("hbase.nsre.high_watermark", super.getProperty("hbase.nsre.high_watermark"));
        }
        if (StringUtils.isNotEmpty(super.getProperty("hbase.nsre.low_watermark"))) {
            config.overrideConfig("hbase.nsre.low_watermark", super.getProperty("hbase.nsre.low_watermark"));
        }
        if (StringUtils.isNotEmpty(super.getProperty("hbase.region_client.check_channel_write_status"))) {
            config.overrideConfig("hbase.region_client.check_channel_write_status",
                super.getProperty("hbase.region_client.check_channel_write_status"));
        }
        if (StringUtils.isNotEmpty(super.getProperty("hbase.region_client.inflight_limit"))) {
            config.overrideConfig("hbase.region_client.inflight_limit", super.getProperty("hbase.region_client.inflight_limit"));
        }
        if (StringUtils.isNotEmpty(super.getProperty("hbase.regionserver.kerberos.password"))) {
            config.overrideConfig("hbase.regionserver.kerberos.password", super.getProperty("hbase.regionserver.kerberos.password"));
        }
        if (StringUtils.isNotEmpty(super.getProperty("hbase.rpcs.batch.size"))) {
            config.overrideConfig("hbase.rpcs.batch.size", super.getProperty("hbase.rpcs.batch.size"));
        }
        if (StringUtils.isNotEmpty(super.getProperty("hbase.rpcs.buffered_flush_interval"))) {
            config.overrideConfig("hbase.rpcs.buffered_flush_interval", super.getProperty("hbase.rpcs.buffered_flush_interval"));
        }
        if (StringUtils.isNotEmpty(super.getProperty("hbase.rpc.protection"))) {
            config.overrideConfig("hbase.rpc.protection", super.getProperty("hbase.rpc.protection"));
        }
        if (StringUtils.isNotEmpty(super.getProperty("hbase.rpc.timeout"))) {
            config.overrideConfig("hbase.rpc.timeout", super.getProperty("hbase.rpc.timeout"));
        }
        if (StringUtils.isNotEmpty(super.getProperty("hbase.sasl.clientconfig"))) {
            config.overrideConfig("hbase.sasl.clientconfig", super.getProperty("hbase.sasl.clientconfig"));
        }
        if (StringUtils.isNotEmpty(super.getProperty("hbase.security.auth.94"))) {
            config.overrideConfig("hbase.security.auth.94", super.getProperty("hbase.security.auth.94"));
        }
        if (StringUtils.isNotEmpty(super.getProperty("hbase.security.auth.enable"))) {
            config.overrideConfig("hbase.security.auth.enable", super.getProperty("hbase.security.auth.enable"));
        }
        if (StringUtils.isNotEmpty(super.getProperty("hbase.security.authentication"))) {
            config.overrideConfig("hbase.security.authentication", super.getProperty("hbase.security.authentication"));
        }
        if (StringUtils.isNotEmpty(super.getProperty("hbase.security.simple.username"))) {
            config.overrideConfig("hbase.security.simple.username", super.getProperty("hbase.security.simple.username"));
        }
        if (StringUtils.isNotEmpty(super.getProperty("hbase.timer.tick"))) {
            config.overrideConfig("hbase.timer.tick", super.getProperty("hbase.timer.tick"));
        }
        if (StringUtils.isNotEmpty(super.getProperty("hbase.timer.ticks_per_wheel"))) {
            config.overrideConfig("hbase.timer.ticks_per_wheel", super.getProperty("hbase.timer.ticks_per_wheel"));
        }
        if (StringUtils.isNotEmpty(super.getProperty("hbase.zookeeper.getroot.retry_delay"))) {
            config.overrideConfig("hbase.zookeeper.getroot.retry_delay", super.getProperty("hbase.zookeeper.getroot.retry_delay"));
        }
        if (StringUtils.isNotEmpty(super.getProperty("hbase.zookeeper.quorum"))) {
            config.overrideConfig("hbase.zookeeper.quorum", super.getProperty("hbase.zookeeper.quorum"));
        }
        if (StringUtils.isNotEmpty(super.getProperty("hbase.zookeeper.session.timeout"))) {
            config.overrideConfig("hbase.zookeeper.session.timeout", super.getProperty("hbase.zookeeper.session.timeout"));
        }
        if (StringUtils.isNotEmpty(super.getProperty("hbase.zookeeper.znode.parent"))) {
            config.overrideConfig("hbase.zookeeper.znode.parent", super.getProperty("hbase.zookeeper.znode.parent"));
        }
        return config;
    }

    /**
     * 从msb配置文件中读取hbase host配置缓存起来
     */
    protected void initHbaseHost() {
        /**
         * msb配置的hbase host 格式为
         * host:ip
         * 多个用;分割
         * host1:ip1;host2:ip2
         */
        String hbaseHost = this.getProperty("hbase.host");
        if(StringUtils.isEmpty(hbaseHost)) {
            return;
        }
        try {
            String[] hosts = hbaseHost.split(";");
            Arrays.stream(hosts).forEach((host) -> {
                String[] domainNameAndIp = host.split(":");
                props.put(domainNameAndIp[0].toLowerCase(), domainNameAndIp[1]);
            });
            // jvm虚拟化dns
             JavaHost.updateVirtualDns(props);
        } catch (Exception e) {
            throw FrameworkException.getInstance("hbase.host虚拟dns映射失败 正确配置按照格式配置 hbase.host=host1:127.0.0.1;host2:127.0.0.1");
        }
    }
}
