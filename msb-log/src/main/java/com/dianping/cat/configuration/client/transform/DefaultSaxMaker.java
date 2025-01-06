package com.dianping.cat.configuration.client.transform;

import com.dianping.cat.configuration.client.entity.Bind;
import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Domain;
import com.dianping.cat.configuration.client.entity.Property;
import com.dianping.cat.configuration.client.entity.Server;
import java.util.Map;
import org.xml.sax.Attributes;

public class DefaultSaxMaker implements IMaker<Attributes> {

    @Override
    public Bind buildBind(Attributes attributes) {
        String ip = attributes.getValue("ip");
        String port = attributes.getValue("port");
        Bind bind = new Bind();
        if (ip != null) {
            bind.setIp(ip);
        }
        if (port != null) {
            bind.setPort(this.convert(Integer.class, port, 0));
        }
        return bind;
    }

    @Override
    public ClientConfig buildConfig(Attributes attributes) {
        String mode = attributes.getValue("mode");
        String enabled = attributes.getValue("enabled");
        String dumpLocked = attributes.getValue("dump-locked");
        ClientConfig config = new ClientConfig();
        if (mode != null) {
            config.setMode(mode);
        }
        if (enabled != null) {
            config.setEnabled(this.convert(Boolean.class, enabled, null));
        }
        if (dumpLocked != null) {
            config.setDumpLocked(this.convert(Boolean.class, dumpLocked, null));
        }
        Map<String, String> dynamicAttributes = config.getDynamicAttributes();
        int _length = attributes == null ? 0 : attributes.getLength();
        for (int i = 0; i < _length; ++i) {
            String _name = attributes.getQName(i);
            String _value = attributes.getValue(i);
            dynamicAttributes.put(_name, _value);
        }
        dynamicAttributes.remove("mode");
        dynamicAttributes.remove("enabled");
        dynamicAttributes.remove("dump-locked");
        return config;
    }

    @Override
    public Domain buildDomain(Attributes attributes) {
        String id = attributes.getValue("id");
        String ip = attributes.getValue("ip");
        String enabled = attributes.getValue("enabled");
        String maxMessageSize = attributes.getValue("max-message-size");
        Domain domain = new Domain(id);
        if (ip != null) {
            domain.setIp(ip);
        }
        if (enabled != null) {
            domain.setEnabled(this.convert(Boolean.class, enabled, null));
        }
        if (maxMessageSize != null) {
            domain.setMaxMessageSize(this.convert(Integer.class, maxMessageSize, 0));
        }
        return domain;
    }

    @Override
    public Property buildProperty(Attributes attributes) {
        String name = attributes.getValue("name");
        return new Property(name);
    }

    @Override
    public Server buildServer(Attributes attributes) {
        String ip = attributes.getValue("ip");
        String port = attributes.getValue("port");
        String httpPort = attributes.getValue("http-port");
        String enabled = attributes.getValue("enabled");
        Server server = new Server(ip);
        if (port != null) {
            server.setPort(this.convert(Integer.class, port, null));
        }
        if (httpPort != null) {
            server.setHttpPort(this.convert(Integer.class, httpPort, null));
        }
        if (enabled != null) {
            server.setEnabled(this.convert(Boolean.class, enabled, null));
        }
        return server;
    }

    protected <T> T convert(Class<T> type, String value, T defaultValue) {
        if (value == null) {
            return defaultValue;
        } else if (type == Boolean.class) {
            return (T) Boolean.valueOf(value);
        } else if (type == Integer.class) {
            return (T) Integer.valueOf(value);
        } else if (type == Long.class) {
            return (T) Long.valueOf(value);
        } else if (type == Short.class) {
            return (T) Short.valueOf(value);
        } else if (type == Float.class) {
            return (T) Float.valueOf(value);
        } else if (type == Double.class) {
            return (T) Double.valueOf(value);
        } else if (type == Byte.class) {
            return (T) Byte.valueOf(value);
        } else if (type == Character.class) {
            return (T) (Character) value.charAt(0);
        } else {
            return (T) value;
        }
    }
}
