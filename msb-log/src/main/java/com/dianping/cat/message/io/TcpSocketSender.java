package com.dianping.cat.message.io;

import com.dianping.cat.configuration.ClientConfigManager;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.internal.MessageIdFactory;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageQueue;
import com.dianping.cat.message.spi.MessageStatistics;
import com.dianping.cat.message.spi.MessageTree;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.helper.Threads;
import org.unidal.lookup.annotation.Inject;

public class TcpSocketSender implements Threads.Task, MessageSender, LogEnabled {
    public static final String ID = "tcp-socket-sender";
    public static final int SIZE = 5000;
    @Inject
    private MessageCodec m_codec;
    @Inject
    private MessageStatistics m_statistics;
    @Inject
    private ClientConfigManager m_configManager;
    @Inject
    private MessageIdFactory m_factory;
    private MessageQueue m_queue = new DefaultMessageQueue(5_000);
    private MessageQueue m_atomicTrees = new DefaultMessageQueue(5_000);
    private List<InetSocketAddress> m_serverAddresses;
    private ChannelManager m_manager;
    private Logger m_logger;
    private transient boolean m_active;
    private AtomicInteger m_errors = new AtomicInteger();
    private AtomicInteger m_attempts = new AtomicInteger();
    private static final int MAX_CHILD_NUMBER = 200;
    private static final long HOUR = 3600000L;

    private boolean checkWritable(ChannelFuture future) {
        boolean isWriteable = false;
        Channel channel = future.channel();
        if (future != null && channel.isOpen()) {
            if (channel.isActive() && channel.isWritable()) {
                isWriteable = true;
            } else {
                int count = this.m_attempts.incrementAndGet();
                if (count % 1_000 == 0 || count == 1) {
                    this.m_logger.error("Netty write buffer is full! Attempts: " + count);
                }
            }
        }
        return isWriteable;
    }

    @Override
    public void enableLogging(Logger logger) {
        this.m_logger = logger;
    }

    @Override
    public String getName() {
        return "TcpSocketSender";
    }

    public void initialize() {
        this.m_manager = new ChannelManager(this.m_logger, this.m_serverAddresses, this.m_queue, this.m_configManager, this.m_factory);
        Threads.forGroup("cat").start(this);
        Threads.forGroup("cat").start(this.m_manager);
        Threads.forGroup("cat").start(new TcpSocketSender.MergeAtomicTask());
    }

    private boolean isAtomicMessage(MessageTree tree) {
        Message message = tree.getMessage();
        if (message instanceof Transaction) {
            String type = message.getType();
            return type.startsWith("Cache.") || "SQL".equals(type);
        } else {
            return true;
        }
    }

    private void logQueueFullInfo(MessageTree tree) {
        if (this.m_statistics != null) {
            this.m_statistics.onOverflowed(tree);
        }
        int count = this.m_errors.incrementAndGet();
        if (count % 1000 == 0 || count == 1) {
            this.m_logger.error("Message queue is full in tcp socket sender! Count: " + count);
        }
        tree = null;
    }

    private MessageTree mergeTree(MessageQueue trees) {
        int max = 200;
        DefaultTransaction tran = new DefaultTransaction("_CatMergeTree", "_CatMergeTree", null);
        MessageTree first = trees.poll();
        tran.setStatus("0");
        tran.setCompleted(true);
        tran.addChild(first.getMessage());
        tran.setTimestamp(first.getMessage().getTimestamp());
        long lastTimestamp = 0L;
        for (long lastDuration = 0L; max >= 0; --max) {
            MessageTree tree = trees.poll();
            if (tree == null) {
                tran.setDurationInMillis(lastTimestamp - tran.getTimestamp() + lastDuration);
                break;
            }
            lastTimestamp = tree.getMessage().getTimestamp();
            if (tree.getMessage() instanceof DefaultTransaction) {
                lastDuration = ((DefaultTransaction) tree.getMessage()).getDurationInMillis();
            } else {
                lastDuration = 0L;
            }
            tran.addChild(tree.getMessage());
            this.m_factory.reuse(tree.getMessageId());
        }
        first.setMessage(tran);
        return first;
    }

    @Override
    public void run() {
        this.m_active = true;
        while (true) {
            while (this.m_active) {
                ChannelFuture channel = this.m_manager.channel();
                if (channel != null && this.checkWritable(channel)) {
                    try {
                        MessageTree tree = this.m_queue.poll();
                        if (tree != null) {
                            this.sendInternal(tree);
                            tree.setMessage((Message) null);
                        }
                    } catch (Throwable var8) {
                        this.m_logger.error("Error when sending message over TCP socket!", var8);
                    }
                } else {
                    long current = System.currentTimeMillis();
                    long oldTimestamp = current - 3600000L;
                    while (true) {
                        try {
                            MessageTree tree = this.m_queue.peek();
                            if (tree == null || tree.getMessage().getTimestamp() >= oldTimestamp) {
                                break;
                            }
                            MessageTree discradTree = this.m_queue.poll();
                            if (discradTree != null) {
                                this.m_statistics.onOverflowed(discradTree);
                            }
                        } catch (Exception var10) {
                            this.m_logger.error(var10.getMessage(), var10);
                            break;
                        }
                    }
                    try {
                        Thread.sleep(5L);
                    } catch (Exception var9) {
                        this.m_active = false;
                    }
                }
            }
            return;
        }
    }

    @Override
    public void send(MessageTree tree) {
        boolean result;
        if (this.isAtomicMessage(tree)) {
            result = this.m_atomicTrees.offer(tree, this.m_manager.getSample());
            if (!result) {
                this.logQueueFullInfo(tree);
            }
        } else {
            result = this.m_queue.offer(tree, this.m_manager.getSample());
            if (!result) {
                this.logQueueFullInfo(tree);
            }
        }
    }

    private void sendInternal(MessageTree tree) {
        ChannelFuture future = this.m_manager.channel();
        ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer(10240);
        this.m_codec.encode(tree, buf);
        int size = buf.readableBytes();
        Channel channel = future.channel();
        channel.writeAndFlush(buf);
        if (this.m_statistics != null) {
            this.m_statistics.onBytes(size);
        }
    }

    public void setServerAddresses(List<InetSocketAddress> serverAddresses) {
        this.m_serverAddresses = serverAddresses;
    }

    private boolean shouldMerge(MessageQueue trees) {
        MessageTree tree = trees.peek();
        if (tree != null) {
            long firstTime = tree.getMessage().getTimestamp();
            int maxDuration = 30000;
            if (System.currentTimeMillis() - firstTime > (long) maxDuration || trees.size() >= 200) {
                return true;
            }
        }
        return false;
    }

    public void shutdown() {
        this.m_active = false;
        this.m_manager.shutdown();
    }

    public class MergeAtomicTask implements Threads.Task {
        public MergeAtomicTask() {
        }

        public String getName() {
            return "merge-atomic-task";
        }

        public void run() {
            while (true) {
                if (TcpSocketSender.this.shouldMerge(TcpSocketSender.this.m_atomicTrees)) {
                    MessageTree tree = TcpSocketSender.this.mergeTree(TcpSocketSender.this.m_atomicTrees);
                    boolean result = TcpSocketSender.this.m_queue.offer(tree);
                    if (!result) {
                        TcpSocketSender.this.logQueueFullInfo(tree);
                    }
                } else {
                    try {
                        Thread.sleep(5L);
                    } catch (InterruptedException var3) {
                        return;
                    }
                }
            }
        }

        public void shutdown() {
        }
    }
}
