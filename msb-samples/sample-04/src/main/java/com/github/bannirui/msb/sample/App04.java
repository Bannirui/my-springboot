package com.github.bannirui.msb.sample;

import com.github.bannirui.msb.common.annotation.EnableMsbFramework;
import com.github.bannirui.msb.config.annotation.EnableMsbConfig;
import com.github.bannirui.msb.log.annotation.EnableMsbLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;

@EnableMsbFramework
@EnableMsbConfig(value = {"application"})
@EnableMsbLog
public class App04 implements ApplicationRunner {
    private static Logger logger = LoggerFactory.getLogger(App04.class);

    public static void main(String[] args) {
        logger.info("App4启动");
        SpringApplication.run(App04.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("应用已经启动 开始执行定制服务");
    }
}
