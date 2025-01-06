package com.dianping.cat.message.io;

import com.dianping.cat.configuration.ClientConfigManager;
import com.dianping.cat.configuration.client.entity.Server;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;

public class DefaultTransportManager implements TransportManager, Initializable, LogEnabled {
    @Inject
    private ClientConfigManager m_configManager;
    @Inject
    private TcpSocketSender m_tcpSocketSender;
    private Logger m_logger;

    public void enableLogging(Logger logger) {
        this.m_logger = logger;
    }

    public MessageSender getSender() {
        return this.m_tcpSocketSender;
    }

    @Override
    public void initialize() throws InitializationException {
        List<Server> servers = this.m_configManager.getServers();
        if (!this.m_configManager.isCatEnabled()) {
            this.m_tcpSocketSender = null;
            this.m_logger.warn("CAT was DISABLED due to not initialized yet!");
        } else {
            List<InetSocketAddress> addresses = new ArrayList<>();
            servers.forEach(server -> {
                if (server.isEnabled()) {
                    addresses.add(new InetSocketAddress(server.getIp(), server.getPort()));
                }
            });
            this.m_logger.info("Remote CAT servers: " + addresses);
            if (addresses.isEmpty()) {
                throw new RuntimeException("All servers in configuration are disabled!\r\n" + servers);
            }
            this.m_tcpSocketSender.setServerAddresses(addresses);
            this.m_tcpSocketSender.initialize();
        }
    }
}
