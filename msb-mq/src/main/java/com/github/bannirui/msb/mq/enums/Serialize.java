package com.github.bannirui.msb.mq.enums;

public enum Serialize {
    STRING("String"),
    JSON("JSON"),
    JSON_ARRAY("JSONArray");

    private String value;

    private Serialize(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
