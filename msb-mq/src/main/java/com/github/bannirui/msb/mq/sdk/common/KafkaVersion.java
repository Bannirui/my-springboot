package com.github.bannirui.msb.mq.sdk.common;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.utils.AppInfoParser;

public class KafkaVersion {

    private static final String CURRENT = "2.2.1";

    public static void checkVersion() {
        long current = convertVersion("2.2.1");
        long clientVersion = convertVersion(AppInfoParser.getVersion());
        if (current > clientVersion) {
            throw MmsException.KAFKA_CLIENT_VERSION_TOO_LOW;
        }
    }

    private static long convertVersion(String version) {
        int maxVersionDot = 3;
        String[] parts = StringUtils.split(version, '.');
        long result = 0L;
        int i = 1;
        int size = parts.length;
        if (size > maxVersionDot + 1) {
            throw new MmsException("incompatible version format:" + version, 2230);
        }
        size = maxVersionDot + 1;
        for (String part : parts) {
            if (StringUtils.isNumeric(part)) {
                result += calculatePartValue(part, size, i);
            } else {
                String[] subParts = StringUtils.split(part, '-');
                if (StringUtils.isNumeric(subParts[0])) {
                    result += calculatePartValue(subParts[0], size, i);
                }
            }
            ++i;
        }
        return result;
    }

    private static long calculatePartValue(String partNumeric, int size, int index) {
        return Long.parseLong(partNumeric) * Double.valueOf(Math.pow(100.0D, size - index)).longValue();
    }
}
