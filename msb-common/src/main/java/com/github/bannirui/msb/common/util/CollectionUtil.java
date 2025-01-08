package com.github.bannirui.msb.common.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CollectionUtil {
    public static <T extends Comparable<? super T>> void sort(List<T> list) {
        Collections.sort(list);
    }

    public static <T> void sort(List<T> list, Comparator<? super T> c) {
        Collections.sort(list, c);
    }

    public static void reverse(List<?> list) {
        Collections.reverse(list);
    }

    public static <T> void copy(List<? super T> dest, List<? extends T> src) {
        Collections.copy(dest, src);
    }
}
