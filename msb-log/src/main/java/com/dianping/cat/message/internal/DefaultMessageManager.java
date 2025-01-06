package com.dianping.cat.message.internal;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ClientConfigManager;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.configuration.client.entity.Domain;
import com.dianping.cat.message.ForkedTransaction;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.TaggedTransaction;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.io.MessageSender;
import com.dianping.cat.message.io.TransportManager;
import com.dianping.cat.message.spi.MessageManager;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;

public class DefaultMessageManager extends ContainerHolder implements MessageManager, Initializable, LogEnabled {

    @Inject
    private ClientConfigManager m_configManager;
    @Inject
    private TransportManager m_transportManager;
    @Inject
    private MessageIdFactory m_factory;
    private ThreadLocal<DefaultMessageManager.Context> m_context = new ThreadLocal<>();
    private long m_throttleTimes;
    private Domain m_domain;
    private String m_hostName;
    private boolean m_firstMessage = true;
    private DefaultMessageManager.TransactionHelper m_validator = new DefaultMessageManager.TransactionHelper();
    private Map<String, TaggedTransaction> m_taggedTransactions;
    private Logger m_logger;

    @Override
    public void add(Message message) {
        DefaultMessageManager.Context ctx = this.getContext();
        if (ctx != null) {
            ctx.add(message);
        }
    }

    @Override
    public void bind(String tag, String title) {
        TaggedTransaction t = this.m_taggedTransactions.get(tag);
        if (t != null) {
            MessageTree tree = this.getThreadLocalMessageTree();
            String messageId = tree.getMessageId();
            if (messageId == null) {
                messageId = this.nextMessageId();
                tree.setMessageId(messageId);
            }
            if (tree != null) {
                t.start();
                t.bind(tag, messageId, title);
            }
        }
    }

    @Override
    public void enableLogging(Logger logger) {
        this.m_logger = logger;
    }

    @Override
    public void end(Transaction transaction) {
        DefaultMessageManager.Context ctx = this.getContext();
        if (ctx != null && transaction.isStandalone() && ctx.end(this, transaction)) {
            this.m_context.remove();
        }
    }

    public void flush(MessageTree tree) {
        if (tree.getMessageId() == null) {
            tree.setMessageId(this.nextMessageId());
        }
        MessageSender sender = this.m_transportManager.getSender();
        if (sender != null && this.isMessageEnabled()) {
            sender.send(tree);
            this.reset();
        } else {
            ++this.m_throttleTimes;
            if (this.m_throttleTimes % 10000L == 0L || this.m_throttleTimes == 1L) {
                this.m_logger.info("Cat Message is throttled! Times:" + this.m_throttleTimes);
            }
        }
    }

    public ClientConfigManager getConfigManager() {
        return this.m_configManager;
    }

    private DefaultMessageManager.Context getContext() {
        if (Cat.isInitialized()) {
            DefaultMessageManager.Context ctx = (DefaultMessageManager.Context) this.m_context.get();
            if (ctx != null) {
                return ctx;
            } else {
                if (this.m_domain != null) {
                    ctx = new DefaultMessageManager.Context(this.m_domain.getId(), this.m_hostName, this.m_domain.getIp());
                } else {
                    ctx = new DefaultMessageManager.Context("Unknown", this.m_hostName, "");
                }
                this.m_context.set(ctx);
                return ctx;
            }
        } else {
            return null;
        }
    }

    @Override
    public String getDomain() {
        return this.m_domain.getId();
    }

    public String getMetricType() {
        return "";
    }

    @Override
    public Transaction getPeekTransaction() {
        DefaultMessageManager.Context ctx = this.getContext();
        return ctx != null ? ctx.peekTransaction(this) : null;
    }

    @Override
    public MessageTree getThreadLocalMessageTree() {
        DefaultMessageManager.Context ctx = (DefaultMessageManager.Context) this.m_context.get();
        if (ctx == null) {
            this.setup();
        }
        ctx = this.m_context.get();
        return ctx.m_tree;
    }

    @Override
    public boolean hasContext() {
        return this.m_context.get() != null;
    }

    @Override
    public void initialize() throws InitializationException {
        this.m_domain = this.m_configManager.getDomain();
        this.m_hostName = NetworkInterfaceManager.INSTANCE.getLocalHostName();
        if (this.m_domain.getIp() == null) {
            this.m_domain.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
        }
        try {
            this.m_factory.initialize(this.m_domain.getId());
        } catch (IOException e) {
            throw new InitializationException("Error while initializing MessageIdFactory!", e);
        }

        final int size = this.m_configManager.getTaggedTransactionCacheSize();
        this.m_taggedTransactions = new LinkedHashMap<String, TaggedTransaction>(size * 4 / 3 + 1, 0.75F, true) {
            private static final long serialVersionUID = 1L;
            protected boolean removeEldestEntry(Map.Entry<String, TaggedTransaction> eldest) {
                return this.size() >= size;
            }
        };
    }

    @Override
    public boolean isCatEnabled() {
        return this.m_domain != null && this.m_domain.isEnabled() && this.m_configManager.isCatEnabled();
    }

    @Override
    public boolean isMessageEnabled() {
        return this.m_domain != null && this.m_domain.isEnabled() && this.m_context.get() != null && this.m_configManager.isCatEnabled();
    }

    @Override
    public boolean isTraceMode() {
        DefaultMessageManager.Context content = this.getContext();
        return content != null ? content.isTraceMode() : false;
    }

    public void linkAsRunAway(DefaultForkedTransaction transaction) {
        DefaultMessageManager.Context ctx = this.getContext();
        if (ctx != null) {
            ctx.linkAsRunAway(transaction);
        }
    }

    public String nextMessageId() {
        return this.m_factory.getNextId();
    }

    @Override
    public void reset() {
        DefaultMessageManager.Context ctx = (DefaultMessageManager.Context) this.m_context.get();
        if (ctx != null) {
            if (ctx.m_totalDurationInMicros == 0L) {
                ctx.m_stack.clear();
                ctx.m_knownExceptions.clear();
                this.m_context.remove();
            } else {
                ctx.m_knownExceptions.clear();
            }
        }
    }

    public void setMetricType(String metricType) {
    }

    @Override
    public void setTraceMode(boolean traceMode) {
        DefaultMessageManager.Context context = this.getContext();
        if (context != null) {
            context.setTraceMode(traceMode);
        }
    }

    @Override
    public void setup() {
        DefaultMessageManager.Context ctx;
        if (this.m_domain != null) {
            ctx = new DefaultMessageManager.Context(this.m_domain.getId(), this.m_hostName, this.m_domain.getIp());
        } else {
            ctx = new DefaultMessageManager.Context("Unknown", this.m_hostName, "");
        }
        this.m_context.set(ctx);
    }

    boolean shouldLog(Throwable e) {
        DefaultMessageManager.Context ctx = (DefaultMessageManager.Context) this.m_context.get();
        return ctx != null ? ctx.shouldLog(e) : true;
    }

    @Override
    public void start(Transaction transaction, boolean forked) {
        DefaultMessageManager.Context ctx = this.getContext();
        if (ctx != null) {
            ctx.start(transaction, forked);
            if (transaction instanceof TaggedTransaction) {
                TaggedTransaction tt = (TaggedTransaction) transaction;
                this.m_taggedTransactions.put(tt.getTag(), tt);
            }
        } else if (this.m_firstMessage) {
            this.m_firstMessage = false;
            this.m_logger.warn("CAT client is not enabled because it's not initialized yet");
        }

    }

    class TransactionHelper {
        TransactionHelper() {
        }

        private void linkAsRunAway(DefaultForkedTransaction transaction) {
            DefaultEvent event = new DefaultEvent("RemoteCall", "RunAway");
            event.addData(transaction.getForkedMessageId(), transaction.getType() + ":" + transaction.getName());
            event.setTimestamp(transaction.getTimestamp());
            event.setStatus("0");
            event.setCompleted(true);
            transaction.setStandalone(true);
            DefaultMessageManager.this.add(event);
        }

        private void markAsNotCompleted(DefaultTransaction transaction) {
            DefaultEvent event = new DefaultEvent("cat", "BadInstrument");
            event.setStatus("TransactionNotCompleted");
            event.setCompleted(true);
            transaction.addChild(event);
            transaction.setCompleted(true);
        }

        private void markAsRunAway(Transaction parent, DefaultTaggedTransaction transaction) {
            if (!transaction.hasChildren()) {
                transaction.addData("RunAway");
            }
            transaction.setStatus("0");
            transaction.setStandalone(true);
            transaction.complete();
        }

        private void migrateMessage(Stack<Transaction> stack, Transaction source, Transaction target, int level) {
            Transaction current = level < stack.size() ? (Transaction) stack.get(level) : null;
            boolean shouldKeep = false;
            for (Message child : source.getChildren()) {
                if (child != current) {
                    target.addChild(child);
                } else {
                    DefaultTransaction cloned = new DefaultTransaction(current.getType(), current.getName(), DefaultMessageManager.this);
                    cloned.setTimestamp(current.getTimestamp());
                    cloned.setDurationInMicros(current.getDurationInMicros());
                    cloned.addData(current.getData().toString());
                    cloned.setStatus("0");
                    target.addChild(cloned);
                    this.migrateMessage(stack, current, cloned, level + 1);
                    shouldKeep = true;
                }
            }
            source.getChildren().clear();
            if (shouldKeep) {
                source.addChild(current);
            }
        }

        public void truncateAndFlush(DefaultMessageManager.Context ctx, long timestamp) {
            MessageTree tree = ctx.m_tree;
            Stack<Transaction> stack = ctx.m_stack;
            Message message = tree.getMessage();
            if (message instanceof DefaultTransaction) {
                String id = tree.getMessageId();
                if (id == null) {
                    id = DefaultMessageManager.this.nextMessageId();
                    tree.setMessageId(id);
                }
                String rootId = tree.getRootMessageId();
                String childId = DefaultMessageManager.this.nextMessageId();
                DefaultTransaction source = (DefaultTransaction) message;
                DefaultTransaction target = new DefaultTransaction(source.getType(), source.getName(), DefaultMessageManager.this);
                target.setTimestamp(source.getTimestamp());
                target.setDurationInMicros(source.getDurationInMicros());
                target.addData(source.getData().toString());
                target.setStatus("0");
                this.migrateMessage(stack, source, target, 1);
                for (int i = stack.size() - 1; i >= 0; --i) {
                    DefaultTransaction t = (DefaultTransaction) stack.get(i);
                    t.setTimestamp(timestamp);
                    t.setDurationStart(System.nanoTime());
                }
                DefaultEvent next = new DefaultEvent("RemoteCall", "Next");
                next.addData(childId);
                next.setStatus("0");
                target.addChild(next);
                MessageTree tx = tree.copy();
                tx.setMessage(target);
                ctx.m_tree.setMessageId(childId);
                ctx.m_tree.setParentMessageId(id);
                ctx.m_tree.setRootMessageId(rootId != null ? rootId : id);
                ctx.m_length = stack.size();
                ctx.m_totalDurationInMicros = ctx.m_totalDurationInMicros + target.getDurationInMicros();
                DefaultMessageManager.this.flush(tx);
            }
        }

        public void validate(Transaction parent, Transaction transaction) {
            if (transaction.isStandalone()) {
                List<Message> children = transaction.getChildren();
                int len = children.size();
                for (int i = 0; i < len; ++i) {
                    Message message = (Message) children.get(i);
                    if (message instanceof Transaction) {
                        this.validate(transaction, (Transaction) message);
                    }
                }
                if (!transaction.isCompleted() && transaction instanceof DefaultTransaction) {
                    this.markAsNotCompleted((DefaultTransaction) transaction);
                }
            } else if (!transaction.isCompleted()) {
                if (transaction instanceof DefaultForkedTransaction) {
                    this.linkAsRunAway((DefaultForkedTransaction) transaction);
                } else if (transaction instanceof DefaultTaggedTransaction) {
                    this.markAsRunAway(parent, (DefaultTaggedTransaction) transaction);
                }
            }
        }
    }

    class Context {
        private MessageTree m_tree = new DefaultMessageTree();
        private Stack<Transaction> m_stack = new Stack<>();
        private int m_length;
        private boolean m_traceMode;
        private long m_totalDurationInMicros;
        private Set<Throwable> m_knownExceptions;

        public Context(String domain, String hostName, String ipAddress) {
            Thread thread = Thread.currentThread();
            String groupName = thread.getThreadGroup().getName();
            this.m_tree.setThreadGroupName(groupName);
            this.m_tree.setThreadId(String.valueOf(thread.getId()));
            this.m_tree.setThreadName(thread.getName());
            this.m_tree.setDomain(domain);
            this.m_tree.setHostName(hostName);
            this.m_tree.setIpAddress(ipAddress);
            this.m_length = 1;
            this.m_knownExceptions = new HashSet<>();
        }

        public void add(Message message) {
            if (this.m_stack.isEmpty()) {
                MessageTree tree = this.m_tree.copy();
                tree.setMessage(message);
                DefaultMessageManager.this.flush(tree);
            } else {
                Transaction parent = (Transaction) this.m_stack.peek();
                this.addTransactionChild(message, parent);
            }
        }

        private void addTransactionChild(Message message, Transaction transaction) {
            long treePeriod = this.trimToHour(this.m_tree.getMessage().getTimestamp());
            long messagePeriod = this.trimToHour(message.getTimestamp() - 10000L);
            if (treePeriod < messagePeriod || this.m_length >= DefaultMessageManager.this.m_configManager.getMaxMessageLength()) {
                DefaultMessageManager.this.m_validator.truncateAndFlush(this, message.getTimestamp());
            }
            transaction.addChild(message);
            ++this.m_length;
        }

        private void adjustForTruncatedTransaction(Transaction root) {
            DefaultEvent next = new DefaultEvent("TruncatedTransaction", "TotalDuration");
            long actualDurationInMicros = this.m_totalDurationInMicros + root.getDurationInMicros();
            next.addData(String.valueOf(actualDurationInMicros));
            next.setStatus("0");
            root.addChild(next);
            this.m_totalDurationInMicros = 0L;
        }

        public boolean end(DefaultMessageManager manager, Transaction transaction) {
            if (!this.m_stack.isEmpty()) {
                Transaction current = (Transaction) this.m_stack.pop();
                if (transaction == current) {
                    DefaultMessageManager.this.m_validator.validate(this.m_stack.isEmpty() ? null : (Transaction) this.m_stack.peek(), current);
                } else {
                    while (transaction != current && !this.m_stack.empty()) {
                        DefaultMessageManager.this.m_validator.validate((Transaction) this.m_stack.peek(), current);
                        current = (Transaction) this.m_stack.pop();
                    }
                }
                if (this.m_stack.isEmpty()) {
                    MessageTree tree = this.m_tree.copy();
                    this.m_tree.setMessageId((String) null);
                    this.m_tree.setMessage((Message) null);
                    if (this.m_totalDurationInMicros > 0L) {
                        this.adjustForTruncatedTransaction((Transaction) tree.getMessage());
                    }
                    manager.flush(tree);
                    return true;
                }
            }
            return false;
        }

        public boolean isTraceMode() {
            return this.m_traceMode;
        }

        public void linkAsRunAway(DefaultForkedTransaction transaction) {
            DefaultMessageManager.this.m_validator.linkAsRunAway(transaction);
        }

        public Transaction peekTransaction(DefaultMessageManager defaultMessageManager) {
            return this.m_stack.isEmpty() ? null : (Transaction) this.m_stack.peek();
        }

        public void setTraceMode(boolean traceMode) {
            this.m_traceMode = traceMode;
        }

        public boolean shouldLog(Throwable e) {
            if (this.m_knownExceptions == null) {
                this.m_knownExceptions = new HashSet<>();
            }
            if (this.m_knownExceptions.contains(e)) {
                return false;
            } else {
                this.m_knownExceptions.add(e);
                return true;
            }
        }

        public void start(Transaction transaction, boolean forked) {
            if (!this.m_stack.isEmpty()) {
                if (!(transaction instanceof ForkedTransaction)) {
                    Transaction parent = this.m_stack.peek();
                    this.addTransactionChild(transaction, parent);
                }
            } else {
                this.m_tree.setMessage(transaction);
            }
            if (!forked) {
                this.m_stack.push(transaction);
            }
        }

        private long trimToHour(long timestamp) {
            return timestamp - timestamp % 3600_000L;
        }
    }
}
