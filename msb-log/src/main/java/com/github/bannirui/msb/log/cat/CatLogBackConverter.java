package com.github.bannirui.msb.log.cat;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.dianping.cat.Cat;
import com.dianping.cat.message.spi.MessageTree;

/**
 * 为日志自定义trace id.
 */
public class CatLogBackConverter extends ClassicConverter {

    /**
     * 生成日志的traceId
     */
    @Override
    public String convert(ILoggingEvent iLoggingEvent) {
        MessageTree tree = Cat.getManager().getThreadLocalMessageTree();
        if (tree != null) {
            String messageId = tree.getMessageId();
            if (messageId == null) {
                messageId = Cat.createMessageId();
                tree.setMessageId(messageId);
            }
            return String.join(",", messageId, tree.getParentMessageId());
        } else {
            return null;
        }
    }
}
