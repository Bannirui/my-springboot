package com.github.bannirui.msb.web.util;

import java.util.HashMap;
import java.util.Map;

public class RadixBuilder {
    private int radix;
    private char[] chars;
    private Map<Character, Integer> number = new HashMap<>();

    public RadixBuilder(String letter) {
        if (letter == null) {
            throw new IllegalArgumentException("letter must not be null!");
        }
        this.radix = letter.length();
        this.chars = letter.toCharArray();
        for(int i = 0; i < this.chars.length; ++i) {
            this.number.put(this.chars[i], i);
        }
    }

    public String parse(long num) {
        if (num == 0L) {
            return String.valueOf(this.chars[0]);
        }
        StringBuilder s = new StringBuilder();
        this.append(s, num);
        return s.toString();
    }

    private void append(StringBuilder s, long num) {
        if (num != 0L) {
            long l = num % (long)this.radix;
            s.insert(0, this.chars[(int) l]);
            this.append(s, num / (long)this.radix);
        }
    }
}
