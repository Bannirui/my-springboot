package com.github.bannirui.msb.common.util;

import org.apache.commons.lang3.StringUtils;

public class StringUtil {

    public static boolean equals(String str1, String str2) {
        return StringUtils.equals(str1, str2);
    }

    public static boolean equalsIgnoreCase(String str1, String str2) {
        return StringUtils.equalsIgnoreCase(str1, str2);
    }

    public static boolean isEmpty(String str) {
        return StringUtils.isEmpty(str);
    }

    public static boolean isBlank(String str) {
        return StringUtils.isBlank(str);
    }

    public static boolean isNotEmpty(String str) {
        return StringUtils.isNotEmpty(str);
    }

    public static boolean isNotBlank(String str) {
        return StringUtils.isNotBlank(str);
    }

    public static String trim(String str) {
        return StringUtils.trim(str);
    }

    public static String upperCase(final String str) {
        return StringUtils.upperCase(str);
    }

    public static String lowerCase(final String str) {
        return StringUtils.lowerCase(str);
    }

    public static boolean isAlpha(final String str) {
        return StringUtils.isAlpha(str);
    }

    public static boolean isNumeric(final String str) {
        return StringUtils.isNumeric(str);
    }

    public static boolean startsWith(final String str1, final String prefix) {
        return StringUtils.startsWith(str1, prefix);
    }

    public static boolean endsWith(final String str, final String suffix) {
        return StringUtils.endsWith(str, suffix);
    }

    public static boolean contains(final String str, final String searchStr) {
        return StringUtils.contains(str, searchStr);
    }

    public static String[] split(final String str, final String splitStr) {
        return StringUtils.split(str, splitStr);
    }

    public static String replace(final String text, final String searchString, final String replacement) {
        return StringUtils.replace(text, searchString, replacement);
    }

    public static String join(final CharSequence delimiter, final Iterable<? extends CharSequence> elements) {
        return elements == null ? null : String.join(delimiter, elements);
    }

    public static String toLowerCaseFirstOne(String s) {
        if (s != null && s.length() != 0) {
            return Character.isLowerCase(s.charAt(0)) ? s : Character.toLowerCase(s.charAt(0)) + s.substring(1);
        } else {
            return s;
        }
    }

    public static String toUpperCaseFirstOne(String s) {
        if (s != null && s.length() != 0) {
            return Character.isUpperCase(s.charAt(0)) ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
        } else {
            return s;
        }
    }
}
