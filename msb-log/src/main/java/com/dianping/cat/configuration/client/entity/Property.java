package com.dianping.cat.configuration.client.entity;

import com.dianping.cat.configuration.client.BaseEntity;
import com.dianping.cat.configuration.client.IVisitor;

public class Property extends BaseEntity<Property> {
    private String m_name;
    private String m_text;

    public Property(String name) {
        this.m_name = name;
    }

    public String getName() {
        return this.m_name;
    }

    public String getText() {
        return this.m_text;
    }

    public Property setName(String name) {
        this.m_name = name;
        return this;
    }

    public Property setText(String text) {
        this.m_text = text;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Property) {
            Property _o = (Property) obj;
            return this.equals(this.m_name, _o.getName());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = hash * 31 + (this.m_name == null ? 0 : this.m_name.hashCode());
        return hash;
    }

    @Override
    public void accept(IVisitor visitor) {
        visitor.visitProperty(this);
    }

    @Override
    public void mergeAttributes(Property other) {
        this.assertAttributeEquals(other, "property", "name", this.m_name, other.getName());
    }
}
