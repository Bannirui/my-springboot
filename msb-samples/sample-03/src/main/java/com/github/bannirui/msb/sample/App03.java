package com.github.bannirui.msb.sample;

import com.github.bannirui.msb.annotation.EnableMsbFramework;
import com.github.bannirui.msb.config.annotation.EnableMsbConfig;
import com.github.bannirui.msb.sample.component.MyComponent;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;

@EnableMsbFramework
@EnableMsbConfig(value = {"application", "TEST1.mysql"})
public class App03 implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(App03.class);

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
        logger.info("App3启动");
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("name={}",this.name);
        logger.info("age={}", this.age);
        logger.info("male={}", this.male);
        logger.info("ids={}", this.ids);
        logger.info("mysqlPort={}", this.mysqlPort);

        logger.info("setter inject, name={}", this.myComponent.getName());
    }
}
