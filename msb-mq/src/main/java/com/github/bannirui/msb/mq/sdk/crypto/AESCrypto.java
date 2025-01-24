package com.github.bannirui.msb.mq.sdk.crypto;

import com.github.bannirui.msb.mq.sdk.common.MmsException;
import java.security.Key;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AESCrypto extends BaseMMSCrypto {
    private static final Logger logger = LoggerFactory.getLogger(AESCrypto.class);
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    public static final byte[] IV;
    private final Key key;

    static {
        IV = "#%$&^*@#==@!#$*^".getBytes();
    }

    public AESCrypto(String topicName) {
        String password = super.getKeyInfo(topicName);
        this.key = this.generateKey(password);
    }

    public byte[] encrypt(byte[] data) {
        return this.cryptoImpl(1, data);
    }

    public byte[] decrypt(byte[] data) {
        return this.cryptoImpl(2, data);
    }

    private byte[] cryptoImpl(int mode, byte[] data) {
        if (data == null) {
            return null;
        } else {
            try {
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(mode, this.key, new IvParameterSpec(IV));
                return cipher.doFinal(data);
            } catch (Exception e) {
                if (mode == 2) {
                    logger.error("decrypt failure", e);
                    throw MmsException.DECRYPT_EXCEPTION;
                } else {
                    logger.error("encrypt failure", e);
                    throw MmsException.ENCRYPT_EXCEPTION;
                }
            }
        }
    }

    private Key generateKey(String password) {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(MMSCryptoType.AES_128.getAlg());
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(password.getBytes());
            keyGenerator.init(MMSCryptoType.AES_128.getLength(), random);
            SecretKey secretKey = keyGenerator.generateKey();
            byte[] keyBytes = secretKey.getEncoded();
            return new SecretKeySpec(keyBytes, MMSCryptoType.AES_128.getAlg());
        } catch (Exception e) {
            logger.error("generate key failure", e);
            throw MmsException.GENERATE_KEY_EXCEPTION;
        }
    }
}
