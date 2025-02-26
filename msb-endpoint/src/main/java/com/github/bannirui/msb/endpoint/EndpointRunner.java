package com.github.bannirui.msb.endpoint;

import com.github.bannirui.msb.endpoint.health.HealthIndicator;
import com.github.bannirui.msb.endpoint.health.HealthIndicatorNameFactory;
import com.github.bannirui.msb.endpoint.info.EnvInfoProvider;
import com.github.bannirui.msb.endpoint.web.EndpointManager;
import com.github.bannirui.msb.endpoint.web.http.NettyHttpService;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

@Order(0)
public class EndpointRunner implements ApplicationRunner, ApplicationContextAware, DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(EndpointRunner.class);
    public static final String PORT_KEY = "msb.endpoint.port";
    public static final String AUTHORIZATION_KEY = "msb.endpoint.authorization";
    private ApplicationContext applicationContext;
    private Integer port = 8166;
    private NettyHttpService httpService;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.applicationContext = context;
        Environment environment = this.applicationContext.getEnvironment();
        this.port = environment.getProperty("msb.endpoint.port", Integer.class, 8166);
        EndpointManager.setAuthorization(environment.getProperty("msb.endpoint.authorization", "msb:msb#666"));
    }

    @Override
    public void run(ApplicationArguments args) {
        Thread thread = new Thread(() -> {
            this.registerHealth();
            this.registerInfoProvider();
            this.httpService = new NettyHttpService(this.port);
            try {
                this.httpService.start();
            } catch (Exception e) {
                logger.error("健康检查Http端口启动失败 端口号{}", this.port, e);
            }
        });
        thread.setName("NettyHttpServiceThread");
        thread.start();
        System.out.println("this app is start");
    }

    @Override
    public void destroy() {
        if (this.httpService != null) {
            this.httpService.close();
        }
    }

    private void registerHealth() {
        HealthIndicatorNameFactory healthIndicatorNameFactory = new HealthIndicatorNameFactory();
        Map<String, HealthIndicator> healthIndicators = this.applicationContext.getBeansOfType(HealthIndicator.class);
        healthIndicators.forEach((k,v)->{
            String name = healthIndicatorNameFactory.apply(k);
            EndpointManager.registerHealth(name, v);
        });
    }

    private void registerInfoProvider() {
        EnvInfoProvider envInfoProvider = new EnvInfoProvider();
        envInfoProvider.setApplicationContext(this.applicationContext);
        EndpointManager.registerInfoProvider(envInfoProvider.id(), envInfoProvider);
    }
}
