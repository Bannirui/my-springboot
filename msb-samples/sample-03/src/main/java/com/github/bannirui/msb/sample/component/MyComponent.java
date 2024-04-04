package com.github.bannirui.msb.sample.component;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MyComponent {

    @Value("${sex}")
    public void setter1(Long sex) {

    }

    @Value("${id}")
    public MyBean setter2(List<Integer> id) {
        return new MyBean();
    }
}
