package com.github.bannirui.msb.dfs.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.github.bannirui.msb.dfs.param.*;
import com.github.bannirui.msb.dfs.result.DefaultResult;
import com.github.bannirui.msb.dfs.result.DeleteResult;
import com.github.bannirui.msb.dfs.result.StsResult;
import com.github.bannirui.msb.dfs.result.UploadResult;
import com.github.bannirui.msb.dfs.util.DfsUtil;
import com.github.bannirui.msb.enums.ExceptionEnum;
import com.github.bannirui.msb.ex.ErrorCodeException;
import com.github.bannirui.msb.http.util.HttpClientUtils;
import com.github.bannirui.msb.properties.bind.PropertyBinder;
import com.github.bannirui.msb.util.AssertUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

public class FastDfsTemplate implements EnvironmentAware, InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(FastDfsTemplate.class);
    private static int connectTime = 60000;
    private static int socketConnectTime = 60000;
    private ConfigurableEnvironment environment;
    private DfsProperties dfsProperties;

    @Override
    public void setEnvironment(Environment environment) {
        if (environment instanceof ConfigurableEnvironment env) {
            this.environment = env;
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        PropertyBinder propertyBinder = new PropertyBinder(this.environment);
        this.dfsProperties = propertyBinder.bind("dfs", DfsProperties.class).orElseGet(()->null);
    }

    private StsResult dfsGetSts(StsParam param, int connectTime, int socketConnectTime) {
        try {
            String stsResult = HttpClientUtils.sendHttpGet(DfsUtil.getStsUrl(this.dfsProperties.getUrl()), connectTime, socketConnectTime, null, DfsUtil.buildStsParam(param, this.dfsProperties.getAppId()));
            return JSON.parseObject(stsResult, StsResult.class);
        } catch (IOException e) {
            throw new ErrorCodeException(ExceptionEnum.HTTP_REQUEST_ERROR, e);
        }
    }

    private UploadResult defUpload(UploadParam param, byte[] file, int connectTime, int socketConnectTime, boolean isEncrypt) {
        try {
            String uploadResult = HttpClientUtils.sendFileUpload(DfsUtil.getUploadUrl(isEncrypt, this.dfsProperties.getUrl()), DfsUtil.buildUploadParam(param, this.dfsProperties.getAppId(), this.dfsProperties.getSecret()), "uploadfile", file, connectTime, socketConnectTime);
            return JSON.parseObject(uploadResult, UploadResult.class);
        } catch (IOException e) {
            throw new ErrorCodeException(ExceptionEnum.HTTP_REQUEST_ERROR, e);
        }
    }

    private UploadResult defUploadInStream(UploadParam param, InputStream fileInputStream, int connectTime, int socketConnectTime, boolean isEncrypt) {
        String uploadResult = null;
        try {
            uploadResult = HttpClientUtils.sendFileUpload(DfsUtil.getUploadUrl(isEncrypt, this.dfsProperties.getUrl()), DfsUtil.buildUploadParam(param, this.dfsProperties.getAppId(), this.dfsProperties.getSecret()), "uploadfile", fileInputStream, connectTime, socketConnectTime);
            return JSON.parseObject(uploadResult, UploadResult.class);
        } catch (IOException e) {
            throw new ErrorCodeException(ExceptionEnum.HTTP_REQUEST_ERROR, e);
        } catch (JSONException e) {
            UploadResult result = new UploadResult();
            result.setStatus(false);
            result.setMessage(uploadResult);
            return result;
        }
    }

    private DeleteResult dfsDelete(DeleteParam param, int connectTime, int socketConnectTime) {
        try {
            String deleteResult = HttpClientUtils.sendHttpUrlEncodedPost(DfsUtil.getDeleteUrl(this.dfsProperties.getUrl()), connectTime, socketConnectTime, (Map)null, DfsUtil.buildDeleteParam(param, this.dfsProperties.getAppId(), this.dfsProperties.getSecret()));
            return JSON.parseObject(deleteResult, DeleteResult.class);
        } catch (IOException e) {
            throw new ErrorCodeException(ExceptionEnum.HTTP_REQUEST_ERROR, e);
        }
    }

    public byte[] dfsGetPublicFile(PublicGetFileParam param, int connectTime, int socketConnectTime) {
        try {
            return HttpClientUtils.sendFileDownload(DfsUtil.getPublicFileUrl(this.dfsProperties.getUrl()), DfsUtil.buildGetPublicFileParam(param, this.dfsProperties.getAppId()), connectTime, socketConnectTime);
        } catch (IOException e) {
            throw new ErrorCodeException(ExceptionEnum.HTTP_REQUEST_ERROR, e);
        }
    }

    public byte[] dfsGetFile(GetFileParam param, int connectTime, int socketConnectTime) {
        try {
            return HttpClientUtils.sendFileDownload(DfsUtil.getFileUrl(this.dfsProperties.getUrl()), DfsUtil.buildGetFileParam(param, this.dfsProperties.getAppId(), this.dfsProperties.getSecret()), connectTime, socketConnectTime);
        } catch (IOException e) {
            throw new ErrorCodeException(ExceptionEnum.HTTP_REQUEST_ERROR, e);
        }
    }

    public byte[] dfsGetEncryptFile(GetFileParam param, int connectTime, int socketConnectTime) {
        try {
            return HttpClientUtils.sendFileDownload(DfsUtil.getEncryptFileUrl(this.dfsProperties.getUrl()), DfsUtil.buildGetFileParam(param, this.dfsProperties.getAppId(), this.dfsProperties.getSecret()), connectTime, socketConnectTime);
        } catch (IOException e) {
            throw new ErrorCodeException(ExceptionEnum.HTTP_REQUEST_ERROR, e);
        }
    }

    private GetFileParam createFileParam(String remoteFileId, Integer width, boolean waterMark, String openId, StsResult stsResult) {
        if (StringUtils.isEmpty(stsResult.getSecurityToken())) {
            throw new ErrorCodeException(ExceptionEnum.DFS_SECURITY_TOKEN_NOT_NULL, stsResult.toString());
        }
        GetFileParam.Data data = new GetFileParam.Data();
        data.setSecurityToken(stsResult.getSecurityToken());
        data.setUserName(openId);
        data.setUserType("openid");
        data.setRemoteFileId(remoteFileId);
        data.setWaterMark(waterMark ? "true" : "false");
        GetFileParam param = new GetFileParam();
        param.setWidth(width);
        param.setData(data);
        return param;
    }

    private GetFileParam createFileParam(String remoteFileId, String openId, StsResult stsResult) {
        if(StringUtils.isEmpty(stsResult.getSecurityToken())) {
            throw new ErrorCodeException(ExceptionEnum.DFS_SECURITY_TOKEN_NOT_NULL, new Object[]{stsResult.toString()});
        }
        GetFileParam.Data data = new GetFileParam.Data();
        data.setSecurityToken(stsResult.getSecurityToken());
        data.setUserName(openId);
        data.setUserType("openid");
        data.setRemoteFileId(remoteFileId);
        data.setWaterMark("false");
        GetFileParam param = new GetFileParam();
        param.setData(data);
        return param;
    }

    private PublicGetFileParam createPublicGetFileParam(String remoteFileId, Integer width) {
        PublicGetFileParam publicGetFileParam = new PublicGetFileParam();
        publicGetFileParam.setWidth(width);
        publicGetFileParam.setRemoteFileId(remoteFileId);
        return publicGetFileParam;
    }

    private PublicGetFileParam createPublicGetFileParam(String remoteFileId) {
        PublicGetFileParam publicGetFileParam = new PublicGetFileParam();
        publicGetFileParam.setRemoteFileId(remoteFileId);
        return publicGetFileParam;
    }

    private StsParam createStsParam(String userName) {
        StsParam stsParam = new StsParam();
        stsParam.setUserName(userName);
        return stsParam;
    }

    private DeleteParam createDeleteParam(String remoteFileId, String accessToken) {
        DeleteParam deleteParam = new DeleteParam();
        deleteParam.setAccessToken(accessToken);
        deleteParam.setRemoteFileId(remoteFileId);
        return deleteParam;
    }

    private UploadParam createUploadParam(String group, File file, Long expire) {
        UploadParam uploadParam = new UploadParam();
        uploadParam.setFilename(DfsUtil.getFileNameWithoutSuffix(file));
        uploadParam.setExt(DfsUtil.getFileSuffix(file));
        uploadParam.setRemoteFileId(DfsUtil.getFileNameWithoutSuffix(file).concat(".").concat(DfsUtil.getFileSuffix(file)));
        uploadParam.setGroup(group);
        uploadParam.setExpire(expire);
        return uploadParam;
    }

    private UploadParam createUploadParam(String group, String ext, String filename, Long expire) {
        if (StringUtils.isEmpty(filename)) {
            throw new ErrorCodeException(ExceptionEnum.DFS_FILENAME_NOT_NULL);
        }
        if(Objects.isNull(ext)) ext="";
        UploadParam uploadParam = new UploadParam();
        uploadParam.setRemoteFileId(filename.concat(".").concat(ext));
        uploadParam.setFilename(filename);
        uploadParam.setGroup(group);
        uploadParam.setExt(ext);
        uploadParam.setExpire(expire);
        return uploadParam;
    }

    public UploadResult dfsPrivateUpload(byte[] file, String ext, String filename) {
        if (file != null && file.length > 0) {
            return this.defUpload(this.createUploadParam("private", ext, filename, null), file, connectTime, socketConnectTime, true);
        } else {
            throw new ErrorCodeException(ExceptionEnum.DFS_UPLOAD_BYTE_UNREADABLE);
        }
    }

    public UploadResult dfsPrivateUpload(byte[] file, String ext, String filename, Long expire) {
        if (file != null && file.length > 0) {
            return this.defUpload(this.createUploadParam("private", ext, filename, expire), file, connectTime, socketConnectTime, true);
        } else {
            throw new ErrorCodeException(ExceptionEnum.DFS_UPLOAD_BYTE_UNREADABLE);
        }
    }

    public UploadResult dfsPrivateUpload(byte[] file, String ext, String filename, boolean isEncrypt) {
        if (file != null && file.length > 0) {
            return this.defUpload(this.createUploadParam("private", ext, filename, null), file, connectTime, socketConnectTime, isEncrypt);
        } else {
            throw new ErrorCodeException(ExceptionEnum.DFS_UPLOAD_BYTE_UNREADABLE);
        }
    }

    public UploadResult dfsPrivateUpload(byte[] file, String ext, String filename, boolean isEncrypt, Long expire) {
        if (file != null && file.length > 0) {
            return this.defUpload(this.createUploadParam("private", ext, filename, expire), file, connectTime, socketConnectTime, isEncrypt);
        } else {
            throw new ErrorCodeException(ExceptionEnum.DFS_UPLOAD_BYTE_UNREADABLE);
        }
    }

    public UploadResult dfsPublicUpload(byte[] file, String ext, String filename) {
        if (file != null && file.length > 0) {
            return this.defUpload(this.createUploadParam("public", ext, filename, null), file, connectTime, socketConnectTime, false);
        } else {
            throw new ErrorCodeException(ExceptionEnum.DFS_UPLOAD_BYTE_UNREADABLE);
        }
    }

    public UploadResult dfsPublicUpload(byte[] file, String ext, String filename, Long expire) {
        if (file != null && file.length > 0) {
            return this.defUpload(this.createUploadParam("public", ext, filename, expire), file, connectTime, socketConnectTime, false);
        } else {
            throw new ErrorCodeException(ExceptionEnum.DFS_UPLOAD_BYTE_UNREADABLE);
        }
    }

    public UploadResult dfsPrivateUpload(File file) {
        return this.dfsUploadFile(file, "private", true, null);
    }

    public UploadResult dfsPrivateUpload(File file, Long expire) {
        return this.dfsUploadFile(file, "private", true, expire);
    }

    public UploadResult dfsPublicUpload(File file) {
        return this.dfsUploadFile(file, "public", false, null);
    }

    public UploadResult dfsPublicUpload(File file, Long expire) {
        return this.dfsUploadFile(file, "public", false, expire);
    }

    private UploadResult dfsUploadFile(File file, String privateOrPublic, boolean isEncrypt, Long expire) {
        if (file != null && file.exists() && file.isFile() && file.canRead()) {
            FileInputStream fileInputStream = DfsUtil.toInputStream(file);
            UploadResult ret=null;
            try {
                ret = this.defUploadInStream(this.createUploadParam(privateOrPublic, file, expire), fileInputStream, connectTime, socketConnectTime, isEncrypt);
            } catch (Exception e) {
                logger.error("dfsUploadFile", e);
                throw new ErrorCodeException(ExceptionEnum.DFS_UPLOAD_FILE_UNREADABLE);
            } finally {
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e) {
                        logger.warn("Close InputStream[{}] Error!", file.getName(), e);
                    }
                }
            }
            return ret;
        } else {
            throw new ErrorCodeException(ExceptionEnum.DFS_UPLOAD_FILE_UNREADABLE);
        }
    }

    public DeleteResult dfsDeleteFile(String remoteFileId, String accessToken) {
        if (StringUtils.isNotEmpty(remoteFileId) && StringUtils.isNotEmpty(accessToken)) {
            return this.dfsDelete(this.createDeleteParam(remoteFileId, accessToken), connectTime, socketConnectTime);
        } else {
            throw new ErrorCodeException(ExceptionEnum.DFS_DELETE_PARAM_NOT_NULL);
        }
    }

    public StsResult getSts(String userName) {
        return this.dfsGetSts(this.createStsParam(userName), connectTime, socketConnectTime);
    }

    public byte[] getPublicFile(String remoteFileId, Integer width) {
        return this.dfsGetPublicFile(this.createPublicGetFileParam(remoteFileId, width), connectTime, socketConnectTime);
    }

    public byte[] getPublicFile(String remoteFileId) {
        return this.dfsGetPublicFile(this.createPublicGetFileParam(remoteFileId), connectTime, socketConnectTime);
    }

    public byte[] getFile(String remoteFileId, Integer width, boolean waterMark, String openId, StsResult stsResult) {
        AssertUtil.notNull(stsResult);
        AssertUtil.hasLength(openId, "openId不能为空字符串");
        return this.dfsGetFile(this.createFileParam(remoteFileId, width, waterMark, openId, stsResult), connectTime, socketConnectTime);
    }

    public String getFileUrl(String remoteFileId, Integer width, boolean waterMark, String openId, StsResult stsResult) throws IOException {
        AssertUtil.notNull(stsResult);
        AssertUtil.hasLength(openId, "openId不能为空字符串");
        GetFileParam fileParam = this.createFileParam(remoteFileId, width, waterMark, openId, stsResult);
        Map<String, String> stringStringMap = DfsUtil.buildGetFileParam(fileParam, this.dfsProperties.getAppId(), this.dfsProperties.getSecret());
        return HttpClientUtils.urlParamJoint(DfsUtil.getFileUrl(this.dfsProperties.getUrl()), stringStringMap);
    }

    public byte[] getFile(String remoteFileId, String openId, StsResult stsResult) {
        AssertUtil.notNull(stsResult);
        AssertUtil.hasLength(openId, "openId不能为空字符串");
        return this.dfsGetFile(this.createFileParam(remoteFileId, openId, stsResult), connectTime, socketConnectTime);
    }

    public String getFileUrl(String remoteFileId, String openId) throws IOException {
        StsResult sts = this.getSts(openId);
        GetFileParam fileParam = this.createFileParam(remoteFileId, openId, sts);
        Map<String, String> stringStringMap = DfsUtil.buildGetFileParam(fileParam, this.dfsProperties.getAppId(), this.dfsProperties.getSecret());
        return HttpClientUtils.urlParamJoint(DfsUtil.getFileUrl(this.dfsProperties.getUrl()), stringStringMap);
    }

    public byte[] getEncryptFile(String remoteFileId, String openId, StsResult stsResult) {
        AssertUtil.notNull(stsResult);
        AssertUtil.hasLength(openId, "openId不能为空字符串");
        return this.dfsGetEncryptFile(this.createFileParam(remoteFileId, openId, stsResult), connectTime, socketConnectTime);
    }

    public String getEncryptFileUrl(String remoteFileId, String openId) throws IOException {
        StsResult sts = this.getSts(openId);
        GetFileParam fileParam = this.createFileParam(remoteFileId, openId, sts);
        Map<String, String> stringStringMap = DfsUtil.buildGetFileParam(fileParam, this.dfsProperties.getAppId(), this.dfsProperties.getSecret());
        return HttpClientUtils.urlParamJoint(DfsUtil.getEncryptFileUrl(this.dfsProperties.getUrl()), stringStringMap);
    }

    public String getExtranetUrl(String remoteFileId, String openId) throws IOException {
        String extranetUrl = this.dfsProperties.getExtranetUrl();
        AssertUtil.hasLength(extranetUrl, "请先配置msb.dfs.extranetUrl指定外网域名");
        StsResult sts = this.getSts(openId);
        GetFileParam fileParam = this.createFileParam(remoteFileId, openId, sts);
        Map<String, String> stringStringMap = DfsUtil.buildGetFileParam(fileParam, this.dfsProperties.getAppId(), this.dfsProperties.getSecret());
        return HttpClientUtils.urlParamJoint(DfsUtil.getEncryptFileUrl(extranetUrl), stringStringMap);
    }

    public String getExtranetImageUrl(String remoteFileId, Integer width, boolean waterMark, String openId, StsResult stsResult) throws IOException {
        AssertUtil.notNull(stsResult);
        AssertUtil.hasLength(openId, "openId不能为空字符串");
        String extranetUrl = this.dfsProperties.getExtranetUrl();
        AssertUtil.hasLength(extranetUrl, "请先配置msb.dfs.extranetUrl指定外网域名");
        GetFileParam fileParam = this.createFileParam(remoteFileId, width, waterMark, openId, stsResult);
        Map<String, String> stringStringMap = DfsUtil.buildGetFileParam(fileParam, this.dfsProperties.getAppId(), this.dfsProperties.getSecret());
        return HttpClientUtils.urlParamJoint(DfsUtil.getEncryptFileUrl(extranetUrl), stringStringMap);
    }

    public byte[] getEncryptFile(String remoteFileId, Integer width, boolean waterMark, String openId, StsResult stsResult) {
        AssertUtil.notNull(stsResult);
        AssertUtil.hasLength(openId, "openId不能为空字符串");
        return this.dfsGetEncryptFile(this.createFileParam(remoteFileId, width, waterMark, openId, stsResult), connectTime, socketConnectTime);
    }

    public String getEncryptFileUrl(String remoteFileId, Integer width, boolean waterMark, String openId, StsResult stsResult) throws IOException {
        AssertUtil.notNull(stsResult);
        AssertUtil.hasLength(openId, "openId不能为空字符串");
        GetFileParam fileParam = this.createFileParam(remoteFileId, width, waterMark, openId, stsResult);
        Map<String, String> stringStringMap = DfsUtil.buildGetFileParam(fileParam, this.dfsProperties.getAppId(), this.dfsProperties.getSecret());
        return HttpClientUtils.urlParamJoint(DfsUtil.getEncryptFileUrl(this.dfsProperties.getUrl()), stringStringMap);
    }

    public Boolean checkFileExist(String fileName) {
        try {
            String result = HttpClientUtils.sendHttpUrlEncodedPost(DfsUtil.getCheckUrl(this.dfsProperties.getUrl()), connectTime, socketConnectTime, (Map)null, DfsUtil.buildCheckParam(this.dfsProperties.getAppId(), fileName));
            DefaultResult deFaultResult = JSON.parseObject(result, DefaultResult.class);
            return deFaultResult.getStatus();
        } catch (IOException e) {
            throw new ErrorCodeException(ExceptionEnum.HTTP_REQUEST_ERROR, e);
        }
    }

    public static int getConnectTime() {
        return connectTime;
    }

    public static void setConnectTime(int connectTime) {
        FastDfsTemplate.connectTime = connectTime;
    }

    public static int getSocketConnectTime() {
        return socketConnectTime;
    }

    public static void setSocketConnectTime(int socketConnectTime) {
        FastDfsTemplate.socketConnectTime = socketConnectTime;
    }
}
