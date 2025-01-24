package com.github.bannirui.msb.mq.sdk.page;

import com.google.common.collect.Lists;
import java.util.List;

public class PageResult<T> {
    private int currentPage;
    private int pageSize;
    private long total;
    private List<T> records;

    public PageResult(int currentPage, int pageSize, long total, List<T> records) {
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.total = total;
        this.records = records;
    }

    public PageResult(int currentPage, int pageSize) {
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.records = Lists.newArrayList();
    }

    private PageResult() {
    }

    public int getCurrentPage() {
        return this.currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return this.pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotal() {
        return this.total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<?> getRecords() {
        return this.records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }
}
