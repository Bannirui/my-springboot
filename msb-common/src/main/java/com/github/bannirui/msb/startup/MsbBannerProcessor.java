package com.github.bannirui.msb.startup;

import com.github.bannirui.msb.constant.AppEventListenerSort;
import com.github.bannirui.msb.env.MsbEnvironmentMgr;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;

/**
 * 自定义启动控制台Banner.
 * 因为{@link LogBackConfigListener}用的是{@link Ordered} 为了让Spring有序处理 这边也要用{@link Ordered}
 */
public class MsbBannerProcessor implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered {

    private static final AtomicBoolean springboot_banner_set = new AtomicBoolean(false);
    private static final AtomicBoolean msb_banner_set = new AtomicBoolean(false);
    private static final Logger logger = LoggerFactory.getLogger(MsbBannerProcessor.class);
    private static final String LINE_SEPARATOR = System.lineSeparator();

    public MsbBannerProcessor() {
    }

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        if (springboot_banner_set.compareAndSet(false, true)) {
            // disable printing Spring's banner
            event.getSpringApplication().setBannerMode(Banner.Mode.OFF);
        }
        // 自定义Banner
        if (msb_banner_set.compareAndSet(false, true)) {
            logger.info(buildBannerText());
        }
        String msg = null;
        if (Objects.nonNull(msg = MsbEnvironmentMgr.getAppName())) {
            logger.info("Application name is [{}]", msg);
        }
        if (Objects.nonNull(msg = MsbEnvironmentMgr.getEnv())) {
            logger.info("Environment is set to [{}]", msg);
        }
        if (Objects.nonNull(msg = MsbEnvironmentMgr.getNetEnv())) {
            logger.info("Network environment is set to [{}]", msg);
        }
    }

    @Override
    public int getOrder() {
        /**
         * 要用到log进行输出Banner 依赖{@link LogBackConfigListener}先执行初始化好日志环境.
         */
        return AppEventListenerSort.MSB_BANNER;
    }

    private String buildBannerText() {
        StringBuilder sb = new StringBuilder();
        sb.append(LINE_SEPARATOR);
        sb.append("  __  __            ____             _                   ____              _   \n");
        sb.append(" |  \\/  |_   _     / ___| _ __  _ __(_)_ __   __ _      | __ )  ___   ___ | |_ \n");
        sb.append(" | |\\/| | | | |____\\___ \\| '_ \\| '__| | '_ \\ / _` |_____|  _ \\ / _ \\ / _ \\| __|\n");
        sb.append(" | |  | | |_| |_____|__) | |_) | |  | | | | | (_| |_____| |_) | (_) | (_) | |_ \n");
        sb.append(" |_|  |_|\\__, |    |____/| .__/|_|  |_|_| |_|\\__, |     |____/ \\___/ \\___/ \\__|\n");
        sb.append("         |___/           |_|                 |___/                             ");
        return sb.toString();
    }
}
