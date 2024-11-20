package com.github.bannirui.msb.sample;

import com.github.bannirui.msb.common.annotation.EnableMsbFramework;
import com.github.bannirui.msb.config.annotation.EnableMsbConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;

@EnableMsbFramework
@EnableMsbConfig
public class App02 implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(App02.class);

    @Value("${my-env}")
    private String env;


    public static void main(String[] args) {
        SpringApplication.run(App02.class, args);
        log.info("App2启动成功");
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("测试apollo env={}", this.env);
    }
}
