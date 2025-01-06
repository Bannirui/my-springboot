package com.dianping.cat.configuration.client.entity;

import com.dianping.cat.configuration.client.BaseEntity;
import com.dianping.cat.configuration.client.IVisitor;

public class Server extends BaseEntity<Server> {

    private String m_ip;
    private Integer m_port = 2280;
    private Integer m_httpPort = 8080;
    private Boolean m_enabled = true;

    public Server(String ip) {
        this.m_ip = ip;
    }

    public Boolean getEnabled() {
        return this.m_enabled;
    }

    public Integer getHttpPort() {
        return this.m_httpPort;
    }

    public String getIp() {
        return this.m_ip;
    }

    public Integer getPort() {
        return this.m_port;
    }

    public boolean isEnabled() {
        return this.m_enabled != null && this.m_enabled;
    }

    public Server setEnabled(Boolean enabled) {
        this.m_enabled = enabled;
        return this;
    }

    public Server setHttpPort(Integer httpPort) {
        this.m_httpPort = httpPort;
        return this;
    }

    public Server setIp(String ip) {
        this.m_ip = ip;
        return this;
    }

    public Server setPort(Integer port) {
        this.m_port = port;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Server) {
            Server _o = (Server) obj;
            return this.equals(this.m_ip, _o.getIp());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = hash * 31 + (this.m_ip == null ? 0 : this.m_ip.hashCode());
        return hash;
    }

    @Override
    public void accept(IVisitor visitor) {
        visitor.visitServer(this);
    }

    @Override
    public void mergeAttributes(Server other) {
        this.assertAttributeEquals(other, "server", "ip", this.m_ip, other.getIp());
        if (other.getPort() != null) {
            this.m_port = other.getPort();
        }
        if (other.getHttpPort() != null) {
            this.m_httpPort = other.getHttpPort();
        }
        if (other.getEnabled() != null) {
            this.m_enabled = other.getEnabled();
        }
    }
}
