package com.github.bannirui.msb.dfs.param;

public class GetFileParam {
    private Integer width;
    private GetFileParam.Data data;

    public Integer getWidth() {
        return this.width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public GetFileParam.Data getData() {
        return this.data;
    }

    public void setData(GetFileParam.Data data) {
        this.data = data;
    }

    public static class Data {
        private String remoteFileId;
        private String waterMark;
        private String userName;
        private String userType;
        private String securityToken;

        public String getRemoteFileId() {
            return this.remoteFileId;
        }

        public void setRemoteFileId(String remoteFileId) {
            this.remoteFileId = remoteFileId;
        }

        public String getWaterMark() {
            return this.waterMark;
        }

        public void setWaterMark(String waterMark) {
            this.waterMark = waterMark;
        }

        public String getUserName() {
            return this.userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getUserType() {
            return this.userType;
        }

        public void setUserType(String userType) {
            this.userType = userType;
        }

        public String getSecurityToken() {
            return this.securityToken;
        }

        public void setSecurityToken(String securityToken) {
            this.securityToken = securityToken;
        }
    }
}
