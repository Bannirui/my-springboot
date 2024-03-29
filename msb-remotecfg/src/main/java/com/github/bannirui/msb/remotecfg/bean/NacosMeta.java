package com.github.bannirui.msb.remotecfg.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * 交互nacos的要素.
 */
public class NacosMeta {

    private String server;
    private String namespace;
    private List<String> dataIds;

    public NacosMeta() {
        this.dataIds = new ArrayList<>();
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public List<String> getDataIds() {
        return dataIds;
    }

    public void setDataIds(List<String> dataIds) {
        this.dataIds = dataIds;
    }
}
