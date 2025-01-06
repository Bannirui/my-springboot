package com.github.bannirui.msb.log.cat.enhance;

import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Domain;
import com.dianping.cat.configuration.client.entity.Server;
import com.dianping.cat.configuration.client.transform.DefaultValidator;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

public class ClientConfigValidator extends DefaultValidator {
    private ClientConfig config;

    private String getLocalAddress() {
        return NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
    }

    private void log(String severity, String message) {
        MessageFormat format = new MessageFormat("[{0,date,MM-dd HH:mm:ss.sss}] [{1}] [{2}] {3}");
        System.out.println(format.format(new Object[]{new Date(), severity, "cat", message}));
    }

    public void visitConfig(ClientConfig config) {
        config.setMode("client");
        if (config.getServers().isEmpty()) {
            config.setEnabled(false);
            this.log("WARN", "CAT client was disabled due to no CAT servers configured!");
        } else if (!config.isEnabled()) {
            this.log("WARN", "CAT client was globally disabled!");
        }
        this.config = config;
        super.visitConfig(config);
        if (this.config.isEnabled()) {
            Map<String, Domain> domains = this.config.getDomains();
            if(Objects.nonNull(domains)) {
                domains.forEach((k, v) -> {
                    if (!v.isEnabled()) {
                        this.config.setEnabled(false);
                        this.log("WARN", "CAT client was disabled in domain(" + v.getId() + ") explicitly!");
                    }
                });
            }
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
        if (server.getEnabled() == null) {
            server.setEnabled(true);
        }
    }
}
