package com.github.bannirui.msb.common.util;

import org.apache.commons.lang3.math.NumberUtils;

public class NumberUtil {

    public static int toInt(String str) {
        return NumberUtils.toInt(str);
    }

    public static float toFloat(String str) {
        return NumberUtils.toFloat(str);
    }

    public static long toLong(String str) {
        return NumberUtils.toLong(str);
    }

    public static double toDouble(final String str) {
        return NumberUtils.toDouble(str);
    }

    public static short toShort(final String str) {
        return NumberUtils.toShort(str);
    }
}
