package com.github.bannirui.msb;

import com.github.bannirui.Echo;
import com.github.bannirui.msb.annotation.EnableMsbFramework;
import com.github.bannirui.msb.dubbo.annotation.EnableMsbDubbo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;

@EnableMsbFramework
@EnableMsbDubbo
public class App08 implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(App08.class);

    @Autowired
    Echo echo;

    public static void main(String[] args) {
        SpringApplication.run(App08.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("spring启动成功");
        logger.info("spring invoke api ret: {}", echo.echo("this is from spring app"));
    }
}
