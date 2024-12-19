package com.github.bannirui.msb.common.listener;

import com.github.bannirui.msb.common.env.MsbEnvironmentMgr;
import com.github.bannirui.msb.common.event.ConfigChangeSpringEvent;
import com.github.bannirui.msb.common.listener.param.SpringParamResolver;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

public class ConfigChangeSpringMulticaster implements ApplicationListener<ConfigChangeSpringEvent>, ApplicationContextAware, EnvironmentAware {
    private static final Logger logger = LoggerFactory.getLogger(ConfigChangeSpringMulticaster.class);
    private ApplicationContext applicationContext;
    private final List<SpringParamResolver> springParamResolvers = ParamResolverDetector.getSpringParamResolverList();
    private ConfigurableEnvironment environment;
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private int defaultInvokeTimeoutInSecond = 5;

    public ConfigChangeSpringMulticaster() {
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        this.init();
    }

    @Override
    public void onApplicationEvent(ConfigChangeSpringEvent event) {
        Set<ConfigChangeListenerMetaData> metaDatas =
            ConfigChangeListenerContainer.getCheckedMetaData(event.getConfigChange().getChangedConfigKeys());
        for (ConfigChangeListenerMetaData metaData : metaDatas) {
            logger.info(String.format("ConfigChangeSpringMulticaster invoke Listener[%s] method[%s]", metaData.getObj().getClass().getName(),
                metaData.getMethod().getName()));
            this.multicastEvent(event, metaData, this.applicationContext, this.environment, this.springParamResolvers);
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;
    }

    private void init() throws BeansException {
        String strDefaultInvokeTimeoutInSecond = MsbEnvironmentMgr.getProperty("listener.invoke.timeoutInSecond");
        if (strDefaultInvokeTimeoutInSecond != null) {
            this.defaultInvokeTimeoutInSecond = Integer.parseInt(strDefaultInvokeTimeoutInSecond);
        }
    }

    private void multicastEvent(final ConfigChangeSpringEvent event, final ConfigChangeListenerMetaData metaData,
                                final ApplicationContext applicationContext, final Environment environment,
                                final List<SpringParamResolver> springParamResolvers) {
        this.executorService.submit(() -> {
            Method method = metaData.getMethod();
            Object target = metaData.getObj();
            Object[] params = new Object[method.getParameterCount()];
            String[] paramNames = metaData.getParamNames();
            int i = 0;
            for (int len = method.getParameterCount(); i < len; ++i) {
                Parameter parameter = method.getParameters()[i];
                for (SpringParamResolver paramResolver : springParamResolvers) {
                    if (paramResolver.isSupport(parameter)) {
                        try {
                            params[i] = paramResolver.resolveParameter(parameter, paramNames[i], event.getConfigChange(), event.getConfigChange().getChangedConfigKeys(), environment, applicationContext);
                        } catch (Exception e) {
                            ConfigChangeSpringMulticaster.logger.warn("事件监听类[{}]方法[{}]参数[{}]在执行时出错", target.getClass().getName(), method.getName(), paramNames[i], e);
                        }
                        break;
                    }
                }
            }
            try {
                method.invoke(target, params);
            } catch (InvocationTargetException e) {
                ConfigChangeSpringMulticaster.logger.error("事件监听类[{}]方法[{}]在执行时出错", target.getClass().getName(), method.getName(), e.getTargetException());
            } catch (Exception e) {
                ConfigChangeSpringMulticaster.logger.error("事件监听类[{}]方法[{}]在执行时出错", target.getClass().getName(), method.getName(), e);
            }
            return null;
        });
    }
}
