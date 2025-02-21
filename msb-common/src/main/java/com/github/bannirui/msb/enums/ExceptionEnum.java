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

    CONFIG_DUBBO_AUTH_CONFIG_ERROR("config_dubbo_002", "系统对dubbo鉴权的配置错误"),
    CONFIG_DUBBO_AUTH_FAILURE("config_dubbo_003", "服务端拦截器权限验证失败"),

    HTTP_UNKNOWN_ERROR("SOA_001", "HttpClient未知错误"),
    HTTP_ANNOTATION_PARAM_ERROR("SOA_003", "每个注解必须与形参相对应"),
    HTTP_NNOTATION_LACK_ERROR("SOA_004", "目标方法必须标注一个注解(HttpExecute或HttpJsonExecute)"),
    HTTP_ANNOTATION_ERROR("SOA_005", "目标方法只能标注一个注解(HttpExecute或HttpJsonExecute)"),
    HTTP_REQUEST_CONSTRUCTOR_ERROR("SOA_007", "Http 请求body构造器配置异常 必须为HttpRequestBodyConstructor 的实现类并注册为spring容器管理的bean"),
    HTTP_REQUEST_ENTITY_ERROR("SOA_008", "Http请求实体错误"),
    HTTP_CONSTRUCTOR_CREATE_ERROR("SOA_009", "Http请求构造器创建错误"),
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
