package com.github.bannirui.msb.web.util;

import java.util.Random;

public class TokenUtils {
    private static RadixBuilder radix = new RadixBuilder("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");

    public static String randomState() {
        StringBuilder s = new StringBuilder();
        s.append(radix.parse(System.currentTimeMillis()));
        int i = 15;
        while(i-- > 0) {
            s.append(radix.parse(randomInt(1_000)));
        }
        return s.toString();
    }

    private static int randomInt(int i) {
        return (new Random(randomLong())).nextInt(10_000) % i;
    }

    private static long randomLong() {
        int i = (new Random()).nextInt(10_000);
        return (new Random(Runtime.getRuntime().freeMemory())).nextLong() * 10_000L + (long)i;
    }
}
