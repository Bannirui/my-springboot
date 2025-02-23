package com.github.bannirui.msb.dfs.param;

public class UploadParam {
    private String ext;
    private String group;
    private String filename;
    private String remoteFileId;
    private Long expire;

    public Long getExpire() {
        return this.expire;
    }

    public void setExpire(Long expire) {
        this.expire = expire;
    }

    public String getExt() {
        return this.ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public String getGroup() {
        return this.group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getFilename() {
        return this.filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getRemoteFileId() {
        return this.remoteFileId;
    }

    public void setRemoteFileId(String remoteFileId) {
        this.remoteFileId = remoteFileId;
    }
}
