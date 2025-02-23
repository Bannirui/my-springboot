package com.github.bannirui.msb.web.session;

public interface SessionConst {
    String REDIS = "com.github.bannirui.msb.web.session.RedisSessionStorageImpl";
    String DEFAULT = "com.github.bannirui.msb.web.session.MapSessionStorageImpl";
    String X_AUTH_TOKEN = "x-auth-token";
    String COOKIE = "COOKIE";
}
