package com.github.bannirui.msb.compensate;

import java.io.Serializable;
import java.util.Arrays;

public class CsaRecord implements Serializable {

    private Long id;
    private String appId;
    private String clientId;
    private String resource;
    private String type;
    private String interfaceName;
    private String className;
    private String beanName;
    private String methodName;
    private byte[] paramValue;
    private byte[] paramType;
    private String returnType;
    private String status;
    private String valueJson;
    private String typeJson;

    public CsaRecord() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public byte[] getParamValue() {
        return paramValue;
    }

    public void setParamValue(byte[] paramValue) {
        this.paramValue = paramValue;
    }

    public byte[] getParamType() {
        return paramType;
    }

    public void setParamType(byte[] paramType) {
        this.paramType = paramType;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getValueJson() {
        return valueJson;
    }

    public void setValueJson(String valueJson) {
        this.valueJson = valueJson;
    }

    public String getTypeJson() {
        return typeJson;
    }

    public void setTypeJson(String typeJson) {
        this.typeJson = typeJson;
    }

    @Override
    public String toString() {
        return "CsaRecord{" +
            "id=" + id +
            ", appId='" + appId + '\'' +
            ", clientId='" + clientId + '\'' +
            ", resource='" + resource + '\'' +
            ", type='" + type + '\'' +
            ", interfaceName='" + interfaceName + '\'' +
            ", className='" + className + '\'' +
            ", beanName='" + beanName + '\'' +
            ", methodName='" + methodName + '\'' +
            ", paramValue=" + Arrays.toString(paramValue) +
            ", paramType=" + Arrays.toString(paramType) +
            ", returnType='" + returnType + '\'' +
            ", status='" + status + '\'' +
            ", valueJson='" + valueJson + '\'' +
            ", typeJson='" + typeJson + '\'' +
            '}';
    }
}
