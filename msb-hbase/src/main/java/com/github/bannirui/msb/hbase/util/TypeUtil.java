package com.github.bannirui.msb.hbase.util;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TypeUtil {

    public static boolean isLongType(Field f) {
        if (f == null) {
            return false;
        } else if (f.getType().isAssignableFrom(Long.TYPE)) {
            return true;
        } else {
            return f.getType().isAssignableFrom(Long.class);
        }
    }

    public static boolean isShortType(Field f) {
        if (f == null) {
            return false;
        } else if (f.getType().isAssignableFrom(Short.TYPE)) {
            return true;
        } else {
            return f.getType().isAssignableFrom(Short.class);
        }
    }

    public static boolean isIntType(Field f) {
        if (f == null) {
            return false;
        } else if (f.getType().isAssignableFrom(Integer.TYPE)) {
            return true;
        } else {
            return f.getType().isAssignableFrom(Integer.class);
        }
    }

    public static boolean isFloatType(Field f) {
        if (f == null) {
            return false;
        } else if (f.getType().isAssignableFrom(Float.TYPE)) {
            return true;
        } else {
            return f.getType().isAssignableFrom(Float.class);
        }
    }

    public static boolean isDoubleType(Field f) {
        if (f == null) {
            return false;
        } else if (f.getType().isAssignableFrom(Double.TYPE)) {
            return true;
        } else {
            return f.getType().isAssignableFrom(Double.class);
        }
    }

    public static boolean isBooleanType(Field f) {
        if (f == null) {
            return false;
        } else if (f.getType().isAssignableFrom(Boolean.TYPE)) {
            return true;
        } else {
            return f.getType().isAssignableFrom(Boolean.class);
        }
    }

    public static boolean isBigDecimalType(Field f) {
        return f == null ? false : f.getType().isAssignableFrom(BigDecimal.class);
    }

    public static boolean isStringType(Field f) {
        return f == null ? false : f.getType().isAssignableFrom(String.class);
    }

    public static String getSuitableTypes() {
        List<String> result = new ArrayList<>();
        result.add("boolean[Boolean]");
        result.add("short[Short]");
        result.add("int[Integer]");
        result.add("long[Long]");
        result.add("float[Float]");
        result.add("double[Double]");
        result.add("String");
        result.add("BigDecimal");
        result.add("Date");
        result.add("byte[]");
        result.add("Byte[]");
        return join(result.toArray(), ',');
    }

    public static String join(Object[] array, char separator) {
        return array == null ? null : join(array, separator, 0, array.length);
    }

    public static String join(Object[] array, char separator, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        }
        int noOfItems = endIndex - startIndex;
        if (noOfItems <= 0) {
            return "";
        }
        StringBuilder buf = new StringBuilder(noOfItems * 16);
        for (int i = startIndex; i < endIndex; ++i) {
            if (i > startIndex) {
                buf.append(separator);
            }
            if (array[i] != null) {
                buf.append(array[i]);
            }
        }
        return buf.toString();
    }
}
