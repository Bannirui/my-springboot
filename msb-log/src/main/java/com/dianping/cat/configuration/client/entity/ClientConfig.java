package com.dianping.cat.configuration.client.entity;

import com.dianping.cat.configuration.client.BaseEntity;
import com.dianping.cat.configuration.client.IVisitor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ClientConfig extends BaseEntity<ClientConfig> {

    private String m_mode;
    private Boolean m_enabled = true;
    private Boolean m_dumpLocked;
    private List<Server> m_servers = new ArrayList<>();
    private Map<String, Domain> m_domains = new LinkedHashMap<>();
    private Bind m_bind;
    private Map<String, Property> m_properties = new LinkedHashMap<>();
    private String m_baseLogDir = "target/catlog";
    private Map<String, String> m_dynamicAttributes = new LinkedHashMap<>();


    public ClientConfig addDomain(Domain domain) {
        this.m_domains.put(domain.getId(), domain);
        return this;
    }

    public ClientConfig addProperty(Property property) {
        this.m_properties.put(property.getName(), property);
        return this;
    }

    public ClientConfig addServer(Server server) {
        this.m_servers.add(server);
        return this;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof ClientConfig)) {
            return false;
        }
        ClientConfig _o = (ClientConfig) obj;
        if (!this.equals(this.m_mode, _o.getMode())) {
            return false;
        } else if (!this.equals(this.m_enabled, _o.getEnabled())) {
            return false;
        } else if (!this.equals(this.m_dumpLocked, _o.getDumpLocked())) {
            return false;
        } else if (!this.equals(this.m_servers, _o.getServers())) {
            return false;
        } else if (!this.equals(this.m_domains, _o.getDomains())) {
            return false;
        } else if (!this.equals(this.m_bind, _o.getBind())) {
            return false;
        } else if (!this.equals(this.m_properties, _o.getProperties())) {
            return false;
        } else if (!this.equals(this.m_baseLogDir, _o.getBaseLogDir())) {
            return false;
        } else {
            return this.m_dynamicAttributes.equals(_o.getDynamicAttributes());
        }
    }

    public Domain findDomain(String id) {
        return this.m_domains.get(id);
    }

    public Property findProperty(String name) {
        return this.m_properties.get(name);
    }

    public Server findServer(String ip) {
        for (Server s : this.m_servers) {
            if (this.equals(s.getIp(), ip)) {
                return s;
            }
        }
        return null;
    }

    public String getDynamicAttribute(String name) {
        return this.m_dynamicAttributes.get(name);
    }

    public Map<String, String> getDynamicAttributes() {
        return this.m_dynamicAttributes;
    }

    public String getBaseLogDir() {
        return this.m_baseLogDir;
    }

    public Bind getBind() {
        return this.m_bind;
    }

    public Map<String, Domain> getDomains() {
        return this.m_domains;
    }

    public Boolean getDumpLocked() {
        return this.m_dumpLocked;
    }

    public Boolean getEnabled() {
        return this.m_enabled;
    }

    public String getMode() {
        return this.m_mode;
    }

    public Map<String, Property> getProperties() {
        return this.m_properties;
    }

    public List<Server> getServers() {
        return this.m_servers;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = hash * 31 + (this.m_mode == null ? 0 : this.m_mode.hashCode());
        hash = hash * 31 + (this.m_enabled == null ? 0 : this.m_enabled.hashCode());
        hash = hash * 31 + (this.m_dumpLocked == null ? 0 : this.m_dumpLocked.hashCode());
        hash = hash * 31 + (this.m_servers == null ? 0 : this.m_servers.hashCode());
        hash = hash * 31 + (this.m_domains == null ? 0 : this.m_domains.hashCode());
        hash = hash * 31 + (this.m_bind == null ? 0 : this.m_bind.hashCode());
        hash = hash * 31 + (this.m_properties == null ? 0 : this.m_properties.hashCode());
        hash = hash * 31 + (this.m_baseLogDir == null ? 0 : this.m_baseLogDir.hashCode());
        hash = hash * 31 + this.m_dynamicAttributes.hashCode();
        return hash;
    }

    public boolean isDumpLocked() {
        return this.m_dumpLocked != null && this.m_dumpLocked;
    }

    public boolean isEnabled() {
        return this.m_enabled != null && this.m_enabled;
    }

    public Domain removeDomain(String id) {
        return this.m_domains.remove(id);
    }

    public Property removeProperty(String name) {
        return this.m_properties.remove(name);
    }

    public Server removeServer(String ip) {
        int len = this.m_servers.size();
        for (int i = 0; i < len; ++i) {
            Server server = this.m_servers.get(i);
            if (this.equals(server.getIp(), ip)) {
                return this.m_servers.remove(i);
            }
        }
        return null;
    }

    public void setDynamicAttribute(String name, String value) {
        this.m_dynamicAttributes.put(name, value);
    }

    public ClientConfig setBaseLogDir(String baseLogDir) {
        this.m_baseLogDir = baseLogDir;
        return this;
    }

    public ClientConfig setBind(Bind bind) {
        this.m_bind = bind;
        return this;
    }

    public ClientConfig setDumpLocked(Boolean dumpLocked) {
        this.m_dumpLocked = dumpLocked;
        return this;
    }

    public ClientConfig setEnabled(Boolean enabled) {
        this.m_enabled = enabled;
        return this;
    }

    public ClientConfig setMode(String mode) {
        this.m_mode = mode;
        return this;
    }

    @Override
    public void accept(IVisitor visitor) {
        visitor.visitConfig(this);
    }

    @Override
    public void mergeAttributes(ClientConfig other) {
        this.m_dynamicAttributes.putAll(other.getDynamicAttributes());
        if (other.getMode() != null) {
            this.m_mode = other.getMode();
        }
        if (other.getEnabled() != null) {
            this.m_enabled = other.getEnabled();
        }
        if (other.getDumpLocked() != null) {
            this.m_dumpLocked = other.getDumpLocked();
        }
    }
}
