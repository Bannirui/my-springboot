package com.github.bannirui.msb.log.appender;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.LogbackException;
import com.dianping.cat.Cat;
import com.github.bannirui.msb.common.ex.BusinessException;
import com.github.bannirui.msb.log.cat.MsbCat;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;

/**
 * 实现日志采集到远程服务器
 * <ul>比如
 *     <li>ELK</li>
 *     <li>CAT链路追踪</li>
 * </ul>
 */
public class CatAppender extends AppenderBase<ILoggingEvent> {

    @Override
    protected void append(ILoggingEvent event) {
        try {
            boolean isTraceMode = Cat.getManager().isTraceMode();
            Level level = event.getLevel();
            if (level.isGreaterOrEqual(Level.ERROR)) {
                this.logError(event);
            } else if (isTraceMode) {
                this.logTrace(event);
            }
        } catch (Exception e) {
            throw new LogbackException(event.getFormattedMessage(), e);
        }
    }

    private void logError(ILoggingEvent event) {
        ThrowableProxy info = (ThrowableProxy) event.getThrowableProxy();
        if (info != null) {
            Throwable exception = info.getThrowable();
            if (exception != null && exception.getMessage() != null
                && (exception.getMessage().contains("ORA-00001") || exception.getMessage().contains("Duplicate entry") || exception.getMessage().contains("DuplicateKeyException"))) {
                return;
            }
            if (exception != null && exception instanceof InvocationTargetException ex) {
                Throwable e = ex.getTargetException();
                if (e != null && e.getMessage() != null
                    && (e.getMessage().contains("ORA-00001") || e.getMessage().contains("Duplicate entry") || e.getMessage().contains("DuplicateKeyException"))) {
                    return;
                }
            }
            if (BusinessException.isBusinessException(exception)) {
                return;
            }
            String message = event.getFormattedMessage();
            if (message != null && !message.contains("ORA-00001") && !message.contains("Duplicate entry")
                && !message.contains("DuplicateKeyException")) {
                Cat.logError(message, exception);
            } else {
                Cat.logError(exception);
            }
        }
    }

    private void logTrace(ILoggingEvent event) {
        String type = "Logback";
        String name = event.getLevel().toString();
        Object message = event.getFormattedMessage();
        String data;
        if (message instanceof Throwable m) {
            data = this.buildExceptionStack(m);
        } else {
            data = event.getFormattedMessage().toString();
        }
        ThrowableProxy info = (ThrowableProxy) event.getThrowableProxy();
        if (info != null) {
            data = data + '\n' + this.buildExceptionStack(info.getThrowable());
        }
        MsbCat.getInstance().logTrace(type, name, "0", data);
    }

    private String buildExceptionStack(Throwable exception) {
        if (exception != null) {
            StringWriter writer = new StringWriter(2048);
            exception.printStackTrace(new PrintWriter(writer));
            return writer.toString();
        } else {
            return "";
        }
    }
}
