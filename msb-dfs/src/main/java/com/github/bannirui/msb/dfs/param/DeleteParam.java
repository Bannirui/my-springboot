package com.github.bannirui.msb.dfs.param;

public class DeleteParam {
    private String remoteFileId;
    private String accessToken;

    public String getRemoteFileId() {
        return this.remoteFileId;
    }

    public void setRemoteFileId(String remoteFileId) {
        this.remoteFileId = remoteFileId;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
