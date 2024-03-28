package com.github.bannirui.msb.sample.web;

import com.github.bannirui.msb.common.annotation.EnableFramework;
import org.springframework.boot.SpringApplication;

/**
 * 启用框架.
 */
@EnableFramework
public class WebApp {

    public static void main(String[] args) {
        SpringApplication.run(WebApp.class, args);
        System.out.println("WebApp Hello World");
    }
}
