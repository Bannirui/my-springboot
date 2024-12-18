package com.github.bannirui.msb.sample;

import com.github.bannirui.msb.common.annotation.EnableMsbFramework;
import com.github.bannirui.msb.common.properties.bind.PropertyBinder;
import com.github.bannirui.msb.config.annotation.EnableMsbConfig;
import com.github.bannirui.msb.log.annotation.EnableMsbLog;
import java.util.Collections;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

@EnableMsbFramework
@EnableMsbConfig
@EnableMsbLog
public class App05 implements EnvironmentAware,ApplicationRunner {

    private ConfigurableEnvironment env;

    private static final Logger logger = LoggerFactory.getLogger(App05.class);

    public static void main(String[] args) {
        SpringApplication.run(App05.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        PropertyBinder binder = new PropertyBinder(this.env);
        Map<String, String> map = binder.bind("logging", Bindable.mapOf(String.class, String.class)).orElseGet(Collections::emptyMap);
        logger.error("bind get, map={}", map);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.env=(ConfigurableEnvironment) environment;
    }
}
