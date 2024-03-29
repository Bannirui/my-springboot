package com.github.bannirui.msb.common.constant;

/**
 * 强制上层应用必须存在一个配置文件放上应用标识.
 * 文件=/META-INF/application.yml
 * key=app.id
 * val=xxx
 */
public interface AppCfg {

    // 应用的唯一标识
    String APP_ID_KEY = "app.id";
}
