package com.github.bannirui.msb.mq.sdk.utils;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.MapUtils;

public class HMacSHA1Util {
    private static final String CHER_SET = "utf-8";
    private static final String ALGORITHM = "HmacSHA1";

    public static String spliceParam(Map<String, String> map) {
        String result = "";
        if(MapUtils.isEmpty(map)) {
            return result;
        }
        List<Map.Entry<String, String>> entries = map.entrySet().stream().filter((item) -> item.getValue() != null).sorted(Map.Entry.comparingByKey()).toList();
        StringBuffer paramBf = new StringBuffer();
        int index = 0;
        for (Map.Entry<String, String> entry : entries) {
            paramBf.append(entry.getKey() + "=" + entry.getValue());
            ++index;
            if (index != entries.size()) {
                paramBf.append("&");
            }
        }
        result = paramBf.toString();
        return result;
    }

    public static String hmacSHA1Encrypt(String encryptText, String encryptKey) {
        try {
            byte[] data = encryptKey.getBytes(StandardCharsets.UTF_8);
            SecretKey secretKey = new SecretKeySpec(data, "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(secretKey);
            byte[] text = encryptText.getBytes(StandardCharsets.UTF_8);
            byte[] result = mac.doFinal(text);
            return byteArrayToHexString(result).toLowerCase();
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String hmacSHA1Encrypt(Map<String, String> body, String encryptKey) {
        String spliceParam = spliceParam(body);
        return hmacSHA1Encrypt(spliceParam, encryptKey);
    }

    public static String initMacKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA1");
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.encodeBase64String(secretKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String byteArrayToHexString(byte[] bArray) {
        String data = "";
        for (byte b : bArray) {
            data += Integer.toHexString(b >> 4 & 15);
            data += Integer.toHexString(b & 15);
        }
        return data;
    }
}
