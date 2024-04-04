package com.github.bannirui.msb.sample;

import com.github.bannirui.msb.common.annotation.EnableMyFramework;
import com.github.bannirui.msb.remotecfg.annotation.EnableMyRemoteCfg;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

@EnableMyFramework
@EnableMyRemoteCfg(dataId = {"sample-02"}, hotReplace = true)
public class App03 implements CommandLineRunner {

    @Autowired
    private ApplicationContext context;

    @Value("${name}")
    private String name;

    @Value("${age}")
    private Integer age;

    @Value("${sex}")
    private Long sex;

    @Value("${id}")
    private List<Integer> id;

    public static void main(String[] args) {
        SpringApplication.run(App03.class, args);
        System.out.println("App3启动");
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("name=" + this.name);
        System.out.println("age=" + this.age);
        System.out.println("sex=" + this.sex);
        System.out.println("id=" + this.id);
        BeanDefinition beanDefinition = ((BeanDefinitionRegistry) this.context).getBeanDefinition("myComponent");
        System.out.println();
    }
}
