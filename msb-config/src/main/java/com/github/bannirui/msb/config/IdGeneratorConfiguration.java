package com.github.bannirui.msb.config;

import com.github.bannirui.msb.common.id.EasyGenerator;
import com.github.bannirui.msb.common.register.AbstractBeanRegistrar;
import java.util.Objects;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

public class IdGeneratorConfiguration extends AbstractBeanRegistrar {

    private static final String WORKER_ID_KEY = "msb.worker.id";

    public IdGeneratorConfiguration() {
    }

    @Override
    public void registerBeans() {
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        Integer workerId = this.getPropertyAsInteger(WORKER_ID_KEY);
        if (Objects.nonNull(workerId) && workerId >= 0 && workerId < 1024) {
            EasyGenerator generator = new EasyGenerator(workerId, 600);
            beanFactory.registerSingleton("msb_EasyGenerator", generator);
        } else {
            throw new BeanInitializationException(
                "@EnableIdGenerator加载ID生成器异常workerId: " + workerId + ", 造成异常原因可能是应用进程数量超过1024个");
        }
    }
}
