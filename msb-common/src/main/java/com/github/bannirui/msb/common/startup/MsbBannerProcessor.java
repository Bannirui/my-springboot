package com.github.bannirui.msb.common.startup;

import com.github.bannirui.msb.common.env.EnvironmentMgr;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.PriorityOrdered;

/**
 * 自定义启动控制台Banner.
 */
public class MsbBannerProcessor implements ApplicationListener<ApplicationPreparedEvent>, PriorityOrdered {

    private static AtomicBoolean atomicBoolean = new AtomicBoolean(false);
    private static final Logger logger = LoggerFactory.getLogger(MsbBannerProcessor.class);
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    @Override
    public void onApplicationEvent(ApplicationPreparedEvent event) {
        if (atomicBoolean.compareAndSet(false, true)) {
            event.getSpringApplication().setBannerMode(Banner.Mode.OFF);
            String s = buildBannerText();
            logger.info(s);
        }
        if (EnvironmentMgr.getNetEnv() != null) {
            logger.info("Network environment is set to [{}]", EnvironmentMgr.getNetEnv());
        }
    }

    @Override
    public int getOrder() {
        return PriorityOrdered.HIGHEST_PRECEDENCE;
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
