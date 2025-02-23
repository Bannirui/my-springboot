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
    HTTP_REQUEST_ERROR("SOA_006", "HTTP请求错误,errorMessage:{0}"),
    HTTP_REQUEST_CONSTRUCTOR_ERROR("SOA_007", "Http 请求body构造器配置异常 必须为HttpRequestBodyConstructor 的实现类并注册为spring容器管理的bean"),
    HTTP_REQUEST_ENTITY_ERROR("SOA_008", "Http请求实体错误"),
    HTTP_CONSTRUCTOR_CREATE_ERROR("SOA_009", "Http请求构造器创建错误"),

    DFS_SECRET_NOT_FIND("DFS_003", "fastDfs的secret没有在配置中找到"),
    DFS_APPID_NOT_FIND("DFS_004", "appid没有在配置中找到"),
    DFS_UPLOAD_FILE_UNREADABLE("DFS_007", "请检查本地文件是否有读取权限或者是否存在"),
    DFS_UPLOAD_BYTE_UNREADABLE("DFS_008", "byte[]不可读取"),
    DFS_FILENAME_NOT_NULL("DFS_009", "FileName不能为空"),
    DFS_FILE_TO_BYTE_ERROR("DFS_010", "{0}File转byte[]失败"),
    DFS_DELETE_PARAM_NOT_NULL("DFS_011", "accessToken或remoteFileId不能为空"),
    DFS_SECURITY_TOKEN_NOT_NULL("DFS_012", "securityToken不允许为null,StsResult:{0}"),
    DFS_FILE_TO_INPUTSTREAM_ERROR("DFS_013", "{0}File转InputStream失败"),
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
