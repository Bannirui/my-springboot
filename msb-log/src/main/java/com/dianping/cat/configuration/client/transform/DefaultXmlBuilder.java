package com.dianping.cat.configuration.client.transform;

import com.dianping.cat.configuration.client.IEntity;
import com.dianping.cat.configuration.client.IVisitor;
import com.dianping.cat.configuration.client.entity.Bind;
import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Domain;
import com.dianping.cat.configuration.client.entity.Property;
import com.dianping.cat.configuration.client.entity.Server;
import java.util.Map;
import java.util.Objects;

public class DefaultXmlBuilder implements IVisitor {

    private IVisitor m_visitor;
    private int m_level;
    private StringBuilder m_sb;
    private boolean m_compact;

    public DefaultXmlBuilder() {
        this(false);
    }

    public DefaultXmlBuilder(boolean compact) {
        this(compact, new StringBuilder(4096));
    }

    public DefaultXmlBuilder(boolean compact, StringBuilder sb) {
        this.m_visitor = this;
        this.m_compact = compact;
        this.m_sb = sb;
        this.m_sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n");
    }

    public String buildXml(IEntity<?> entity) {
        entity.accept(this.m_visitor);
        return this.m_sb.toString();
    }

    @Override
    public void visitBind(Bind bind) {
        this.startTag("bind", true, null, "ip", bind.getIp(), "port", bind.getPort());
    }

    @Override
    public void visitConfig(ClientConfig config) {
        this.startTag("config", config.getDynamicAttributes(), "mode", config.getMode(), "enabled", config.getEnabled(), "dump-locked",
            config.getDumpLocked());
        this.element("base-log-dir", config.getBaseLogDir(), true);
        if (!config.getServers().isEmpty()) {
            this.startTag("servers");
            config.getServers().forEach(server -> server.accept(this.m_visitor));
            this.endTag("servers");
        }
        if (!config.getDomains().isEmpty()) {
            config.getDomains().values().forEach(domain -> domain.accept(this.m_visitor));
        }
        if (config.getBind() != null) {
            config.getBind().accept(this.m_visitor);
        }
        if (!config.getProperties().isEmpty()) {
            this.startTag("properties");
            config.getProperties().forEach((s, property) -> property.accept(this.m_visitor));
            this.endTag("properties");
        }
        this.endTag("config");
    }

    @Override
    public void visitDomain(Domain domain) {
        this.startTag("domain", true, null, "id", domain.getId(), "ip", domain.getIp(), "enabled", domain.getEnabled(), "max-message-size", domain.getMaxMessageSize());
    }

    @Override
    public void visitProperty(Property property) {
        this.startTag("property", property.getText(), true, null, "name", property.getName());
    }

    @Override
    public void visitServer(Server server) {
        this.startTag("server", true, null, "ip", server.getIp(), "port", server.getPort(), "http-port", server.getHttpPort(), "enabled", server.getEnabled());
    }

    protected void startTag(String name) {
        this.startTag(name, false, null);
    }

    protected void startTag(String name, boolean closed, Map<String, String> dynamicAttributes, Object... nameValues) {
        this.startTag(name, null, closed, dynamicAttributes, nameValues);
    }

    protected void startTag(String name, Map<String, String> dynamicAttributes, Object... nameValues) {
        this.startTag(name, null, false, dynamicAttributes, nameValues);
    }

    protected void startTag(String name, Object text, boolean closed, Map<String, String> dynamicAttributes, Object... nameValues) {
        this.indent();
        this.m_sb.append('<').append(name);
        int len = nameValues.length;
        for (int i = 0; i + 1 < len; i += 2) {
            Object attrName = nameValues[i];
            Object attrValue = nameValues[i + 1];
            if (attrValue != null) {
                this.m_sb.append(' ').append(attrName).append("=\"").append(this.escape(attrValue)).append('"');
            }
        }
        if (dynamicAttributes != null) {
            dynamicAttributes.forEach((k, v) -> this.m_sb.append(' ').append(k).append("=\"").append(this.escape(v)).append('"'));
        }
        if (text != null && closed) {
            this.m_sb.append('>');
            this.m_sb.append(this.escape(text, true));
            this.m_sb.append("</").append(name).append(">\r\n");
        } else {
            if (closed) {
                this.m_sb.append('/');
            } else {
                ++this.m_level;
            }
            this.m_sb.append(">\r\n");
        }
    }

    protected void indent() {
        if (!this.m_compact) {
            this.m_sb.append("   ".repeat(Math.max(0, this.m_level)));
        }
    }

    protected String escape(Object value) {
        return this.escape(value, false);
    }

    protected String escape(Object value, boolean text) {
        if (value == null) {
            return null;
        }
        String str = value.toString();
        int len = str.length();
        StringBuilder sb = new StringBuilder(len + 16);
        for (int i = 0; i < len; ++i) {
            char ch = str.charAt(i);
            switch (ch) {
                case '"':
                    if (!text) {
                        sb.append("&quot;");
                        break;
                    }
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                default:
                    sb.append(ch);
                    break;
            }
        }
        return sb.toString();
    }

    protected void element(String name, String text, boolean escape) {
        if (Objects.isNull(text)) {
            return;
        }
        this.indent();
        this.m_sb.append('<').append(name).append(">");
        if (escape) {
            this.m_sb.append(this.escape(text, true));
        } else {
            this.m_sb.append("<![CDATA[").append(text).append("]]>");
        }
        this.m_sb.append("</").append(name).append(">\r\n");
    }

    protected void endTag(String name) {
        --this.m_level;
        this.indent();
        this.m_sb.append("</").append(name).append(">\r\n");
    }
}
