package com.github.bannirui.msb.endpoint.health;

import java.util.HashMap;

public class Health extends HashMap<String, String> {
    public static final String STATUS_UP = "UP";
    public static final String STATUS_DOWN = "DOWN";
    public static final String OUT_OF_SERVICE = "OUT_OF_SERVICE";

    public static Health build() {
        return new Health();
    }

    public Health down() {
        this.put("status", "DOWN");
        return this;
    }

    public Health down(String error) {
        this.down().withException(error);
        return this;
    }

    public Health withException(String error) {
        this.put("error", error);
        return this;
    }

    public Health outOfService() {
        this.put("status", "OUT_OF_SERVICE");
        return this;
    }

    public Health up() {
        this.put("status", "UP");
        return this;
    }

    public Health withDetail(String key, String value) {
        this.put(key, value);
        return this;
    }
}
