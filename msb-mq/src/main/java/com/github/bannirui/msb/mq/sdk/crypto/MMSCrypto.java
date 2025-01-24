package com.github.bannirui.msb.mq.sdk.crypto;

public interface MMSCrypto {
    byte[] encrypt(byte[] data);

    byte[] decrypt(byte[] data);
}
