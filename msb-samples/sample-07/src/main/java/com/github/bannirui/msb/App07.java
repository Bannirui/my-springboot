package com.github.bannirui.msb;

import com.github.bannirui.msb.annotation.EnableMsbFramework;
import com.github.bannirui.msb.dal.User;
import com.github.bannirui.msb.dal.UserDao;
import com.github.bannirui.msb.orm.annotation.EnableMyBatis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;

@EnableMsbFramework
@EnableMyBatis
public class App07 implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(App07.class);

    @Autowired
    UserDao userDao;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("App07 started");
        User ans = this.userDao.selectByPrimaryKey(1L);
        log.info("db query ret is {}", ans);
    }

    public static void main(String[] args) {
        SpringApplication.run(App07.class, args);
    }
}
