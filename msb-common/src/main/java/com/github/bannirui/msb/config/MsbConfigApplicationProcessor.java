package com.github.bannirui.msb.config;

import com.github.bannirui.msb.constant.AppEventListenerSort;
import com.github.bannirui.msb.env.MsbEnvironmentMgr;
import com.github.bannirui.msb.startup.MsbBannerProcessor;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;

/**
 * spring.factories注入到容器中.
 * 作为框架技术组件整合的统一入口 监听SpringBoot的生命周期事件 在需要的节点进行自定义操作
 * <ul>
 *     <li>ApplicationStartingEvent 开始启动中</li>
 *     <li>ApplicationEnvironmentPreparedEvent 环境已经准备好 Spring抽象的Environment准备好</li>
 *     <li>ApplicationContextInitializedEvent 上下文已实例化 在对单例Bean实例化前</li>
 *     <li>ApplicationPreparedEvent 上下文已经准备好</li>
 *     <li>ApplicationStartedEvent 应用启动成功 Bean还没开始实例化</li>
 *     <li>ApplicationReadyEvent 应用已准备好 几乎等同于上面一个事件</li>
 *     <li>ApplicationFailedEvent Spring启动失败</li>
 * </ul>
 * 对接远程配置中心 要将配置中心的数据缓存到Spring中 即Environment中
 * 所以SpringBoot生命周期锚点是在Environment构建好{@link ApplicationEnvironmentPreparedEvent}
 * <p>
 */
public class MsbConfigApplicationProcessor implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        MsbEnvironmentMgr.addMsbConfig2PropertySource(event.getEnvironment());
    }

    @Override
    public int getOrder() {
        /**
         * 等{@link MsbBannerProcessor}先执行.
         */
        return AppEventListenerSort.MSB_APP_SPRING_ENVIRONMENT_CONFIG;
    }
}
