package com.github.bannirui.msb.mq.sdk.metadata;

import com.github.bannirui.msb.mq.sdk.common.BrokerType;

/**
 * 存在注册中心的my message service集群信息
 */
public class ClusterMetadata {
    private String clusterName;
    /**
     * <ul>
     *     <li>RocketMQ name server, host:9876</li>
     * </ul>
     */
    private String bootAddr;
    /**
     * mq中间件类型
     * <ul>
     *     <li>rocket</li>
     *     <li>kafka</li>
     * </ul>
     */
    private BrokerType brokerType;
    private String serverIps;

    public String getClusterName() {
        return this.clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getBootAddr() {
        return this.bootAddr;
    }

    public void setBootAddr(String bootAddr) {
        this.bootAddr = bootAddr;
    }

    public BrokerType getBrokerType() {
        return this.brokerType;
    }

    public void setBrokerType(BrokerType brokerType) {
        this.brokerType = brokerType;
    }

    public String getServerIps() {
        return this.serverIps;
    }

    public void setServerIps(String serverIps) {
        this.serverIps = serverIps;
    }

    public String toString() {
        return "ClusterMetadata{clusterName='" + this.clusterName + '\'' + ", bootAddr='" + this.bootAddr + '\'' + ", brokerType=" + this.brokerType + ", serverIps='" + this.serverIps + '\'' + '}';
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof ClusterMetadata)) {
            return false;
        } else {
            ClusterMetadata metadata;
            label: {
                metadata = (ClusterMetadata)o;
                if (this.clusterName != null) {
                    if (this.clusterName.equals(metadata.clusterName)) {
                        break label;
                    }
                } else if (metadata.clusterName == null) {
                    break label;
                }
                return false;
            }
            if (this.bootAddr != null) {
                if (!this.bootAddr.equals(metadata.bootAddr)) {
                    return false;
                }
            } else if (metadata.bootAddr != null) {
                return false;
            }
            if (this.brokerType != metadata.brokerType) {
                return false;
            } else {
                return this.serverIps != null ? this.serverIps.equals(metadata.serverIps) : metadata.serverIps == null;
            }
        }
    }

    public int hashCode() {
        int result = this.clusterName != null ? this.clusterName.hashCode() : 0;
        result = 31 * result + (this.bootAddr != null ? this.bootAddr.hashCode() : 0);
        result = 31 * result + (this.brokerType != null ? this.brokerType.hashCode() : 0);
        result = 31 * result + (this.serverIps != null ? this.serverIps.hashCode() : 0);
        return result;
    }
}
