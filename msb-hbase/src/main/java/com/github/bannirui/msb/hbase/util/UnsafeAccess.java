package com.github.bannirui.msb.hbase.util;

import java.lang.reflect.Field;
import java.nio.ByteOrder;
import java.security.AccessController;
import java.security.PrivilegedAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Unsafe;

public final class UnsafeAccess {
    private static final Logger LOG = LoggerFactory.getLogger(UnsafeAccess.class);
    public static final Unsafe theUnsafe = (Unsafe) AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            return f.get(null);
        } catch (Throwable e) {
            UnsafeAccess.LOG.warn("sun.misc.Unsafe is not accessible", e);
            return null;
        }
    });
    public static final int BYTE_ARRAY_BASE_OFFSET;
    public static final boolean littleEndian;

    static {
        if (theUnsafe != null) {
            BYTE_ARRAY_BASE_OFFSET = theUnsafe.arrayBaseOffset(byte[].class);
        } else {
            BYTE_ARRAY_BASE_OFFSET = -1;
        }
        littleEndian = ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN);
    }
}
