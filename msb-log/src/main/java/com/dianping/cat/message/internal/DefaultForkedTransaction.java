package com.dianping.cat.message.internal;

import com.dianping.cat.Cat;
import com.dianping.cat.message.ForkedTransaction;
import com.dianping.cat.message.spi.MessageManager;
import com.dianping.cat.message.spi.MessageTree;

public class DefaultForkedTransaction extends DefaultTransaction implements ForkedTransaction {

    private String m_rootMessageId;
    private String m_parentMessageId;
    private String m_forkedMessageId;

    public DefaultForkedTransaction(String type, String name, MessageManager manager) {
        super(type, name, manager);
        this.setStandalone(false);
        MessageTree tree = manager.getThreadLocalMessageTree();
        if (tree != null) {
            this.m_rootMessageId = tree.getRootMessageId();
            this.m_parentMessageId = tree.getMessageId();
            this.m_forkedMessageId = Cat.createMessageId();
        }
    }

    @Override
    public void fork() {
        MessageManager manager = this.getManager();
        manager.setup();
        manager.start(this, false);
        MessageTree tree = manager.getThreadLocalMessageTree();
        if (tree != null) {
            tree.setMessageId(this.m_forkedMessageId);
            tree.setRootMessageId(this.m_rootMessageId == null ? this.m_parentMessageId : this.m_rootMessageId);
            tree.setParentMessageId(this.m_parentMessageId);
        }
    }

    @Override
    public String getForkedMessageId() {
        return this.m_forkedMessageId;
    }
}
