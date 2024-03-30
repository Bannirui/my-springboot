package com.github.bannirui.msb.sample;

import com.github.bannirui.msb.common.annotation.EnableMyFramework;
import org.springframework.boot.SpringApplication;

@EnableMyFramework
public class App01 {

    public static void main(String[] args) {
        SpringApplication.run(App01.class, args);
        System.out.println("App01");
    }
}
