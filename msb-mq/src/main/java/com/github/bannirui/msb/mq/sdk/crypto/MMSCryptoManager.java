package com.github.bannirui.msb.mq.sdk.crypto;

import com.github.bannirui.msb.mq.sdk.common.MmsException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MMSCryptoManager {
    private static final Logger logger= LoggerFactory.getLogger(MMSCryptoManager.class);
    public static final String ENCRYPT_MARK = "encrypt_mark";
    public static final String ENCRYPT_MARK_VALUE = "#%$==";
    public static final String DEV = "DEV";
    public static final String FAT = "FAT";
    public static final String SIT = "SIT";
    public static final String PRO = "PRO";
    private static final ConcurrentMap<String, MMSCrypto> CRYPTO_CACHE = new ConcurrentHashMap<>();

    static {
        // TODO: 2025/1/21
        // ZtoKmc.Init();
    }

    public static byte[] encrypt(String topicName, byte[] data) {
        return getMMSCrypto(topicName).encrypt(data);
    }

    public static byte[] decrypt(String topicName, byte[] data) {
        return getMMSCrypto(topicName).decrypt(data);
    }

    public static boolean createKey(String topicName) {
        boolean dev = doCreate("DEV", topicName);
        boolean fat = doCreate("FAT", topicName);
        boolean sit = doCreate("SIT", topicName);
        boolean pro = doCreate("PRO", topicName);
        return dev && fat && sit && pro;
    }

    private static boolean doCreate(String group, String topicName) {
        try {
            // TODO: 2025/1/21
            // return ZtoKmc.createKey(group, topicName);
            return true;
        } catch (Exception e) {
            if (e.getMessage().contains("密钥已经存在")) {
                return true;
            } else {
                logger.error("create key failure", e);
                throw new MmsException("create key failure: " + e.getMessage(), 2202);
            }
        }
    }

    public static String getKeyInfo(String topicName) {
        try {
            // TODO: 2025/1/21
            // return ZtoKmc.getKeyInfo(topicName);
            return "";
        } catch (Exception e) {
            logger.error("Failed to obtain the key", e);
            throw new MmsException("Failed to obtain the key, please check if the subject has applied for the key:" + e.getMessage(), 2201);
        }
    }

    private static MMSCrypto getMMSCrypto(String topicName) {
        return CRYPTO_CACHE.computeIfAbsent(topicName, MMSCryptoManager::selectCrypto);
    }

    private static MMSCrypto selectCrypto(String topicName) {
        return new AESCrypto(topicName);
    }
}
