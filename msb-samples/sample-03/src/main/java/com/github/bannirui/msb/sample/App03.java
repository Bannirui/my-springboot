package com.github.bannirui.msb.sample;

import com.github.bannirui.msb.common.annotation.EnableMSBFramework;
import com.github.bannirui.msb.remotecfg.annotation.EnableMyRemoteCfg;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

@EnableMSBFramework
@EnableMyRemoteCfg(dataId = {"sample-03"}, hotReplace = true)
public class App03 implements CommandLineRunner {

    @Autowired
    private ApplicationContext context;

    @Value("${name}")
    private String name;

    @Value("${age}")
    private Integer age;

    @Value("${sex}")
    private Long sex;

    @Value("${id}")
    private List<Integer> id;

    public static void main(String[] args) {
        SpringApplication.run(App03.class, args);
        System.out.println("App3启动");
    }

    @Override
    public void run(String... args) throws Exception {
        Executors.newSingleThreadScheduledExecutor()
            .scheduleAtFixedRate(() -> {
                System.out.println("name=" + App03.this.name);
                System.out.println("age=" + App03.this.age);
                System.out.println("sex=" + App03.this.sex);
                System.out.println("id=" + App03.this.id);
                System.out.println();
            }, 2_000L, 5_000L, TimeUnit.MILLISECONDS);
    }
}
