package com.github.bannirui.msb.sample;

import com.github.bannirui.msb.common.annotation.EnableMsbFramework;
import com.github.bannirui.msb.config.annotation.EnableMsbConfig;
import com.github.bannirui.msb.sample.component.MyComponent;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;

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
