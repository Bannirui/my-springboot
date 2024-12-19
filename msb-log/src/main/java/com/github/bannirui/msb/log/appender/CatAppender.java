package com.github.bannirui.msb.log.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 实现日志采集到远程服务器
 * <ul>比如
 *     <li>ELK</li>
 *     <li>CAT链路追踪</li>
 * </ul>
 */
public class CatAppender extends AppenderBase<ILoggingEvent> {

    private static final Logger logger = LoggerFactory.getLogger(CatAppender.class);

    @Override
    protected void append(ILoggingEvent e) {
        // TODO: 2024/11/14
        logger.info("日志锚点...");
    }
}
