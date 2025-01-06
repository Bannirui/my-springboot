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
        try {
            boolean isTraceMode = Cat.getManager().isTraceMode();
            Level level = e.getLevel();
            if (level.isGreaterOrEqual(Level.ERROR)) {
                this.logError(e);
            } else if (isTraceMode) {
                this.logTrace(e);
            }
            this.logTrace(e);
        } catch (Exception var4) {
            throw new LogbackException(e.getFormattedMessage(), var4);
        }
    }

    private void logError(ILoggingEvent event) {
        ThrowableProxy info = (ThrowableProxy) event.getThrowableProxy();
        if (info != null) {
            Throwable exception = info.getThrowable();
            if (exception != null && exception.getMessage() != null &&
                (exception.getMessage().indexOf("ORA-00001") >= 0 || exception.getMessage().indexOf("Duplicate entry") >= 0 ||
                    exception.getMessage().indexOf("DuplicateKeyException") >= 0)) {
                return;
            }
            if (exception != null && exception instanceof InvocationTargetException ex) {
                Throwable e = ex.getTargetException();
                if (e != null && e.getMessage() != null &&
                    (e.getMessage().indexOf("ORA-00001") >= 0 || e.getMessage().indexOf("Duplicate entry") >= 0 ||
                        e.getMessage().indexOf("DuplicateKeyException") >= 0)) {
                    return;
                }
            }
            if (BusinessException.isBusinessException(exception)) {
                return;
            }
            String message = event.getFormattedMessage();
            if (message != null && message.indexOf("ORA-00001") < 0 && message.indexOf("Duplicate entry") < 0 &&
                message.indexOf("DuplicateKeyException") < 0) {
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
