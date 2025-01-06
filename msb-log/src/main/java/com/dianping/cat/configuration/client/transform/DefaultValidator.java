package com.dianping.cat.configuration.client.transform;

import com.dianping.cat.configuration.client.IVisitor;
import com.dianping.cat.configuration.client.entity.Bind;
import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Domain;
import com.dianping.cat.configuration.client.entity.Property;
import com.dianping.cat.configuration.client.entity.Server;
import java.util.Stack;

public class DefaultValidator implements IVisitor {
    private DefaultValidator.Path m_path = new DefaultValidator.Path();

    protected void assertRequired(String name, Object value) {
        if (value == null) {
            throw new RuntimeException(String.format("%s at path(%s) is required!", name, this.m_path));
        }
    }

    @Override
    public void visitBind(Bind bind) {
    }

    @Override
    public void visitConfig(ClientConfig config) {
        this.m_path.down("config");
        this.assertRequired("mode", config.getMode());
        this.visitConfigChildren(config);
        this.m_path.up("config");
    }

    protected void visitConfigChildren(ClientConfig config) {
        this.m_path.down("servers");
        config.getServers().forEach(this::visitServer);
        this.m_path.up("servers");
        config.getDomains().values().forEach(this::visitDomain);
        if (config.getBind() != null) {
            this.visitBind(config.getBind());
        }
        this.m_path.down("properties");
        config.getProperties().values().forEach(this::visitProperty);
        this.m_path.up("properties");
    }

    public void visitDomain(Domain domain) {
        this.m_path.down("domain");
        this.assertRequired("id", domain.getId());
        this.m_path.up("domain");
    }

    public void visitProperty(Property property) {
        this.m_path.down("property");
        this.assertRequired("name", property.getName());
        this.m_path.up("property");
    }

    public void visitServer(Server server) {
        this.m_path.down("server");
        this.assertRequired("ip", server.getIp());
        this.m_path.up("server");
    }

    static class Path {
        private Stack<String> m_sections = new Stack<>();

        public DefaultValidator.Path down(String nextSection) {
            this.m_sections.push(nextSection);
            return this;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (String section : this.m_sections) {
                sb.append('/').append(section);
            }
            return sb.toString();
        }

        public DefaultValidator.Path up(String currentSection) {
            if (!this.m_sections.isEmpty() && this.m_sections.peek().equals(currentSection)) {
                this.m_sections.pop();
                return this;
            } else {
                throw new RuntimeException("INTERNAL ERROR: stack mismatched!");
            }
        }
    }
}
