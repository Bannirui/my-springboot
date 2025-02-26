package com.github.bannirui.msb.endpoint;

import com.dianping.cat.status.StatusExtension;
import com.dianping.cat.status.StatusExtensionRegister;
import com.github.bannirui.msb.endpoint.jmx.MonitorForCat;
import java.util.Map;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@AutoConfigureAfter({MonitorComponentAutoConfig.class, EndpointAutoConfiguration.class})
public class CatMonitorAutoConfig implements ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, MonitorForCat> monitorForCatBeans = applicationContext.getBeansOfType(MonitorForCat.class);
        monitorForCatBeans.forEach((catName, monitorForCat) -> {
            StatusExtensionRegister.getInstance().register((StatusExtension) monitorForCat);
        });
    }
}
