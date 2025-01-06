package com.dianping.cat.status.model.entity;

import com.dianping.cat.status.model.BaseEntity;
import com.dianping.cat.status.model.IVisitor;
import java.util.LinkedHashMap;
import java.util.Map;

public class Extension extends BaseEntity<Extension> {
    private String m_id;
    private String m_description;
    private Map<String, ExtensionDetail> m_details = new LinkedHashMap<>();
    private Map<String, String> m_dynamicAttributes = new LinkedHashMap<>();

    public Extension(String id) {
        this.m_id = id;
    }

    @Override
    public void accept(IVisitor visitor) {
        visitor.visitExtension(this);
    }

    public Extension addExtensionDetail(ExtensionDetail extensionDetail) {
        this.m_details.put(extensionDetail.getId(), extensionDetail);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Extension) {
            Extension _o = (Extension) obj;
            return this.equals(this.m_id, _o.getId());
        } else {
            return false;
        }
    }

    public ExtensionDetail findExtensionDetail(String id) {
        return this.m_details.get(id);
    }

    public ExtensionDetail findOrCreateExtensionDetail(String id) {
        ExtensionDetail extensionDetail = this.m_details.get(id);
        if (extensionDetail == null) {
            synchronized (this.m_details) {
                extensionDetail = this.m_details.get(id);
                if (extensionDetail == null) {
                    extensionDetail = new ExtensionDetail(id);
                    this.m_details.put(id, extensionDetail);
                }
            }
        }
        return extensionDetail;
    }

    public String getDynamicAttribute(String name) {
        return this.m_dynamicAttributes.get(name);
    }

    public Map<String, String> getDynamicAttributes() {
        return this.m_dynamicAttributes;
    }

    public String getDescription() {
        return this.m_description;
    }

    public Map<String, ExtensionDetail> getDetails() {
        return this.m_details;
    }

    public String getId() {
        return this.m_id;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = hash * 31 + (this.m_id == null ? 0 : this.m_id.hashCode());
        return hash;
    }

    @Override
    public void mergeAttributes(Extension other) {
        this.assertAttributeEquals(other, "extension", "id", this.m_id, other.getId());
        this.m_dynamicAttributes.putAll(other.getDynamicAttributes());
    }

    public ExtensionDetail removeExtensionDetail(String id) {
        return this.m_details.remove(id);
    }

    public void setDynamicAttribute(String name, String value) {
        this.m_dynamicAttributes.put(name, value);
    }

    public Extension setDescription(String description) {
        this.m_description = description;
        return this;
    }

    public Extension setId(String id) {
        this.m_id = id;
        return this;
    }
}
