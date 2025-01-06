package com.dianping.cat.status.model.entity;

import com.dianping.cat.status.model.BaseEntity;
import com.dianping.cat.status.model.IVisitor;
import java.util.LinkedHashMap;
import java.util.Map;

public class ExtensionDetail extends BaseEntity<ExtensionDetail> {
    private String m_id;
    private double m_value;
    private Map<String, String> m_dynamicAttributes = new LinkedHashMap<>();

    public ExtensionDetail(String id) {
        this.m_id = id;
    }

    @Override
    public void accept(IVisitor visitor) {
        visitor.visitExtensionDetail(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ExtensionDetail) {
            ExtensionDetail _o = (ExtensionDetail) obj;
            return this.equals(this.m_id, _o.getId());
        } else {
            return false;
        }
    }

    public String getDynamicAttribute(String name) {
        return this.m_dynamicAttributes.get(name);
    }

    public Map<String, String> getDynamicAttributes() {
        return this.m_dynamicAttributes;
    }

    public String getId() {
        return this.m_id;
    }

    public double getValue() {
        return this.m_value;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = hash * 31 + (this.m_id == null ? 0 : this.m_id.hashCode());
        return hash;
    }

    @Override
    public void mergeAttributes(ExtensionDetail other) {
        this.assertAttributeEquals(other, "extensionDetail", "id", this.m_id, other.getId());
        this.m_dynamicAttributes.putAll(other.getDynamicAttributes());
        this.m_value = other.getValue();
    }

    public void setDynamicAttribute(String name, String value) {
        this.m_dynamicAttributes.put(name, value);
    }

    public ExtensionDetail setId(String id) {
        this.m_id = id;
        return this;
    }

    public ExtensionDetail setValue(double value) {
        this.m_value = value;
        return this;
    }
}
