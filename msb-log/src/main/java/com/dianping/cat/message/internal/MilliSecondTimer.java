package com.dianping.cat.message.internal;

import java.util.concurrent.locks.LockSupport;

public class MilliSecondTimer {
    private static long m_baseTime;
    private static long m_startNanoTime;
    private static boolean m_isWindows = false;

    public static long currentTimeMillis() {
        if (m_isWindows) {
            if (m_baseTime == 0L) {
                initialize();
            }
            long elipsed = (long) ((double) (System.nanoTime() - m_startNanoTime) / 1000000.0D);
            return m_baseTime + elipsed;
        } else {
            return System.currentTimeMillis();
        }
    }

    public static void initialize() {
        String os = System.getProperty("os.name");
        if (os.startsWith("Windows")) {
            m_isWindows = true;
            m_baseTime = System.currentTimeMillis();
            long millis;
            do {
                LockSupport.parkNanos(100000L);
                millis = System.currentTimeMillis();
            } while (millis == m_baseTime);
            m_baseTime = millis;
            m_startNanoTime = System.nanoTime();
        } else {
            m_baseTime = System.currentTimeMillis();
            m_startNanoTime = System.nanoTime();
        }
    }
}
