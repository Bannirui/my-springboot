package com.github.bannirui.msb.common.compensate;

import java.io.Serializable;

public class CsaMQRequest implements Serializable {
    private Long id;
    private String status;

    public CsaMQRequest() {
    }

    public CsaMQRequest(Long id, String status) {
        this.id = id;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "CsaMQRequest{" +
            "id=" + id +
            ", status='" + status + '\'' +
            '}';
    }
}
