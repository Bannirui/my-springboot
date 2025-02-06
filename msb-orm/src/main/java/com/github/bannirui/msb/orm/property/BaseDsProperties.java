package com.github.bannirui.msb.orm.property;

import com.zaxxer.hikari.HikariConfig;

public class BaseDsProperties {
    private String name;
    private HikariConfig hikari;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HikariConfig getHikari() {
        return this.hikari;
    }

    public void setHikari(HikariConfig hikari) {
        this.hikari = hikari;
    }
}
