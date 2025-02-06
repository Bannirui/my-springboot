package com.github.bannirui.msb.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PageResult<T> implements Serializable {

    private int pageSize;
    private int currentPage;
    private int pageCount;
    private long total;
    private List<T> data;
    private List<String> sortFields;

    public PageResult() {
        this.sortFields = new ArrayList<>();
    }

    public PageResult(int pageSize, int currentPage, int pageCount, List<T> data) {
        this(pageSize, currentPage, pageCount, 0L, data, null);
    }

    public PageResult(int pageSize, int currentPage, int pageCount, long total, List<T> data) {
        this(pageSize, currentPage, pageCount, total, data, null);
    }

    public PageResult(int pageSize, int currentPage, int pageCount, List<T> data, List<String> sortFields) {
        this(pageSize, currentPage, pageCount, 0L, data, sortFields);
    }

    public PageResult(int pageSize, int currentPage, int pageCount, long total, List<T> data, List<String> sortFields) {
        this.sortFields = new ArrayList<>();
        this.pageSize = pageSize;
        this.currentPage = currentPage;
        this.pageCount = pageCount;
        this.data = data;
        this.total = total;
        if (null != sortFields) {
            this.sortFields = sortFields;
        }
    }

    public long getTotal() {
        return this.total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<String> getSortFields() {
        return this.sortFields;
    }

    public void setSortFields(List<String> sortFields) {
        this.sortFields = sortFields;
    }

    public int getPageSize() {
        return this.pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getCurrentPage() {
        return this.currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageCount() {
        return this.pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public List<T> getData() {
        return this.data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}
