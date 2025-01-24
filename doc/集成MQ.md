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
mq.nameServerAddress
```

### 5 业务Apollo配置

```properties
mq.consumerGroup
mq.minThread
mq.maxThread
```

### 6 启动类注解

- @EnableMsbFramework
- @EnableMsbConfig
- @EnableMsbLog

```java
@EnableMsbFramework
@EnableMsbConfig
@EnableMsbLog
public class App04 implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(App04.class);

    public static void main(String[] args) {
        logger.info("App4启动");
        SpringApplication.run(App04.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("应用已经启动 开始执行定制服务");
    }
}
```

### 7 运行参数


### 8 运行结果


