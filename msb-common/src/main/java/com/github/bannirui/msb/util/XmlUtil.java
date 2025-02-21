package com.github.bannirui.msb.util;

import com.github.bannirui.msb.annotation.XStreamItem;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.mapper.ClassAliasingMapper;
import com.thoughtworks.xstream.security.NoTypePermission;
import com.thoughtworks.xstream.security.NullPermission;
import com.thoughtworks.xstream.security.PrimitiveTypePermission;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class XmlUtil {

    private static Map<String, XStream> xstreams = new ConcurrentHashMap<>();
    private static Set<String> defaultPermissionSets = new HashSet<>(Arrays.asList("java.lang.*", "java.util.*", "java.util.concurrent.*", "com.zto.**"));
    private static Map<Object, Set<String>> allowedTypes = new ConcurrentHashMap<>();

    private static XStream getXStream(Class clazz) {
        if (!xstreams.containsKey(clazz.toString())) {
            XStream xstream = new XStream(new DomDriver());
            xstream.ignoreUnknownElements();
            xstream.processAnnotations(clazz);
            XStreamItem itemAnnotation = (XStreamItem)clazz.getAnnotation(XStreamItem.class);
            if (itemAnnotation != null) {
                ClassAliasingMapper mapper = new ClassAliasingMapper(xstream.getMapper());
                mapper.addClassAlias(itemAnnotation.item(), String.class);
                xstream.registerLocalConverter(itemAnnotation.clazz(), itemAnnotation.list(), new CollectionConverter(mapper));
            }
            xstream.addPermission(NoTypePermission.NONE);
            xstream.addPermission(NullPermission.NULL);
            xstream.addPermission(PrimitiveTypePermission.PRIMITIVES);
            String[] defaultPermissions = new String[defaultPermissionSets.size()];
            defaultPermissionSets.toArray(defaultPermissions);
            xstream.allowTypesByWildcard(defaultPermissions);
            allowedTypes.put(xstream, defaultPermissionSets);
            xstreams.put(clazz.toString(), xstream);
        }
        return xstreams.get(clazz.toString());
    }

    public static <T> T toBean(String xmlStr, Class<T> cls) {
        XStream xstream = getXStream(cls);
        return  (T) xstream.fromXML(xmlStr);
    }
    public static String toXml(Object obj) {
        XStream xstream = getXStream(obj.getClass());
        return xstream.toXML(obj);
    }
}
