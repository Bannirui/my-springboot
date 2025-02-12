package com.github.bannirui.msb;

import com.github.bannirui.msb.annotation.EnableMsbFramework;
import com.github.bannirui.msb.dubbo.annotation.EnableMsbDubbo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;

@EnableMsbFramework
@EnableMsbDubbo
public class App08 implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(App08.class);

    public static void main(String[] args) {
        SpringApplication.run(App08.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("app started");
    }
}
