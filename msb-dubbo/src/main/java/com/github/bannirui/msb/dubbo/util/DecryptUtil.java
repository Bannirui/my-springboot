package com.github.bannirui.msb.dubbo.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class DecryptUtil {
    private static final String key = "12345abc$efg6789";

    public static String decrypt(String input) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException {
        if (input == null) {
            return null;
        } else {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKey keySpec = new SecretKeySpec("12345abc$efg6789".getBytes(StandardCharsets.UTF_8), "AES");
            cipher.init(2, keySpec);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(input));
            return new String(decrypted, StandardCharsets.UTF_8);
        }
    }
}
