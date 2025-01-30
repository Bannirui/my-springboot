集成MQ
---
### 1 maven的依赖GAV

```xml
<parent>
    <groupId>com.github.bannirui</groupId>
    <artifactId>my-springboot</artifactId>
    <version>3.2.4</version>
</parent>
```

### 3 配置文件

classpath:META-INF/app.properties

```properties
app.id=SampleApp
```

### 4 msb Apollo配置

```properties
# mq server, split by comma, zk addr
mms.nameServerAddress = 127.0.0.1:2181
```

### 5 业务Apollo配置

```properties
# the option for console log, true enable and false disable, the default is enabling
console.log = true

# determines the log level
logging.level.root = info

```

### 6 启动类注解

- @EnableMsbFramework
- @EnableMsbConfig
- @EnableMsbLog
- @EnableMsbMQ

```java
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
        this.registerConsumerGroup();
        // this.cleanZk();
        // mq发送消息
        // this.mmsTemplate.send("a", "1", 1);
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
    private void registerConsumerGroup() {
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
```

### 7 运行参数
```shell
-Denv=dev -Dconsole.log=true -Dapollo.configService=http://127.0.0.1:8081/ -Dapollo.autoUpdateInjectedSpringProperties=true -DCAT_HOME=/Users/dingrui/MyDev/code/java/cat/docker
```

### 8 运行结果

![](../img/1738248371.png)
![](../img/1738248465.png)
