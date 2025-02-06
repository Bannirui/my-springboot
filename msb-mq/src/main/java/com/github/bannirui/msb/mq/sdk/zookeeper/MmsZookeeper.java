package com.github.bannirui.msb.mq.sdk.zookeeper;

import com.github.bannirui.msb.ex.FrameworkException;
import com.github.bannirui.msb.mq.sdk.common.BrokerType;
import com.github.bannirui.msb.mq.sdk.common.MmsConst;
import com.github.bannirui.msb.mq.sdk.common.MmsException;
import com.github.bannirui.msb.mq.sdk.common.MmsType;
import com.github.bannirui.msb.mq.sdk.metadata.ClusterMetadata;
import com.github.bannirui.msb.mq.sdk.metadata.ConsumerGroupMetadata;
import com.github.bannirui.msb.mq.sdk.metadata.TopicMetadata;
import com.github.bannirui.msb.mq.sdk.metadata.MmsMetadata;
import com.github.bannirui.msb.mq.sdk.utils.Utils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 原生zk客户端
 */
public class MmsZookeeper extends ZooKeeper {

    private static final Logger logger = LoggerFactory.getLogger(MmsZookeeper.class);

    /**
     * 连接zk服务端
     */
    public MmsZookeeper(String connectString, int sessionTimeout, Watcher watcher) throws IOException {
        super(connectString, sessionTimeout, watcher);
    }

    /**
     * 写cluster
     * @param clusterName cluster的子节点 路径/mms/cluster/${clusterName}
     * @param zkMeta 要写入znode的数据
     */
    public void writeClusterMetadata(String clusterName, String zkMeta) {
        // 节点路径
        String path = Utils.buildPath(MmsConst.ZK.CLUSTER_ZKPATH, clusterName);
        try {
            Stat exists = super.exists(path, false);
            if (Objects.isNull(exists)) {
                // 新增
                super.create(path, zkMeta.getBytes(StandardCharsets.UTF_8), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            } else {
                // 更新 不指定版本
                super.setData(path, zkMeta.getBytes(StandardCharsets.UTF_8), -1);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 写topic
     * @param topicName topic节点名称 路径/mms/topic/${topicName}
     * @param topicZkData topic数据
     */
    public void writeTopicMetadata(String topicName, String topicZkData) {
        try {
            String path = Utils.buildPath(MmsConst.ZK.TOPIC_ZKPATH, topicName);
            Stat exists=super.exists(path, false);
            if (Objects.isNull(exists)) {
                // insert
                super.create(path, topicZkData.getBytes(StandardCharsets.UTF_8), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            } else {
                // update 不指定版本
                super.setData(path, topicZkData.getBytes(StandardCharsets.UTF_8), -1);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 写consumer group
     * @param cgName ConsumerGroup节点名称 路径/mms/consumergroup/${ConsumerGroupName}
     * @param cgZkData consumer group数据
     */
    public void writeConsumerGroupMetadata(String cgName, String cgZkData) {
        try {
            String path = Utils.buildPath(MmsConst.ZK.CONSUMERGROUP_ZKPATH, cgName);
            Stat exists = super.exists(path, false);
            if (Objects.isNull(exists)) {
                // insert
                super.create(path, cgZkData.getBytes(StandardCharsets.UTF_8), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            } else {
                // update 不指定版本
                super.setData(path, cgZkData.getBytes(StandardCharsets.UTF_8), -1);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除cluster数据
     * @param clusterName cluster子节点名称 /mms/cluster/${ClusterName}
     */
    public void deleteCluster(String clusterName) {
        try {
            // 不指定版本
            super.delete(Utils.buildPath(MmsConst.ZK.CLUSTER_ZKPATH, clusterName), -1);
        } catch (InterruptedException | KeeperException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除topic数据
     * @param topicName topic子节点名称 路径/mms/topic/${TopicName}
     */
    public void deleteTopic(String topicName) {
        try {
            // 不指定版本
            super.delete(Utils.buildPath(MmsConst.ZK.TOPIC_ZKPATH, topicName), -1);
        } catch (InterruptedException | KeeperException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除ConsumerGroup数据
     * @param consumerGroupName ConsumerGroup子节点名称 /mms/consumergroup/${ConsumerGroup}
     */
    public void deleteConsumerGroup(String consumerGroupName) {
        try {
            // 不指定版本
            super.delete(Utils.buildPath(MmsConst.ZK.CONSUMERGROUP_ZKPATH, consumerGroupName), -1);
        } catch (InterruptedException | KeeperException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 读取cluster数据
     * @param clusterName cluster子节点名称 /mms/cluster/${ClusterName}
     * @return cluster数据
     */
    public ClusterMetadata readClusterMetadata(String clusterName) {
        try {
            String path = Utils.buildPath(MmsConst.ZK.CLUSTER_ZKPATH, clusterName);
            byte[] data = super.getData(path, false, null);
            if(Objects.isNull(data)) {
                logger.error("cluster {} metadata is empty", clusterName);
                throw MmsException.CLUSTER_INFO_EXCEPTION;
            }
            String clusterData = new String(data, StandardCharsets.UTF_8);
            Properties clusterProperties = null;
            ClusterMetadata metadata = new ClusterMetadata();
            try {
                clusterProperties = Utils.parseProperties(clusterData);
                metadata.setBootAddr(clusterProperties.getProperty("bootAddr"));
                metadata.setBrokerType(BrokerType.parseFrom(clusterProperties.getProperty("brokerType")));
                metadata.setClusterName(clusterName);
                metadata.setServerIps(clusterProperties.getProperty("serverIps"));
                return metadata;
            } catch (IOException e) {
                logger.error("parse cluster {}  data info {} error", clusterName, clusterData);
                throw MmsException.CLUSTER_INFO_EXCEPTION;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 准备写入topic或者consumer group信息 前置校验是否已经存在节点
     * @param type 标识topic或consumer group
     * @param name 子节点名称 再拼接topic或者consumer group的路径前缀
     * @return <t>TRUE</t>标识节点已经存在 <t>FALSE</t>标识节点不存在
     */
    public boolean checkPath(MmsType type, String name) {
        boolean isTopic = MmsType.TOPIC.getName().equalsIgnoreCase(type.getName());
        String zkPath = isTopic ? Utils.buildPath(MmsConst.ZK.TOPIC_ZKPATH, name) : Utils.buildPath(MmsConst.ZK.CONSUMERGROUP_ZKPATH, name);
        try {
            Stat exists = super.exists(zkPath, false);
            return Objects.nonNull(exists);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 读取topic或者consumer group数据
     * @param type {@link MmsType}标识topic还是consumer group
     * @param name 子节点名称 拼接上topic或者consumer group父节点就是完整路径
     * @return topic或者consumer group数据
     */
    private MmsMetadata readZkInfo(MmsType type, String name) {
        try {
            boolean isTopic = Objects.equals(type, MmsType.TOPIC);
            // 派生类
            MmsMetadata metadata = isTopic ? new TopicMetadata() : new ConsumerGroupMetadata();
            // zk节点路径
            String zkPath = isTopic ? Utils.buildPath(MmsConst.ZK.TOPIC_ZKPATH, name) : Utils.buildPath(MmsConst.ZK.CONSUMERGROUP_ZKPATH, name);
            Stat exists = super.exists(zkPath, false);
            if (Objects.isNull(exists)) {
                logger.error("zk path not existed: {}", zkPath);
                throw FrameworkException.getInstance("zk节点[{0}]不存在", zkPath);
            }
            byte[] data = super.getData(zkPath, false, null);
            if(Objects.isNull(data)) {
                logger.error("zk data is null for path: {}", zkPath);
                throw FrameworkException.getInstance("zk节点[{0}]数据不存在", zkPath);
            }
            Properties properties = Utils.parseProperties(new String(data));
            metadata.setName(name);
            metadata.setType(type.getName());
            metadata.setGatedIps(properties.getProperty("gatedIps"));
            metadata.setStatisticsLogger(properties.getProperty("statisticsLogger"));
            String clusterName = properties.getProperty("clusterName");
            ClusterMetadata clusterMetadata = this.readClusterMetadata(clusterName);
            metadata.setClusterMetadata(clusterMetadata);
            String gatedClusterName = properties.getProperty("gatedCluster");
            if (StringUtils.isNotBlank(gatedClusterName)) {
                ClusterMetadata gatedCluster = this.readClusterMetadata(gatedClusterName);
                metadata.setGatedCluster(gatedCluster);
            }
            if (isTopic) {
                // topic
                String isEncrypt = properties.getProperty("isEncrypt");
                metadata.setIsEncrypt(StringUtils.isNotBlank(isEncrypt) && Boolean.parseBoolean(isEncrypt));
            } else {
                // consumer group
                ((ConsumerGroupMetadata)metadata).setBindingTopic(properties.getProperty("bindingTopic"));
                ((ConsumerGroupMetadata)metadata).setBroadcast(properties.getProperty("broadcast"));
                ((ConsumerGroupMetadata)metadata).setConsumeFrom(properties.getProperty("consumeFrom"));
                ((ConsumerGroupMetadata)metadata).setSuspend(properties.getProperty("suspend"));
                if (StringUtils.isBlank(properties.getProperty("releaseStatus"))) {
                    ((ConsumerGroupMetadata)metadata).setReleaseStatus(null);
                } else {
                    ((ConsumerGroupMetadata)metadata).setReleaseStatus(properties.getProperty("releaseStatus"));
                }
            }
            return metadata;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * zk中保存的topic元数据
     * @param name topic的子节点名称 路径是/mms/topic/${name}
     * @return topic数据
     */
    public TopicMetadata readTopicMetadata(String name) {
        return (TopicMetadata)this.readZkInfo(MmsType.TOPIC, name);
    }

    /**
     * zk中保存的ConsumerGroup元数据
     * @param name 子节点名称 路径是/mms/consumergroup/${name}
     * @return consumer group数据
     */
    public ConsumerGroupMetadata readConsumerGroupMetadata(String name) {
        return (ConsumerGroupMetadata) this.readZkInfo(MmsType.CONSUMER_GROUP, name);
    }

    /**
     * java entity拼接成字符串
     * <ul>
     *     <li>用换行符分割 便于将来用{@link Properties}读取</li>
     *     <li>序列化成byte存到zk节点</li>
     * </ul>
     * @param topicMetadata topic数据
     * @return 字符串形式
     */
    public String buildTopicData(TopicMetadata topicMetadata) {
        StringBuilder sb = new StringBuilder();
        sb.append("clusterName=" + topicMetadata.getClusterMetadata().getClusterName());
        sb.append(System.lineSeparator());
        sb.append("type=" + MmsType.TOPIC.getName());
        sb.append(System.lineSeparator());
        sb.append("gatedIps=" + (topicMetadata.getGatedIps() == null ? "" : topicMetadata.getGatedIps()));
        sb.append(System.lineSeparator());
        if (topicMetadata.getGatedCluster() != null && StringUtils.isNotBlank(topicMetadata.getGatedCluster().getClusterName())) {
            sb.append("gatedCluster=" + (topicMetadata.getGatedCluster() == null ? "" : topicMetadata.getGatedCluster().getClusterName()));
        } else {
            sb.append("gatedCluster=");
        }
        sb.append(System.lineSeparator());
        sb.append("isEncrypt=").append(topicMetadata.getIsEncrypt().toString());
        return sb.toString();
    }

    /**
     * @see MmsZookeeper#buildTopicData
     */
    public String buildConsumerGroupData(ConsumerGroupMetadata cgMetadata) {
        StringBuilder sb = new StringBuilder();
        sb.append("clusterName=" + cgMetadata.getClusterMetadata().getClusterName());
        sb.append(System.lineSeparator());
        sb.append("type=" + MmsType.CONSUMER_GROUP.getName());
        sb.append(System.lineSeparator());
        sb.append("bindingTopic=" + cgMetadata.getBindingTopic());
        sb.append(System.lineSeparator());
        sb.append("broadcast=" + cgMetadata.getBroadcast());
        sb.append(System.lineSeparator());
        sb.append("consumeFrom=" + cgMetadata.getConsumeFrom());
        sb.append(System.lineSeparator());
        sb.append("suspend=" + cgMetadata.getSuspend());
        sb.append(System.lineSeparator());
        sb.append("gatedIps=" + (cgMetadata.getGatedIps() == null ? "" : cgMetadata.getGatedIps()));
        sb.append(System.lineSeparator());
        if (cgMetadata.getGatedCluster() != null && StringUtils.isNotBlank(cgMetadata.getGatedCluster().getClusterName())) {
            sb.append("gatedCluster=" + (cgMetadata.getGatedCluster() == null ? "" : cgMetadata.getGatedCluster().getClusterName()));
        } else {
            sb.append("gatedCluster=");
        }
        sb.append(System.lineSeparator());
        sb.append("releaseStatus=" + (cgMetadata.getReleaseStatus() == null ? "" : cgMetadata.getReleaseStatus()));
        return sb.toString();
    }

    /**
     * @see MmsZookeeper#buildTopicData
     */
    public String buildClusterData(ClusterMetadata clusterMetadata) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("bootAddr=" + clusterMetadata.getBootAddr());
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append("brokerType=" + clusterMetadata.getBrokerType().getName());
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append("serverIps=" + clusterMetadata.getServerIps());
        return stringBuilder.toString();
    }

    /**
     * cluster数据写到zk
     */
    public void writeClusterMetadata(ClusterMetadata clusterMetadata) {
        String zkMeta = this.buildClusterData(clusterMetadata);
        this.writeClusterMetadata(clusterMetadata.getClusterName(), zkMeta);
    }

    /**
     * topic数据写到zk
     */
    public void writeTopicMetadata(TopicMetadata topicMetadata) {
        String topicZkData = this.buildTopicData(topicMetadata);
        this.writeTopicMetadata(topicMetadata.getName(), topicZkData);
    }

    /**
     * consumer group数据写到zk
     */
    public void writeConsumerGroupMetadata(ConsumerGroupMetadata cgMetadata) {
        String cgZkData = this.buildConsumerGroupData(cgMetadata);
        this.writeConsumerGroupMetadata(cgMetadata.getName(), cgZkData);
    }
}
