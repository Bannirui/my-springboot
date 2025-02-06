package com.github.bannirui.msb.sample;

import com.github.bannirui.msb.annotation.EnableMsbFramework;
import com.github.bannirui.msb.config.annotation.EnableMsbConfig;
import com.github.bannirui.msb.log.annotation.EnableMsbLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;

@EnableMsbFramework
@EnableMsbConfig
@EnableMsbLog
public class App04 implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(App04.class);

    public static void main(String[] args) {
        logger.info("App4启动 pre");
        SpringApplication.run(App04.class, args);
        logger.info("App4启动 post");
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.debug("debug 日志");
        logger.info("info 日志");
        logger.warn("warn 日志");
        logger.error("error 日志");

        while (true) {
            Thread.sleep(2_000);
            logger.info("info级别日志");
            try {
                this.fn();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private void fn() {
        throw new RuntimeException("test");
    }
}
