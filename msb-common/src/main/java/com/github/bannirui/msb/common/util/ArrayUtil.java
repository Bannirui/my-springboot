package com.github.bannirui.msb.common.util;

import java.util.Objects;
import org.apache.commons.lang3.ArrayUtils;

public class ArrayUtil {

    public static boolean isEquals(Object a1, Object a2) {
        return Objects.deepEquals(a1, a2);
    }

    public static String toString(Object a) {
        return ArrayUtils.toString(a);
    }

    public static boolean isSameLength(final Object[] a1, final Object[] a2) {
        return ArrayUtils.isSameLength(a1, a2);
    }

    public static boolean isSameLength(final long[] a1, final long[] a2) {
        return ArrayUtils.isSameLength(a1, a2);
    }

    public static boolean isSameLength(final int[] a1, final int[] a2) {
        return ArrayUtils.isSameLength(a1, a2);
    }

    public static boolean isSameLength(final short[] a1, final short[] a2) {
        return ArrayUtils.isSameLength(a1, a2);
    }

    public static boolean isSameLength(final char[] a1, final char[] a2) {
        return ArrayUtils.isSameLength(a1, a2);
    }

    public static boolean isSameLength(final byte[] a1, final byte[] a2) {
        return ArrayUtils.isSameLength(a1, a2);
    }

    public static boolean isSameLength(final double[] a1, final double[] a2) {
        return ArrayUtils.isSameLength(a1, a2);
    }

    public static boolean isSameLength(final float[] a1, final float[] a2) {
        return ArrayUtils.isSameLength(a1, a2);
    }

    public static boolean isSameLength(final boolean[] a1, final boolean[] a2) {
        return ArrayUtils.isSameLength(a1, a2);
    }

    public static void reverse(final Object[] array) {
        ArrayUtils.reverse(array);
    }

    public static void reverse(final long[] array) {
        ArrayUtils.reverse(array);
    }

    public static void reverse(final int[] array) {
        ArrayUtils.reverse(array);
    }

    public static void reverse(final short[] array) {
        ArrayUtils.reverse(array);
    }

    public static void reverse(final char[] array) {
        ArrayUtils.reverse(array);
    }

    public static void reverse(final byte[] array) {
        ArrayUtils.reverse(array);
    }

    public static void reverse(final double[] array) {
        ArrayUtils.reverse(array);
    }

    public static void reverse(final float[] array) {
        ArrayUtils.reverse(array);
    }

    public static void reverse(final boolean[] array) {
        ArrayUtils.reverse(array);
    }

    public static <T> T[] addAll(final T[] array1, final T... array2) {
        return ArrayUtils.addAll(array1, array2);
    }

    public static boolean isEmpty(Object[] arrays) {
        if (arrays == null) {
            return true;
        } else {
            return arrays.length <= 0;
        }
    }
}
