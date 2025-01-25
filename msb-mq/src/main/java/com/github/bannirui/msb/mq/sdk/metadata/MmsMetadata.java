package com.github.bannirui.msb.mq.sdk.metadata;

import com.github.bannirui.msb.mq.sdk.common.MmsConst;
import com.github.bannirui.msb.mq.sdk.common.MmsEnv;
import com.github.bannirui.msb.mq.sdk.common.MmsType;
import com.github.bannirui.msb.mq.sdk.utils.Utils;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

/**
 * 注册中心存储的my message service的元数据
 * <ul>
 *     <li>topic</li>
 *     <li>consumer group</li>
 * </ul>
 */
public class MmsMetadata {

    /**
     * {@link MmsType}
     * topic还是consumer group
     */
    private String type;
    /**
     * 作为zk节点名称
     * <ul>
     *     <li>/mms/topic/${name}</li>
     *     <li>/mms/consumergroup/${name}</li>
     * </ul>
     */
    private String name;
    private ClusterMetadata clusterMetadata;
    private String domain;
    private String gatedIps;
    private ClusterMetadata gatedCluster;
    private String statisticsLogger;
    private Boolean isEncrypt;

    public Boolean getIsEncrypt() {
        return this.isEncrypt;
    }

    public void setIsEncrypt(Boolean encrypt) {
        this.isEncrypt = encrypt;
    }

    public String getMmsClusterPath() {
        return Utils.buildPath(MmsConst.ZK.CLUSTER_ZKPATH, this.clusterMetadata.getClusterName());
    }

    public String getMmsPath() {
        return this.isTopic() ? Utils.buildPath(MmsConst.ZK.TOPIC_ZKPATH, this.name) : Utils.buildPath(MmsConst.ZK.CONSUMERGROUP_ZKPATH, this.name);
    }

    public boolean isTopic() {
        return MmsType.TOPIC.getName().equalsIgnoreCase(this.type);
    }

    public String getStatisticsLogger() {
        return this.statisticsLogger;
    }

    public void setStatisticsLogger(String statisticsLogger) {
        this.statisticsLogger = statisticsLogger;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ClusterMetadata getClusterMetadata() {
        return this.clusterMetadata;
    }

    public void setClusterMetadata(ClusterMetadata clusterMetadata) {
        this.clusterMetadata = clusterMetadata;
    }

    public String getDomain() {
        return this.domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getGatedIps() {
        return this.gatedIps;
    }

    public void setGatedIps(String gatedIps) {
        this.gatedIps = gatedIps;
    }

    public ClusterMetadata getGatedCluster() {
        return this.gatedCluster;
    }

    public void setGatedCluster(ClusterMetadata gatedCluster) {
        this.gatedCluster = gatedCluster;
    }

    public boolean isGatedLaunch() {
        return StringUtils.isNotBlank(this.gatedIps) && this.gatedIps.contains(MmsEnv.MMS_IP);
    }

    public String toString() {
        return "MmsMetadata{type='" + this.type + '\'' + ", name='" + this.name + '\'' + ", clusterMetadata=" + this.clusterMetadata.toString() + ", domain='" + this.domain + '\'' + ", gatedCluster='" + (this.gatedCluster != null ? this.gatedCluster.toString() : "") + '\'' + ", gatedIps='" + this.gatedIps + '\'' + '}';
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            MmsMetadata that = (MmsMetadata)o;
            return Objects.equals(this.type, that.type) && Objects.equals(this.name, that.name) && Objects.equals(this.clusterMetadata, that.clusterMetadata) && Objects.equals(this.domain, that.domain) && Objects.equals(this.gatedIps, that.gatedIps) && Objects.equals(this.gatedCluster, that.gatedCluster) && Objects.equals(this.isEncrypt, that.isEncrypt);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hashCode(new Object[]{this.type, this.name, this.clusterMetadata, this.domain, this.gatedIps, this.gatedCluster, this.isEncrypt});
    }
}
