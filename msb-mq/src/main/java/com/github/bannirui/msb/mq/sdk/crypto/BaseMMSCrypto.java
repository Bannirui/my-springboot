package com.github.bannirui.msb.mq.sdk.crypto;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class BaseMMSCrypto implements MMSCrypto {
    private static final ConcurrentMap<String, String> KEY_NAME_CACHE = new ConcurrentHashMap<>();

    public byte[] encrypt(byte[] data) {
        return data;
    }

    public byte[] decrypt(byte[] data) {
        return data;
    }

    protected String getKeyInfo(String topicName) {
        return KEY_NAME_CACHE.computeIfAbsent(topicName, MMSCryptoManager::getKeyInfo);
    }
}
