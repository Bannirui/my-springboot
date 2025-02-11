package com.github.bannirui.msb.dubbo.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class SignatureUtils {
    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";

    public static String sign(String metadata, String key) throws SecurityException {
        try {
            return sign(metadata.getBytes(), key);
        } catch (Exception var3) {
            throw new SecurityException("Failed to generate HMAC : " + var3.getMessage(), var3);
        }
    }

    public static String sign(Object[] parameters, String metadata, String key) {
        try {
            if (parameters == null) {
                return sign(metadata, key);
            } else {
                boolean notSerializable = Arrays.stream(parameters).anyMatch((parameter) -> !(parameter instanceof Serializable));
                if (notSerializable) {
                    throw new IllegalArgumentException("");
                } else {
                    Object[] includeMetadata = new Object[parameters.length + 1];
                    System.arraycopy(parameters, 0, includeMetadata, 0, parameters.length);
                    includeMetadata[parameters.length] = metadata;
                    byte[] bytes = toByteArray(includeMetadata);
                    return sign(bytes, key);
                }
            }
        } catch (Exception e) {
            throw new SecurityException("Failed to generate HMAC : " + e.getMessage(), e);
        }
    }

    public static String sign(byte[] data, String key) throws SignatureException {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), "HmacSHA256");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(data);
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
        }
    }

    static byte[] toByteArray(Object[] parameters) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        byte[] ret;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(parameters);
            out.flush();
            ret = bos.toByteArray();
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
            }
        }
        return ret;
    }
}
