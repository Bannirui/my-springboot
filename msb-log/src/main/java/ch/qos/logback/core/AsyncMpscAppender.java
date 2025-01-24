package ch.qos.logback.core;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.github.bannirui.msb.log.queue.MpscBlockingQueue4Log;
import java.util.concurrent.ArrayBlockingQueue;
import org.jctools.queues.MessagePassingQueue;

public class AsyncMpscAppender extends AsyncAppenderBase<ILoggingEvent> {

    boolean includeCallerData = false;

    public boolean isIncludeCallerData() {
        return includeCallerData;
    }

    public void setIncludeCallerData(boolean includeCallerData) {
        this.includeCallerData = includeCallerData;
    }

    @Override
    protected boolean isDiscardable(ILoggingEvent eventObject) {
        Level level = eventObject.getLevel();
        return level.toInt() <= 20_000;
    }

    @Override
    protected void preprocess(ILoggingEvent eventObject) {
        eventObject.prepareForDeferredProcessing();
        if (this.includeCallerData) {
            eventObject.getCallerData();
        }
    }

    @Override
    public void start() {
        if (super.isStarted()) {
            return;
        }
        if (super.appenderCount == 0) {
            super.addError("No attached appenders found.");
        } else if (super.queueSize < 1) {
            super.addError("Invalid queue size [" + super.queueSize + "]");
        } else {
            if (super.neverBlock) {
                super.blockingQueue = new MpscBlockingQueue4Log<>(super.queueSize);
            } else {
                super.blockingQueue = new ArrayBlockingQueue<>(super.queueSize);
            }
            if (super.discardingThreshold == -1) {
                super.discardingThreshold = super.queueSize / 3;
            }
            super.addInfo("Setting discardingThreshold to " + super.discardingThreshold);
            super.worker.setDaemon(true);
            super.worker.setName("AsyncAppender-Worker-" + super.getName());
            super.started = true;
            super.worker.start();
        }
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        if (super.neverBlock && super.blockingQueue instanceof MpscBlockingQueue4Log) {
            this.appendIfLessThreshold(eventObject);
        } else {
            super.append(eventObject);
        }
    }

    protected void appendIfLessThreshold(ILoggingEvent event) {
        ((MpscBlockingQueue4Log) super.blockingQueue).offerIfBelowThreshold(event,
            super.queueSize - super.discardingThreshold,
            (sz, logEvent) -> this.isDiscardable(event),
            (MessagePassingQueue.Consumer<ILoggingEvent>) this::preprocess
        );
    }
}
