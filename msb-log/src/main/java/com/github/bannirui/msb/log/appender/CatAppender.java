package com.github.bannirui.msb.log.appender;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.LogbackException;
import com.dianping.cat.Cat;
import com.github.bannirui.msb.log.cat.MsbCat;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;

/**
 * 业务应用作为cat-client接入cat.
 */
public class CatAppender extends AppenderBase<ILoggingEvent> {

    /**
     * <ul>
     *     <li>异常日志作为事件上报给cat</li>
     *     <li>需要链路跟踪的日志也作为事件上报</li>
     * </ul>
     */
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

    /**
     * throwable的日志才上报给cat
     * 不是throwable说明是业务代码开发人员预期到的 不需要框架去关注
     */
    private void logError(ILoggingEvent event) {
        ThrowableProxy throwableInfo = (ThrowableProxy) event.getThrowableProxy();
        if(Objects.isNull(throwableInfo)) return;
        Throwable exception = throwableInfo.getThrowable();
        String message = event.getFormattedMessage();
        if (Objects.nonNull(message)) {
            Cat.logError(message, exception);
        } else {
            Cat.logError(exception);
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
        if(Objects.isNull(exception)) return "";
        StringWriter writer = new StringWriter(2048);
        exception.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }
}
