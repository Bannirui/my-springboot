package com.dianping.cat.configuration.client.entity;

import com.dianping.cat.configuration.client.BaseEntity;
import com.dianping.cat.configuration.client.IVisitor;

public class Domain extends BaseEntity<Domain> {

    private String m_id;
    private String m_ip;
    private Boolean m_enabled;
    private int m_maxMessageSize = 1000;

    public Domain(String id) {
        this.m_id = id;
    }

    public Boolean getEnabled() {
        return this.m_enabled;
    }

    public String getId() {
        return this.m_id;
    }

    public String getIp() {
        return this.m_ip;
    }

    public int getMaxMessageSize() {
        return this.m_maxMessageSize;
    }


    public boolean isEnabled() {
        return this.m_enabled != null && this.m_enabled;
    }

    public void mergeAttributes(Domain other) {
        this.assertAttributeEquals(other, "domain", "id", this.m_id, other.getId());
        if (other.getIp() != null) {
            this.m_ip = other.getIp();
        }
        if (other.getEnabled() != null) {
            this.m_enabled = other.getEnabled();
        }
        this.m_maxMessageSize = other.getMaxMessageSize();
    }

    public Domain setEnabled(Boolean enabled) {
        this.m_enabled = enabled;
        return this;
    }

    public Domain setId(String id) {
        this.m_id = id;
        return this;
    }

    public Domain setIp(String ip) {
        this.m_ip = ip;
        return this;
    }

    public Domain setMaxMessageSize(int maxMessageSize) {
        this.m_maxMessageSize = maxMessageSize;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Domain) {
            Domain _o = (Domain) obj;
            return this.equals(this.m_id, _o.getId());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = hash * 31 + (this.m_id == null ? 0 : this.m_id.hashCode());
        return hash;
    }

    @Override
    public void accept(IVisitor visitor) {
        visitor.visitDomain(this);
    }
}
