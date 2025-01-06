package com.dianping.cat.configuration.client.transform;

import com.dianping.cat.configuration.client.IEntity;
import com.dianping.cat.configuration.client.entity.Bind;
import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Domain;
import com.dianping.cat.configuration.client.entity.Property;
import com.dianping.cat.configuration.client.entity.Server;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Stack;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class DefaultSaxParser extends DefaultHandler {

    private DefaultLinker m_linker = new DefaultLinker(true);
    private DefaultSaxMaker m_maker = new DefaultSaxMaker();
    private Stack<String> m_tags = new Stack<>();
    private Stack<Object> m_objs = new Stack<>();
    private IEntity<?> m_entity;
    private StringBuilder m_text = new StringBuilder();

    public static ClientConfig parse(InputSource is) throws SAXException, IOException {
        return parseEntity(ClientConfig.class, is);
    }

    public static ClientConfig parse(InputStream in) throws SAXException, IOException {
        return parse(new InputSource(in));
    }

    public static ClientConfig parse(Reader reader) throws SAXException, IOException {
        return parse(new InputSource(reader));
    }

    public static ClientConfig parse(String xml) throws SAXException, IOException {
        return parse(new InputSource(new StringReader(xml)));
    }

    public static <T extends IEntity<?>> T parseEntity(Class<T> type, String xml) throws SAXException, IOException {
        return parseEntity(type, new InputSource(new StringReader(xml)));
    }

    public static <T extends IEntity<?>> T parseEntity(Class<T> type, InputSource is) throws SAXException, IOException {
        try {
            DefaultSaxParser handler = new DefaultSaxParser();
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setFeature("http://xml.org/sax/features/validation", false);
            factory.newSAXParser().parse(is, handler);
            return (T) handler.getEntity();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Unable to get SAX parser instance!", e);
        }
    }

    protected <T> T convert(Class<T> type, String value, T defaultValue) {
        if (value != null && value.length() != 0) {
            if (type == Boolean.class) {
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
        } else {
            return defaultValue;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        this.m_text.append(ch, start, length);
    }

    @Override
    public void endDocument() throws SAXException {
        this.m_linker.finish();
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (uri == null || uri.length() == 0) {
            Object currentObj = this.m_objs.pop();
            String currentTag = (String) this.m_tags.pop();
            if (currentObj instanceof ClientConfig config) {
                if ("base-log-dir".equals(currentTag)) {
                    config.setBaseLogDir(this.getText());
                }
            } else if (currentObj instanceof Property property) {
                property.setText(this.getText());
            }
        }
        this.m_text.setLength(0);
    }

    private IEntity<?> getEntity() {
        return this.m_entity;
    }

    protected String getText() {
        return this.m_text.toString();
    }

    private void parseForBind(Bind parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
        this.m_objs.push(parentObj);
        this.m_tags.push(qName);
    }

    private void parseForConfig(ClientConfig parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
        if (!"servers".equals(qName) && !"properties".equals(qName) && !"base-log-dir".equals(qName)) {
            if ("server".equals(qName)) {
                Server server = this.m_maker.buildServer(attributes);
                this.m_linker.onServer(parentObj, server);
                this.m_objs.push(server);
            } else if ("domain".equals(qName)) {
                Domain domain = this.m_maker.buildDomain(attributes);
                this.m_linker.onDomain(parentObj, domain);
                this.m_objs.push(domain);
            } else if ("bind".equals(qName)) {
                Bind bind = this.m_maker.buildBind(attributes);
                this.m_linker.onBind(parentObj, bind);
                this.m_objs.push(bind);
            } else {
                if (!"property".equals(qName)) {
                    throw new SAXException(String.format("Element(%s) is not expected under config!", qName));
                }
                Property property = this.m_maker.buildProperty(attributes);
                this.m_linker.onProperty(parentObj, property);
                this.m_objs.push(property);
            }
        } else {
            this.m_objs.push(parentObj);
        }
        this.m_tags.push(qName);
    }

    private void parseForDomain(Domain parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
        this.m_objs.push(parentObj);
        this.m_tags.push(qName);
    }

    private void parseForProperty(Property parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
        this.m_objs.push(parentObj);
        this.m_tags.push(qName);
    }

    private void parseForServer(Server parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
        this.m_objs.push(parentObj);
        this.m_tags.push(qName);
    }

    private void parseRoot(String qName, Attributes attributes) throws SAXException {
        if ("config".equals(qName)) {
            ClientConfig config = this.m_maker.buildConfig(attributes);
            this.m_entity = config;
            this.m_objs.push(config);
            this.m_tags.push(qName);
        } else if ("server".equals(qName)) {
            Server server = this.m_maker.buildServer(attributes);
            this.m_entity = server;
            this.m_objs.push(server);
            this.m_tags.push(qName);
        } else if ("domain".equals(qName)) {
            Domain domain = this.m_maker.buildDomain(attributes);
            this.m_entity = domain;
            this.m_objs.push(domain);
            this.m_tags.push(qName);
        } else if ("bind".equals(qName)) {
            Bind bind = this.m_maker.buildBind(attributes);
            this.m_entity = bind;
            this.m_objs.push(bind);
            this.m_tags.push(qName);
        } else {
            if (!"property".equals(qName)) {
                throw new SAXException("Unknown root element(" + qName + ") found!");
            }
            Property property = this.m_maker.buildProperty(attributes);
            this.m_entity = property;
            this.m_objs.push(property);
            this.m_tags.push(qName);
        }

    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (uri != null && uri.length() != 0) {
            throw new SAXException(String.format("Namespace(%s) is not supported by %s.", uri, this.getClass().getName()));
        } else {
            if (this.m_objs.isEmpty()) {
                this.parseRoot(qName, attributes);
            } else {
                Object parent = this.m_objs.peek();
                String tag = (String) this.m_tags.peek();
                if (parent instanceof ClientConfig) {
                    this.parseForConfig((ClientConfig) parent, tag, qName, attributes);
                } else if (parent instanceof Server) {
                    this.parseForServer((Server) parent, tag, qName, attributes);
                } else if (parent instanceof Domain) {
                    this.parseForDomain((Domain) parent, tag, qName, attributes);
                } else if (parent instanceof Bind) {
                    this.parseForBind((Bind) parent, tag, qName, attributes);
                } else {
                    if (!(parent instanceof Property)) {
                        throw new RuntimeException(String.format("Unknown entity(%s) under %s!", qName, parent.getClass().getName()));
                    }
                    this.parseForProperty((Property) parent, tag, qName, attributes);
                }
            }
            this.m_text.setLength(0);
        }
    }
}
