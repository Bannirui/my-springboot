package com.github.bannirui.msb.sample.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MyComponent {

    private String name;

    @Value("${name}")
    public void setter1(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @Value("${age}")
    public MyBean setter2(Integer age) {
        return new MyBean();
    }
}
