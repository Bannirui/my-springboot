package com.dianping.cat.message.io;

import com.dianping.cat.message.spi.MessageQueue;
import com.dianping.cat.message.spi.MessageTree;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultMessageQueue implements MessageQueue {
    private BlockingQueue<MessageTree> m_queue;
    private AtomicInteger m_count = new AtomicInteger();

    public DefaultMessageQueue(int size) {
        this.m_queue = new LinkedBlockingQueue<>(size);
    }

    @Override
    public boolean offer(MessageTree tree) {
        return this.m_queue.offer(tree);
    }

    public boolean offer(MessageTree tree, double sampleRatio) {
        if (tree.isSample() && sampleRatio < 1.0D) {
            if (sampleRatio > 0.0D) {
                int count = this.m_count.incrementAndGet();
                if ((double) count % (1.0D / sampleRatio) == 0.0D) {
                    return this.offer(tree);
                }
            }
            return false;
        } else {
            return this.offer(tree);
        }
    }

    @Override
    public MessageTree peek() {
        return this.m_queue.peek();
    }

    public MessageTree poll() {
        try {
            return (MessageTree) this.m_queue.poll(5L, TimeUnit.MILLISECONDS);
        } catch (InterruptedException var2) {
            return null;
        }
    }

    public int size() {
        return this.m_queue.size();
    }
}
