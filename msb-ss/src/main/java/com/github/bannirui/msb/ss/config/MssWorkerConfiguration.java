package com.github.bannirui.msb.ss.config;

import com.github.bannirui.msb.env.MsbEnvironmentMgr;
import com.github.bannirui.msb.ex.FrameworkException;
import com.github.bannirui.msb.register.AbstractBeanRegistrar;
import com.github.bannirui.msb.register.BeanDefinition;
import com.github.bannirui.msb.ss.MssFactory;
import com.github.bannirui.mss.worker.common.MssWorkerConfig;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.core.io.ClassPathResource;

/**
 * 配置my-schedule-service
 */
public class MssWorkerConfiguration extends AbstractBeanRegistrar {

    private static final Logger logger = LoggerFactory.getLogger(MssWorkerConfiguration.class);

    @Override
    public void registerBeans() {
        List<String> serverAddrs = new ArrayList<>();
        int index = 0;
        while(true) {
            String server = super.getProperty(String.format("mss.worker.config.server-address[%s]", index));
            if (StringUtils.isEmpty(server)) {
                // 尽可能找到mss server地址
                if (serverAddrs.isEmpty()) {
                    serverAddrs.addAll(this.loadLocalServerAddr());
                }
                String appCode = super.getProperty("mss.worker.config.app-code");
                if (StringUtils.isEmpty(appCode)) {
                    appCode = MsbEnvironmentMgr.getAppName();
                }
                if (StringUtils.isEmpty(appCode)) {
                    throw new FrameworkException("500", "appCode不能为空 请检查META-INF/app.properties文件是否配置app.id");
                }
                MssWorkerConfiguration.logger.info("zss worker {}, zss schedule center address {}", appCode, serverAddrs);
                int heartTime = 60;
                try {
                    String heartTimeStr = super.getProperty("zss.worker.config.heart-time");
                    if (StringUtils.isNotEmpty(heartTimeStr) && StringUtils.isNumeric(heartTimeStr)) {
                        heartTime = Integer.parseInt(heartTimeStr);
                    }
                } catch (NumberFormatException e) {
                    throw new FrameworkException("500", "heartTime解析错误 请检查");
                }
                this.registerBeanDefinitionIfNotExists(
                    BeanDefinition.newInstance(MssFactory.class)
                        .addPropertyValue("applicationContext", this.applicationContext)
                        .addPropertyValue("workerConfig", new MssWorkerConfig(appCode, serverAddrs, heartTime))
                        // 初始化方法
                        .setInitMethodName("init"));
                return;
            }
            serverAddrs.add(server);
            ++index;
        }
    }

    /**
     * 尝试读取本地配置mss-xxx.properties
     * @return 在本地读到的定时任务服务地址
     */
    private List<String> loadLocalServerAddr() {
        Properties prop = new Properties();
        ClassPathResource resource = new ClassPathResource("mss-" + MsbEnvironmentMgr.getEnv().toLowerCase() + ".properties");
        logger.info("start load mss-{}.properties file", MsbEnvironmentMgr.getEnv().toLowerCase());
        try {
            InputStream inputStream = resource.getInputStream();
            try {
                prop.load(inputStream);
            } catch (Throwable e) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Throwable var7) {
                        e.addSuppressed(var7);
                    }
                }
                throw e;
            }
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            throw new BeanCreationException("Can not load properties file", e);
        }
        // 本地没有mss-xxx.properties
        if (prop.isEmpty()) return new ArrayList<>();
        // 尝试配置文件中配置项
        List<String> ret = new ArrayList<>();
        int index = 0;
        while(true) {
            String addr = prop.getProperty(String.format("mss.worker.config.server-address[%s]", index));
            // msb没有配置本地配置项
            if (StringUtils.isEmpty(addr)) return ret;
            ret.add(addr);
            ++index;
        }
    }
}
