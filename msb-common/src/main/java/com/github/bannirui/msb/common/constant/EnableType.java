package com.github.bannirui.msb.common.constant;

import java.util.Arrays;

/**
 * 场景启动器.
 */
public enum EnableType {
    ENABLE_WEB("EnableMsbWeb", "MsbWebImportSelector"),
    ENABLE_SSO("EnableMsbSso", "MsbSsoImportSelector"),
    ENABLE_HTTP("EnableMsbHttp", "MsbHttpImportSelector"),
    ENABLE_REMOTE_CONFIG("EnableMsbRemoteCfg", "MsbRemoteCfgImportSelector"),
    ;

    // EnableXXX 启动器
    private final String starter;

    // 对应的ImportSelector
    private final String importer;

    private EnableType(String starter, String importer) {
        this.starter = starter;
        this.importer = importer;
    }

    public String getStarter() {
        return starter;
    }

    public String getImporter() {
        return this.importer;
    }

    public static EnableType get8Importer(String name) {
        return Arrays.stream(values()).filter(e -> e.getImporter().equals(name)).findFirst().orElseGet(() -> null);
    }
}
