package com.dianping.cat.configuration;

import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Domain;
import com.dianping.cat.configuration.client.entity.Server;
import com.dianping.cat.configuration.client.transform.DefaultValidator;
import java.text.MessageFormat;
import java.util.Date;

public class ClientConfigValidator extends DefaultValidator {

    private ClientConfig m_config;

    private String getLocalAddress() {
        return NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
    }

    private void log(String severity, String message) {
        MessageFormat format = new MessageFormat("[{0,date,MM-dd HH:mm:ss.sss}] [{1}] [{2}] {3}");
        System.out.println(format.format(new Object[] {new Date(), severity, "cat", message}));
    }

    @Override
    public void visitConfig(ClientConfig config) {
        config.setMode("client");
        if (config.getServers().size() == 0) {
            config.setEnabled(false);
            this.log("WARN", "CAT client was disabled due to no CAT servers configured!");
        } else if (!config.isEnabled()) {
            this.log("WARN", "CAT client was globally disabled!");
        }
        this.m_config = config;
        super.visitConfig(config);
        if (this.m_config.isEnabled()) {
            this.m_config.getDomains().values().forEach(domain -> {
                if (!domain.isEnabled()) {
                    this.m_config.setEnabled(false);
                    this.log("WARN", "CAT client was disabled in domain(" + domain.getId() + ") explicitly!");
                }
            });
        }
    }

    public void visitDomain(Domain domain) {
        super.visitDomain(domain);
        if (domain.getEnabled() == null) {
            domain.setEnabled(true);
        }
        if (domain.getIp() == null) {
            domain.setIp(this.getLocalAddress());
        }
    }

    public void visitServer(Server server) {
        super.visitServer(server);
        if (server.getPort() == null) {
            server.setPort(2280);
        }
        if (server.getEnabled() == null) {
            server.setEnabled(true);
        }
    }
}
