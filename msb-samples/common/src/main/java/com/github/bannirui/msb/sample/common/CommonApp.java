package com.github.bannirui.msb.sample.common;

import com.github.bannirui.msb.common.annotation.EnableFramework;
import org.springframework.boot.SpringApplication;

/**
 * 启用框架.
 */
@EnableFramework
public class CommonApp {

    public static void main(String[] args) {
        SpringApplication.run(CommonApp.class, args);
        System.out.println("CommonApp Start");
    }
}
