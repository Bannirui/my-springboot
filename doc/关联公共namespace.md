读取远程配置
---

基础设置的配置可能万年不变或者频繁被使用，因此公共的配置诸如mysql/redis/zk等连接信息便可以作为公共namespace被关联到各个项目中。

### 1 Apollo远程配置

![](./../img/1729772923.png)

### 2 maven的依赖GAV

```xml
<parent>
    <groupId>com.github.bannirui</groupId>
    <artifactId>my-springboot</artifactId>
    <version>3.2.4</version>
</parent>
```

### 3 配置文件

classpath:META-INF/app.properties

这个配置文件是Apollo会读取app.id的地方

```properties
app.id=SampleApp
```

### 4 JVM参数

运行的时候指定参数`-Denv=dev`

### 5 启动类上打上注解@EnableMyFramework和注解@EnableMsbConfig

===注解必须打在启动类上===

这个时候需要显式指定要被关联的namespace

```java
@EnableMsbFramework
@EnableMsbConfig(value = {"application", "TEST1.mysql"})
public class App03 implements CommandLineRunner {

    @Value("${name}")
    private String name;

    @Value("${age}")
    private Integer age;

    @Value("${male}")
    private Boolean male;

    @Value("${ids}")
    private List<Long> ids;

    @Value("${port}")
    private Integer mysqlPort;

    @Autowired
    MyComponent myComponent;

    public static void main(String[] args) {
        SpringApplication.run(App03.class, args);
        System.out.println("App3启动");
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("name=" + this.name);
        System.out.println("age=" + this.age);
        System.out.println("male=" + this.male);
        System.out.println("ids=" + this.ids);
        System.out.println("mysqlPort=" + this.mysqlPort);

        System.out.println("setter inject, name=" + this.myComponent.getName());
    }
}
```

### 6 运行结果

![](./../img/1729773195.png)