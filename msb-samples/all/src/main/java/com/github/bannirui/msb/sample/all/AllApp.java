package com.github.bannirui.msb.sample.all;

import com.github.bannirui.msb.common.annotation.EnableMyFramework;
import com.github.bannirui.msb.remotecfg.annotation.EnableMyRemoteCfg;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;

/**
 * 启用框架.
 */
@EnableMyFramework
@EnableMyRemoteCfg(dataId = {"my-app-1", "test"})
public class AllApp implements CommandLineRunner {

    @Value("${test1.hello1}")
    private String name1;

    @Value("${test2.hello2}")
    private String name2;

    @Value("${hello3}")
    private String name3;

    @Value("${hello4}")
    private String name4;

    public static void main(String[] args) {
        SpringApplication.run(AllApp.class, args);
        System.out.println("All App 启动");
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("name1=" + this.name1);
        System.out.println("name2=" + this.name2);
        System.out.println("name3=" + this.name3);
        System.out.println("name4=" + this.name4);
    }
}
