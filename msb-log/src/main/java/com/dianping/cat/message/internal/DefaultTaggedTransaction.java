package com.dianping.cat.message.internal;

import com.dianping.cat.message.TaggedTransaction;
import com.dianping.cat.message.spi.MessageManager;
import com.dianping.cat.message.spi.MessageTree;

public class DefaultTaggedTransaction extends DefaultTransaction implements TaggedTransaction {
    private String m_rootMessageId;
    private String m_parentMessageId;
    private String m_tag;

    public DefaultTaggedTransaction(String type, String name, String tag, MessageManager manager) {
        super(type, name, manager);
        this.m_tag = tag;
        this.setStandalone(false);
        MessageTree tree = manager.getThreadLocalMessageTree();
        if (tree != null) {
            this.m_rootMessageId = tree.getRootMessageId();
            this.m_parentMessageId = tree.getMessageId();
        }
    }

    @Override
    public void bind(String tag, String childMessageId, String title) {
        DefaultEvent event = new DefaultEvent("RemoteCall", "Tagged");
        if (title == null) {
            title = this.getType() + ":" + this.getName();
        }
        event.addData(childMessageId, title);
        event.setTimestamp(this.getTimestamp());
        event.setStatus("0");
        event.setCompleted(true);
        this.addChild(event);
    }

    @Override
    public String getParentMessageId() {
        return this.m_parentMessageId;
    }

    @Override
    public String getRootMessageId() {
        return this.m_rootMessageId;
    }

    @Override
    public String getTag() {
        return this.m_tag;
    }

    @Override
    public void start() {
        MessageTree tree = this.getManager().getThreadLocalMessageTree();
        if (tree != null && tree.getRootMessageId() == null) {
            tree.setParentMessageId(this.m_parentMessageId);
            tree.setRootMessageId(this.m_rootMessageId);
        }
    }
}
