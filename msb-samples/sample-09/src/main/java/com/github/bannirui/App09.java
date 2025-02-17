package com.github.bannirui;

import com.github.bannirui.msb.annotation.EnableMsbFramework;
import com.github.bannirui.msb.dubbo.annotation.EnableMsbDubbo;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;

@EnableMsbFramework
@EnableMsbDubbo
public class App09 implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(App09.class);
    @DubboReference
    Echo echo;

    public static void main(String[] args) {
        SpringApplication.run(App09.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String ret = this.echo.echo("hi, this is invoker for dubbo reference");
        logger.info("spring app启动成功");
        logger.info(ret);
    }
}
