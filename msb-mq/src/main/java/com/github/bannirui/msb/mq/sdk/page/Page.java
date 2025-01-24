package com.github.bannirui.msb.mq.sdk.page;

public class Page {
    private Integer currentPage = 1;
    private Integer pageSize = 25;
    private int offset = 0;
    private String keyWord;

    public String getKeyWord() {
        return this.keyWord;
    }

    public void setKeyWord(String keyWord) {
        this.keyWord = keyWord;
    }

    public int getOffset() {
        return (this.currentPage - 1) * this.pageSize;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public Integer getCurrentPage() {
        return this.currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public Integer getPageSize() {
        return this.pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
