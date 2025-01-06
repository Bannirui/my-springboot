package com.dianping.cat.configuration.client.entity;

import com.dianping.cat.configuration.client.BaseEntity;
import com.dianping.cat.configuration.client.IVisitor;

public class Bind extends BaseEntity<Bind> {

    private String m_ip;
    private int m_port = 2280;

    @Override
    public void accept(IVisitor visitor) {
        visitor.visitBind(this);
    }

    @Override
    public void mergeAttributes(Bind other) {
        if (other.getIp() != null) {
            this.m_ip = other.getIp();
        }
        this.m_port = other.getPort();
    }

    public String getIp() {
        return this.m_ip;
    }

    public int getPort() {
        return this.m_port;
    }

    public Bind setIp(String ip) {
        this.m_ip = ip;
        return this;
    }

    public Bind setPort(int port) {
        this.m_port = port;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Bind) {
            Bind _o = (Bind)obj;
            if (!this.equals(this.m_ip, _o.getIp())) {
                return false;
            } else {
                return this.m_port == _o.getPort();
            }
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = hash * 31 + (this.m_ip == null ? 0 : this.m_ip.hashCode());
        hash = hash * 31 + this.m_port;
        return hash;
    }
}
