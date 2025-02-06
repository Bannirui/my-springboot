package com.github.bannirui.msb.enums;

/**
 * 异常类型.
 */
public enum ExceptionEnum implements ErrorCode {
    INITIALIZATION_EXCEPTION("SYS_000", "{0}初始化失败，参数列表:{1}"),
    PARAM_EXCEPTION("SYS_001", "{0}参数异常，参数列表:{1}"),
    FILE_EXCEPTION("SYS_002", "{0}操作文件发生异常"),
    CONFIG_WEIGHT_RATIO_ERROR("config_001", "权重比例设置不能大于100并且不能小于0"),
    ORM_TRANSACTION_ERROR("ORM_001", "存在多个TransactionManager Bean 请在@MultiTransactional 中明确指定"),
    ORM_CONFIG_NAME_ERROR("ORM_002", "@MultiTransactional 中指定的configName 错误"),
    ;

    private final String code;
    private final String msg;

    ExceptionEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getMsg() {
        return this.msg;
    }
}
