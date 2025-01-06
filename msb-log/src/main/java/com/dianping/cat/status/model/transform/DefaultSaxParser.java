package com.dianping.cat.status.model.transform;

import com.dianping.cat.status.model.IEntity;
import com.dianping.cat.status.model.entity.DiskInfo;
import com.dianping.cat.status.model.entity.DiskVolumeInfo;
import com.dianping.cat.status.model.entity.Extension;
import com.dianping.cat.status.model.entity.ExtensionDetail;
import com.dianping.cat.status.model.entity.GcInfo;
import com.dianping.cat.status.model.entity.MemoryInfo;
import com.dianping.cat.status.model.entity.MessageInfo;
import com.dianping.cat.status.model.entity.OsInfo;
import com.dianping.cat.status.model.entity.RuntimeInfo;
import com.dianping.cat.status.model.entity.StatusInfo;
import com.dianping.cat.status.model.entity.ThreadsInfo;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    public static StatusInfo parse(InputSource is) throws SAXException, IOException {
        return parseEntity(StatusInfo.class, is);
    }

    public static StatusInfo parse(InputStream in) throws SAXException, IOException {
        return parse(new InputSource(in));
    }

    public static StatusInfo parse(Reader reader) throws SAXException, IOException {
        return parse(new InputSource(reader));
    }

    public static StatusInfo parse(String xml) throws SAXException, IOException {
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
        } catch (ParserConfigurationException var4) {
            throw new IllegalStateException("Unable to get SAX parser instance!", var4);
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
            if (currentObj instanceof RuntimeInfo runtime) {
                if ("user-dir".equals(currentTag)) {
                    runtime.setUserDir(this.getText());
                } else if ("java-classpath".equals(currentTag)) {
                    runtime.setJavaClasspath(this.getText());
                }
            } else if (currentObj instanceof ThreadsInfo thread) {
                if ("dump".equals(currentTag)) {
                    thread.setDump(this.getText());
                }
            } else if (currentObj instanceof Extension extension) {
                if ("description".equals(currentTag)) {
                    extension.setDescription(this.getText());
                }
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

    private void parseForDisk(DiskInfo parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
        if ("disk-volume".equals(qName)) {
            DiskVolumeInfo diskVolume = this.m_maker.buildDiskVolume(attributes);
            this.m_linker.onDiskVolume(parentObj, diskVolume);
            this.m_objs.push(diskVolume);
            this.m_tags.push(qName);
        } else {
            throw new SAXException(String.format("Element(%s) is not expected under disk!", qName));
        }
    }

    private void parseForDiskVolume(DiskVolumeInfo parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
        this.m_objs.push(parentObj);
        this.m_tags.push(qName);
    }

    private void parseForExtension(Extension parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
        if ("description".equals(qName)) {
            this.m_objs.push(parentObj);
        } else {
            if (!"extensionDetail".equals(qName)) {
                throw new SAXException(String.format("Element(%s) is not expected under extension!", qName));
            }
            ExtensionDetail extensionDetail = this.m_maker.buildExtensionDetail(attributes);
            this.m_linker.onExtensionDetail(parentObj, extensionDetail);
            this.m_objs.push(extensionDetail);
        }
        this.m_tags.push(qName);
    }

    private void parseForExtensionDetail(ExtensionDetail parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
        this.m_objs.push(parentObj);
        this.m_tags.push(qName);
    }

    private void parseForGc(GcInfo parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
        this.m_objs.push(parentObj);
        this.m_tags.push(qName);
    }

    private void parseForMemory(MemoryInfo parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
        if ("gc".equals(qName)) {
            GcInfo gc = this.m_maker.buildGc(attributes);
            this.m_linker.onGc(parentObj, gc);
            this.m_objs.push(gc);
            this.m_tags.push(qName);
        } else {
            throw new SAXException(String.format("Element(%s) is not expected under memory!", qName));
        }
    }

    private void parseForMessage(MessageInfo parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
        this.m_objs.push(parentObj);
        this.m_tags.push(qName);
    }

    private void parseForOs(OsInfo parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
        this.m_objs.push(parentObj);
        this.m_tags.push(qName);
    }

    private void parseForRuntime(RuntimeInfo parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
        if (!"user-dir".equals(qName) && !"java-classpath".equals(qName)) {
            throw new SAXException(String.format("Element(%s) is not expected under runtime!", qName));
        } else {
            this.m_objs.push(parentObj);
            this.m_tags.push(qName);
        }
    }

    private void parseForStatus(StatusInfo parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
        if ("runtime".equals(qName)) {
            RuntimeInfo runtime = this.m_maker.buildRuntime(attributes);
            this.m_linker.onRuntime(parentObj, runtime);
            this.m_objs.push(runtime);
        } else if ("os".equals(qName)) {
            OsInfo os = this.m_maker.buildOs(attributes);
            this.m_linker.onOs(parentObj, os);
            this.m_objs.push(os);
        } else if ("disk".equals(qName)) {
            DiskInfo disk = this.m_maker.buildDisk(attributes);
            this.m_linker.onDisk(parentObj, disk);
            this.m_objs.push(disk);
        } else if ("memory".equals(qName)) {
            MemoryInfo memory = this.m_maker.buildMemory(attributes);
            this.m_linker.onMemory(parentObj, memory);
            this.m_objs.push(memory);
        } else if ("thread".equals(qName)) {
            ThreadsInfo thread = this.m_maker.buildThread(attributes);
            this.m_linker.onThread(parentObj, thread);
            this.m_objs.push(thread);
        } else if ("message".equals(qName)) {
            MessageInfo message = this.m_maker.buildMessage(attributes);
            this.m_linker.onMessage(parentObj, message);
            this.m_objs.push(message);
        } else {
            if (!"extension".equals(qName)) {
                throw new SAXException(String.format("Element(%s) is not expected under status!", qName));
            }
            Extension extension = this.m_maker.buildExtension(attributes);
            this.m_linker.onExtension(parentObj, extension);
            this.m_objs.push(extension);
        }
        this.m_tags.push(qName);
    }

    private void parseForThread(ThreadsInfo parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
        if ("dump".equals(qName)) {
            this.m_objs.push(parentObj);
            this.m_tags.push(qName);
        } else {
            throw new SAXException(String.format("Element(%s) is not expected under thread!", qName));
        }
    }

    private void parseRoot(String qName, Attributes attributes) throws SAXException {
        if ("status".equals(qName)) {
            StatusInfo status = this.m_maker.buildStatus(attributes);
            this.m_entity = status;
            this.m_objs.push(status);
            this.m_tags.push(qName);
        } else if ("runtime".equals(qName)) {
            RuntimeInfo runtime = this.m_maker.buildRuntime(attributes);
            this.m_entity = runtime;
            this.m_objs.push(runtime);
            this.m_tags.push(qName);
        } else if ("os".equals(qName)) {
            OsInfo os = this.m_maker.buildOs(attributes);
            this.m_entity = os;
            this.m_objs.push(os);
            this.m_tags.push(qName);
        } else if ("memory".equals(qName)) {
            MemoryInfo memory = this.m_maker.buildMemory(attributes);
            this.m_entity = memory;
            this.m_objs.push(memory);
            this.m_tags.push(qName);
        } else if ("thread".equals(qName)) {
            ThreadsInfo thread = this.m_maker.buildThread(attributes);
            this.m_entity = thread;
            this.m_objs.push(thread);
            this.m_tags.push(qName);
        } else if ("disk".equals(qName)) {
            DiskInfo disk = this.m_maker.buildDisk(attributes);
            this.m_entity = disk;
            this.m_objs.push(disk);
            this.m_tags.push(qName);
        } else if ("disk-volume".equals(qName)) {
            DiskVolumeInfo diskVolume = this.m_maker.buildDiskVolume(attributes);
            this.m_entity = diskVolume;
            this.m_objs.push(diskVolume);
            this.m_tags.push(qName);
        } else if ("message".equals(qName)) {
            MessageInfo message = this.m_maker.buildMessage(attributes);
            this.m_entity = message;
            this.m_objs.push(message);
            this.m_tags.push(qName);
        } else if ("gc".equals(qName)) {
            GcInfo gc = this.m_maker.buildGc(attributes);
            this.m_entity = gc;
            this.m_objs.push(gc);
            this.m_tags.push(qName);
        } else if ("extension".equals(qName)) {
            Extension extension = this.m_maker.buildExtension(attributes);
            this.m_entity = extension;
            this.m_objs.push(extension);
            this.m_tags.push(qName);
        } else {
            if (!"extensionDetail".equals(qName)) {
                throw new SAXException("Unknown root element(" + qName + ") found!");
            }
            ExtensionDetail extensionDetail = this.m_maker.buildExtensionDetail(attributes);
            this.m_entity = extensionDetail;
            this.m_objs.push(extensionDetail);
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
                if (parent instanceof StatusInfo) {
                    this.parseForStatus((StatusInfo) parent, tag, qName, attributes);
                } else if (parent instanceof RuntimeInfo) {
                    this.parseForRuntime((RuntimeInfo) parent, tag, qName, attributes);
                } else if (parent instanceof OsInfo) {
                    this.parseForOs((OsInfo) parent, tag, qName, attributes);
                } else if (parent instanceof MemoryInfo) {
                    this.parseForMemory((MemoryInfo) parent, tag, qName, attributes);
                } else if (parent instanceof ThreadsInfo) {
                    this.parseForThread((ThreadsInfo) parent, tag, qName, attributes);
                } else if (parent instanceof DiskInfo) {
                    this.parseForDisk((DiskInfo) parent, tag, qName, attributes);
                } else if (parent instanceof DiskVolumeInfo) {
                    this.parseForDiskVolume((DiskVolumeInfo) parent, tag, qName, attributes);
                } else if (parent instanceof MessageInfo) {
                    this.parseForMessage((MessageInfo) parent, tag, qName, attributes);
                } else if (parent instanceof GcInfo) {
                    this.parseForGc((GcInfo) parent, tag, qName, attributes);
                } else if (parent instanceof Extension) {
                    this.parseForExtension((Extension) parent, tag, qName, attributes);
                } else {
                    if (!(parent instanceof ExtensionDetail)) {
                        throw new RuntimeException(String.format("Unknown entity(%s) under %s!", qName, parent.getClass().getName()));
                    }
                    this.parseForExtensionDetail((ExtensionDetail) parent, tag, qName, attributes);
                }
            }

            this.m_text.setLength(0);
        }
    }

    protected Date toDate(String str, String format) {
        try {
            return (new SimpleDateFormat(format)).parse(str);
        } catch (ParseException e) {
            throw new RuntimeException(String.format("Unable to parse date(%s) in format(%s)!", str, format), e);
        }
    }
}
