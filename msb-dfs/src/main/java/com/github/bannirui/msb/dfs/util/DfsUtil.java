package com.github.bannirui.msb.dfs.util;

import com.alibaba.fastjson.JSON;
import com.github.bannirui.msb.dfs.param.DeleteParam;
import com.github.bannirui.msb.dfs.param.GetFileParam;
import com.github.bannirui.msb.dfs.param.PublicGetFileParam;
import com.github.bannirui.msb.dfs.param.StsParam;
import com.github.bannirui.msb.dfs.param.UploadParam;
import com.github.bannirui.msb.enums.ExceptionEnum;
import com.github.bannirui.msb.env.MsbEnvironmentMgr;
import com.github.bannirui.msb.ex.ErrorCodeException;
import com.github.bannirui.msb.http.util.ApplicationContextUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.ConfigurableEnvironment;

public class DfsUtil {
    private static final Logger logger = LoggerFactory.getLogger(DfsUtil.class);

    public static String getStsUrl(String dfsUrl) {
        return dfsUrl.concat("/GetST");
    }

    public static String getFileUrl(String dfsUrl) {
        return dfsUrl.concat("/GetFile");
    }

    public static String getEncryptFileUrl(String dfsUrl) {
        return dfsUrl.concat("/GetEncryptFile");
    }

    public static String getPublicFileUrl(String dfsUrl) {
        return dfsUrl.concat("/GetPublicFile");
    }

    public static String getDeleteUrl(String dfsUrl) {
        return dfsUrl.concat("/DeleteFile");
    }

    public static String getCheckUrl(String dfsUrl) {
        return dfsUrl.concat("/CheckFileExist");
    }

    public static Map<String, String> buildUploadParam(UploadParam param, String dfsAppId, String secret) {
        checkParamNonNull(secret, ExceptionEnum.DFS_SECRET_NOT_FIND);
        String timestamp = String.valueOf(System.currentTimeMillis());
        String nonce = getRandomNumber();
        Map<String, String> map = new HashMap<>(8);
        map.put("ext", param.getExt());
        map.put("group", param.getGroup());
        if (StringUtils.isNotEmpty(param.getFilename())) {
            map.put("filename", param.getFilename());
        }
        if (StringUtils.isNotEmpty(param.getRemoteFileId())) {
            map.put("remoteFileId", param.getRemoteFileId());
        }
        if (param.getExpire() != null) {
            map.put("expires", String.valueOf(param.getExpire()));
        }
        map.put("appid", getAppid(dfsAppId));
        map.put("timestamp", timestamp);
        map.put("nonce", nonce);
        map.put("signature", makeSignature(timestamp, nonce, secret));
        return map;
    }

    public static Map<String, String> buildDeleteParam(DeleteParam param, String dfsAppId, String secret) {
        checkParamNonNull(secret, ExceptionEnum.DFS_SECRET_NOT_FIND);
        Map<String, String> map = new HashMap<>(4);
        map.put("appid", getAppid(dfsAppId));
        map.put("signature", makeSignature(param.getRemoteFileId(), param.getAccessToken(), secret));
        map.put("remoteFileId", param.getRemoteFileId());
        map.put("accessToken", param.getAccessToken());
        return map;
    }

    public static Map<String, String> buildCheckParam(String dfsAppId, String fileName) {
        Map<String, String> map = new HashMap<>(4);
        map.put("appid", getAppid(dfsAppId));
        map.put("fileName", fileName);
        return map;
    }

    public static Map<String, String> buildStsParam(StsParam param, String dfsAppId) {
        Map<String, String> map = new HashMap<>(2);
        map.put("appid", getAppid(dfsAppId));
        map.put("userName", param.getUserName());
        return map;
    }

    public static Map<String, String> buildGetPublicFileParam(PublicGetFileParam param, String dfsAppId) {
        Map<String, String> map = new HashMap<>(3);
        if (param.getWidth() != null) {
            map.put("width", String.valueOf(param.getWidth()));
        }
        map.put("remoteFileId", param.getRemoteFileId());
        map.put("appid", getAppid(dfsAppId));
        return map;
    }

    public static Map<String, String> buildGetFileParam(GetFileParam param, String dfsAppId, String secret) {
        checkParamNonNull(secret, ExceptionEnum.DFS_SECRET_NOT_FIND);
        Map<String, String> map = new HashMap<>(4);
        GetFileParam.Data data = param.getData();
        map.put("data", JSON.toJSONString(data));
        map.put("signature", makeSignature(getAppid(dfsAppId), JSON.toJSONString(data), secret));
        map.put("appid", getAppid(dfsAppId));
        if (param.getWidth() != null) {
            map.put("width", String.valueOf(param.getWidth()));
        }
        return map;
    }

    private static void checkParamNonNull(String param, ExceptionEnum dfsSecretNotFind) {
        if (StringUtils.isEmpty(param)) {
            throw new ErrorCodeException(dfsSecretNotFind);
        }
    }

    public static String makeSignature(String timestamp, String nonce, String token) {
        String[] arr = new String[]{token, timestamp, nonce};
        Arrays.sort(arr);
        StringBuilder content = new StringBuilder();
        for (String s : arr) {
            content.append(s);
        }
        MessageDigest md = null;
        String tmpStr = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(content.toString().getBytes());
            tmpStr = byteToStr(digest);
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage(), e);
        }

        return tmpStr;
    }

    private static String byteToStr(byte[] digest) {
        StringBuilder strDigest = new StringBuilder();
        for (byte b : digest) {
            strDigest.append(byteToHexStr(b));
        }
        return strDigest.toString();
    }

    private static String byteToHexStr(byte b) {
        char[] Digit = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        char[] tempArr = new char[]{Digit[b >>> 4 & 15], Digit[b & 15]};
        return new String(tempArr);
    }

    private static String getRandomNumber() {
        return String.valueOf((Math.random() * 9.0D + 1.0D) * 100000.0D);
    }

    private static String getAppid(String dfsAppid) {
        if (StringUtils.isEmpty(dfsAppid)) {
            dfsAppid = MsbEnvironmentMgr.getProperty((ConfigurableEnvironment) ApplicationContextUtil.getApplicationContext().getEnvironment(), "sso.appId");
        }
        checkParamNonNull(dfsAppid, ExceptionEnum.DFS_APPID_NOT_FIND);
        return dfsAppid;
    }

    public static String getFileNameWithoutSuffix(File file) {
        String fileName = file.getName();
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    public static String getFileSuffix(File file) {
        String fileName = file.getName();
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    public static byte[] toByteArray(File file) {
        FileChannel channel = null;
        FileInputStream fs = null;
        try {
            fs = new FileInputStream(file);
            channel = fs.getChannel();
            ByteBuffer byteBuffer = ByteBuffer.allocate((int)channel.size());
            while(channel.read(byteBuffer) > 0) {
            }
            return byteBuffer.array();
        } catch (IOException e) {
            throw new ErrorCodeException(ExceptionEnum.DFS_FILE_TO_BYTE_ERROR, e);
        } finally {
            try {
                if (channel != null) {
                    channel.close();
                }
            } catch (IOException e) {
                logger.error("DfsUtil.toByteArray()方法FileChannel关闭异常", e);
            }
            try {
                if (fs != null) {
                    fs.close();
                }
            } catch (IOException e) {
                logger.error("DfsUtil.toByteArray()方法FileInputStream关闭异常", e);
            }
        }
    }

    public static FileInputStream toInputStream(File file) {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new ErrorCodeException(ExceptionEnum.DFS_FILE_TO_INPUTSTREAM_ERROR, e);
        }
    }

    public static String getUploadUrl(boolean isEncrypt, String dfsurl) {
        String path = null;
        if (isEncrypt) {
            path = "UploadEncryptFile";
        } else {
            path = "UploadFile";
        }
        return dfsurl.concat("/" + path);
    }
}
