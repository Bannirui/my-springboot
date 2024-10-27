package com.github.bannirui.msb.common.startup;

import com.github.bannirui.msb.common.env.EnvironmentMgr;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.PriorityOrdered;

/**
 * 自定义启动控制台Banner.
 */
public class MsbBannerProcessor implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, PriorityOrdered {

    private static final AtomicBoolean FLAG = new AtomicBoolean(false);
    private static final Logger logger = LoggerFactory.getLogger(MsbBannerProcessor.class);
    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final int ORDER = -2147483627;

    public MsbBannerProcessor() {
    }

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        logger.error("开始");
        if (FLAG.compareAndSet(false, true)) {
            // disable printing Spring's banner
            event.getSpringApplication().setBannerMode(Banner.Mode.CONSOLE);
            logger.info(buildBannerText());
        }
        if (EnvironmentMgr.getNetEnv() != null) {
            logger.info("Network environment is set to [{}]", EnvironmentMgr.getNetEnv());
        }
    }

    @Override
    public int getOrder() {
        return ORDER;
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
