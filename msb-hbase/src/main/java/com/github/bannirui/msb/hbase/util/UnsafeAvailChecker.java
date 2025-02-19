package com.github.bannirui.msb.hbase.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class UnsafeAvailChecker {
    private static final String CLASS_NAME = "sun.misc.Unsafe";
    private static final Logger LOG = LoggerFactory.getLogger(UnsafeAvailChecker.class);
    private static boolean avail = false;
    private static boolean unaligned = false;

    public static boolean isAvailable() {
        return avail;
    }

    public static boolean unaligned() {
        return unaligned;
    }

    static {
        avail = AccessController.doPrivileged((PrivilegedAction<Boolean>) () -> {
            try {
                Class<?> clazz = Class.forName("sun.misc.Unsafe");
                Field f = clazz.getDeclaredField("theUnsafe");
                f.setAccessible(true);
                return f.get(null) != null;
            } catch (Throwable e) {
                LOG.warn("sun.misc.Unsafe is not available/accessible", e);
                return false;
            }
        });
        if (avail) {
            try {
                Class<?> clazz = Class.forName("java.nio.Bits");
                Method m = clazz.getDeclaredMethod("unaligned");
                m.setAccessible(true);
                unaligned = (Boolean)m.invoke(null);
            } catch (Exception e) {
                LOG.warn("java.nio.Bits#unaligned() check failed.Unsafe based read/write of primitive types won't be used", e);
            }
        }
    }
}
