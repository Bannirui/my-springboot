package com.github.bannirui.msb.log.cat;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class CatLogBackConverter extends ClassicConverter {

    public CatLogBackConverter() {
    }

    /**
     * 生成日志的traceId
     */
    @Override
    public String convert(ILoggingEvent iLoggingEvent) {
        // TODO: 2024/11/15
        return "traceId";
    }
}
