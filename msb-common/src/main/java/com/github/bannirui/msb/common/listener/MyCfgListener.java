package com.github.bannirui.msb.common.listener;

import com.github.bannirui.msb.common.exception.InvalidException;
import java.io.IOException;
import java.util.List;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

/**
 * spring.factories注入到容器中.
 * 读取应用的配置文件 把应用id读出来.
 * 因为时机有前置性要求 因此用spring.factories自动装配.
 */
public class MyCfgListener implements ApplicationListener<ApplicationEvent>, PriorityOrdered {

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationEnvironmentPreparedEvent e) {
            this.readYml2Env(e);
        }
    }

    /**
     * 最高优先级 因为其他模块需要依赖app id.
     */
    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }

    /**
     * yaml配置文件读到Spring Environment中.
     */
    private void readYml2Env(ApplicationEnvironmentPreparedEvent e) {
        String ymlFilePath = "classpath:META-INF/application.yml";
        ConfigurableEnvironment environment = e.getEnvironment();
        ResourceLoader loader = new DefaultResourceLoader();
        YamlPropertySourceLoader ymlLoader = new YamlPropertySourceLoader();
        try {
            List<PropertySource<?>> sourceList = ymlLoader.load(ymlFilePath, loader.getResource(ymlFilePath));
            boolean containsAppId = false;
            for (PropertySource<?> propertySource : sourceList) {
                boolean b = propertySource.containsProperty("app.id");
                if (b) {
                    containsAppId = true;
                }
                environment.getPropertySources().addLast(propertySource);
            }
            if (!containsAppId) {
                throw new InvalidException("classpath:META-INF/application.yml文件不存在app.id配置");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
