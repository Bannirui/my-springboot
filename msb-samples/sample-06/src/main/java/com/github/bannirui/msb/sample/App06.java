package com.github.bannirui.msb.sample;

import com.github.bannirui.msb.common.annotation.EnableMsbFramework;
import com.github.bannirui.msb.config.annotation.EnableMsbConfig;
import com.github.bannirui.msb.log.annotation.EnableMsbLog;
import com.github.bannirui.msb.mq.annotation.EnableMsbMQ;
import com.github.bannirui.msb.mq.annotation.MMSListener;
import com.github.bannirui.msb.mq.annotation.MMSListenerParameter;
import com.github.bannirui.msb.mq.configuration.MMSTemplate;
import com.github.bannirui.msb.mq.enums.MMSResult;
import com.github.bannirui.msb.mq.enums.MQMsgEnum;
import com.github.bannirui.msb.mq.sdk.common.BrokerType;
import com.github.bannirui.msb.mq.sdk.common.MmsType;
import com.github.bannirui.msb.mq.sdk.consumer.MsgConsumedStatus;
import com.github.bannirui.msb.mq.sdk.metadata.ClusterMetadata;
import com.github.bannirui.msb.mq.sdk.metadata.ConsumerGroupMetadata;
import com.github.bannirui.msb.mq.sdk.metadata.TopicMetadata;
import com.github.bannirui.msb.mq.sdk.zookeeper.RouterManager;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;

@EnableMsbFramework
@EnableMsbConfig
@EnableMsbLog
@EnableMsbMQ
public class App06 implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(App06.class);
    @Autowired
    MMSTemplate mmsTemplate;

    public static void main(String[] args) {
        SpringApplication.run(App06.class, args);
    }

    @MMSListener(consumerGroup = "group_a")
    public MMSResult listen(
                                 @MMSListenerParameter(name = MQMsgEnum.TAG) String tag,
                                 @MMSListenerParameter(name = MQMsgEnum.BODY) String body,
                                 @MMSListenerParameter(name = MQMsgEnum.RECONSUME_TIMES) String reconsumeTimes
    ) {
        log.info("业务收到MQ消息 tag={} msg={} retry_times={}", tag, body, reconsumeTimes);
        return MMSResult.status(MsgConsumedStatus.SUCCEED);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
         // cluster and consumer
        // this.initZk();
        this.registerMQMetaDataIfAbsent();
        // this.cleanZk();
        // while(true) {
            Thread.sleep(1500L);
            // mq发送消息
        String mid = this.mmsTemplate.send("topic_a", "1", "hello.");
        log.info("业务发送MQ消息成功 mid={}", mid);
        // }
    }
    private void initZk() {
        try {
            RouterManager.getZkInstance().create("/mms/cluster", null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            RouterManager.getZkInstance().create("/mms/topic", null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            RouterManager.getZkInstance().create("/mms/consumergroup", null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch (KeeperException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    private void cleanZk() {
        RouterManager.getInstance().deleteTopic("topic_a");
        RouterManager.getInstance().deleteConsumerGroup("group_a");
        RouterManager.getInstance().deleteCluster("cluster_a");
    }
    private void registerMQMetaDataIfAbsent() {
        // cluster
        ClusterMetadata cluster = new ClusterMetadata();
        cluster.setClusterName("DefaultCluster");
        cluster.setBootAddr("127.0.0.1:9876");
        cluster.setBrokerType(BrokerType.ROCKETMQ);
        cluster.setServerIps("127.0.0.1");
        // zk注册mq cluster
        RouterManager.getZkInstance().writeClusterMetadata(cluster);
        log.info("向zk中注册了cluster信息");
        TopicMetadata topic = new TopicMetadata();
        topic.setType(MmsType.TOPIC.getName());
        topic.setName("topic_a");
        topic.setClusterMetadata(cluster);
        topic.setIsEncrypt(false);
        RouterManager.getInstance().writeTopicMetadata(topic);
        log.info("向zk中注册了topic信息");
        // zk注册mq consumer group
        ConsumerGroupMetadata consumer = new ConsumerGroupMetadata();
        consumer.setType(MmsType.CONSUMER_GROUP.getName());
        consumer.setName("group_a");
        consumer.setClusterMetadata(cluster);
        consumer.setBindingTopic("topic_a");
        RouterManager.getInstance().writeConsumerGroupMetadata(consumer);
        log.info("向zk中注册了consumer信息");
    }
}
