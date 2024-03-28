package com.github.bannirui.msb.sample.all;

import com.github.bannirui.msb.common.annotation.EnableFramework;
import org.springframework.boot.SpringApplication;

/**
 * 启用框架.
 */
@EnableFramework
public class AllApp {

    public static void main(String[] args) {
        SpringApplication.run(AllApp.class, args);
        System.out.println("All App 启动");
    }
}
