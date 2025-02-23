package com.github.bannirui.msb.dfs.result;

public class UploadResult {
    private Boolean status;
    private String message;
    private String statusCode;
    private UploadResult.Result result;

    public Boolean getStatus() {
        return this.status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatusCode() {
        return this.statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public UploadResult.Result getResult() {
        return this.result;
    }

    public void setResult(UploadResult.Result result) {
        this.result = result;
    }

    public static class Result {
        private String url;
        private String group;
        private String fileName;

        public String getUrl() {
            return this.url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getGroup() {
            return this.group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public String getFileName() {
            return this.fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }
    }
}
