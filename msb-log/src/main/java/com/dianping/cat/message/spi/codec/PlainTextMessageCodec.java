package com.dianping.cat.message.spi.codec;

import com.dianping.cat.message.Event;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Metric;
import com.dianping.cat.message.Trace;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultEvent;
import com.dianping.cat.message.internal.DefaultHeartbeat;
import com.dianping.cat.message.internal.DefaultMetric;
import com.dianping.cat.message.internal.DefaultTrace;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import io.netty.buffer.ByteBuf;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TimeZone;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

public class PlainTextMessageCodec implements MessageCodec, LogEnabled {
    public static final String ID = "plain-text";
    private static final String VERSION = "PT1";
    private static final byte TAB = 9;
    private static final byte LF = 10;
    private BufferWriter m_writer = new EscapingBufferWriter();
    private PlainTextMessageCodec.BufferHelper m_bufferHelper;
    private PlainTextMessageCodec.DateHelper m_dateHelper;
    private ThreadLocal<PlainTextMessageCodec.Context> m_ctx;
    private Logger m_logger;

    public PlainTextMessageCodec() {
        this.m_bufferHelper = new PlainTextMessageCodec.BufferHelper(this.m_writer);
        this.m_dateHelper = new PlainTextMessageCodec.DateHelper();
        this.m_ctx = ThreadLocal.withInitial(Context::new);
    }

    @Override
    public MessageTree decode(ByteBuf buf) {
        MessageTree tree = new DefaultMessageTree();
        this.decode(buf, tree);
        return tree;
    }

    @Override
    public void decode(ByteBuf buf, MessageTree tree) {
        PlainTextMessageCodec.Context ctx = ((PlainTextMessageCodec.Context) this.m_ctx.get()).setBuffer(buf);
        this.decodeHeader(ctx, tree);
        if (buf.readableBytes() > 0) {
            this.decodeMessage(ctx, tree);
        }
    }

    protected void decodeHeader(PlainTextMessageCodec.Context ctx, MessageTree tree) {
        PlainTextMessageCodec.BufferHelper helper = this.m_bufferHelper;
        String id = helper.read(ctx, (byte) 9);
        String domain = helper.read(ctx, (byte) 9);
        String hostName = helper.read(ctx, (byte) 9);
        String ipAddress = helper.read(ctx, (byte) 9);
        String threadGroupName = helper.read(ctx, (byte) 9);
        String threadId = helper.read(ctx, (byte) 9);
        String threadName = helper.read(ctx, (byte) 9);
        String messageId = helper.read(ctx, (byte) 9);
        String parentMessageId = helper.read(ctx, (byte) 9);
        String rootMessageId = helper.read(ctx, (byte) 9);
        String sessionToken = helper.read(ctx, (byte) 10);
        if ("PT1".equals(id)) {
            tree.setDomain(domain);
            tree.setHostName(hostName);
            tree.setIpAddress(ipAddress);
            tree.setThreadGroupName(threadGroupName);
            tree.setThreadId(threadId);
            tree.setThreadName(threadName);
            tree.setMessageId(messageId);
            tree.setParentMessageId(parentMessageId);
            tree.setRootMessageId(rootMessageId);
            tree.setSessionToken(sessionToken);
        } else {
            throw new RuntimeException(String.format("Unrecognized id(%s) for plain text message codec!", id));
        }
    }

    protected Message decodeLine(PlainTextMessageCodec.Context ctx, DefaultTransaction parent, Stack<DefaultTransaction> stack) {
        PlainTextMessageCodec.BufferHelper helper = this.m_bufferHelper;
        byte identifier = ctx.getBuffer().readByte();
        String timestamp = helper.read(ctx, (byte) 9);
        String type = helper.read(ctx, (byte) 9);
        String name = helper.read(ctx, (byte) 9);
        switch (identifier) {
            case 65:
                DefaultTransaction tran = new DefaultTransaction(type, name, null);
                String status = helper.read(ctx, (byte) 9);
                String duration = helper.read(ctx, (byte) 9);
                String data = helper.read(ctx, (byte) 9);
                helper.read(ctx, (byte) 10);
                tran.setTimestamp(this.m_dateHelper.parse(timestamp));
                tran.setStatus(status);
                tran.addData(data);
                long d = Long.parseLong(duration.substring(0, duration.length() - 2));
                tran.setDurationInMicros(d);
                if (parent != null) {
                    parent.addChild(tran);
                    return parent;
                }
                return tran;
            case 69:
                DefaultEvent event = new DefaultEvent(type, name);
                String eventStatus = helper.read(ctx, (byte) 9);
                String eventData = helper.read(ctx, (byte) 9);
                helper.read(ctx, (byte) 10);
                event.setTimestamp(this.m_dateHelper.parse(timestamp));
                event.setStatus(eventStatus);
                event.addData(eventData);
                if (parent != null) {
                    parent.addChild(event);
                    return parent;
                }
                return event;
            case 72:
                DefaultHeartbeat heartbeat = new DefaultHeartbeat(type, name);
                String heartbeatStatus = helper.read(ctx, (byte) 9);
                String heartbeatData = helper.read(ctx, (byte) 9);
                helper.read(ctx, (byte) 10);
                heartbeat.setTimestamp(this.m_dateHelper.parse(timestamp));
                heartbeat.setStatus(heartbeatStatus);
                heartbeat.addData(heartbeatData);
                if (parent != null) {
                    parent.addChild(heartbeat);
                    return parent;
                }
                return heartbeat;
            case 76:
                DefaultTrace trace = new DefaultTrace(type, name);
                String traceStatus = helper.read(ctx, (byte) 9);
                String traceData = helper.read(ctx, (byte) 9);
                helper.read(ctx, (byte) 10);
                trace.setTimestamp(this.m_dateHelper.parse(timestamp));
                trace.setStatus(traceStatus);
                trace.addData(traceData);
                if (parent != null) {
                    parent.addChild(trace);
                    return parent;
                }
                return trace;
            case 77:
                DefaultMetric metric = new DefaultMetric(type, name);
                String metricStatus = helper.read(ctx, (byte) 9);
                String metricData = helper.read(ctx, (byte) 9);
                helper.read(ctx, (byte) 10);
                metric.setTimestamp(this.m_dateHelper.parse(timestamp));
                metric.setStatus(metricStatus);
                metric.addData(metricData);
                if (parent != null) {
                    parent.addChild(metric);
                    return parent;
                }
                return metric;
            case 84:
                String transactionStatus = helper.read(ctx, (byte) 9);
                String transactionDuration = helper.read(ctx, (byte) 9);
                String transactionData = helper.read(ctx, (byte) 9);
                helper.read(ctx, (byte) 10);
                parent.setStatus(transactionStatus);
                parent.addData(transactionData);
                long transactionD = Long.parseLong(transactionDuration.substring(0, transactionDuration.length() - 2));
                parent.setDurationInMicros(transactionD);
                return (Message) stack.pop();
            case 116:
                DefaultTransaction transaction = new DefaultTransaction(type, name, null);
                helper.read(ctx, (byte) 10);
                transaction.setTimestamp(this.m_dateHelper.parse(timestamp));
                if (parent != null) {
                    parent.addChild(transaction);
                }
                stack.push(parent);
                return transaction;
            default:
                this.m_logger.warn("Unknown identifier(" + (char) identifier + ") of message: " + ctx.getBuffer().toString(StandardCharsets.UTF_8));
                throw new RuntimeException("Unknown identifier int name");
        }
    }

    protected void decodeMessage(PlainTextMessageCodec.Context ctx, MessageTree tree) {
        Stack<DefaultTransaction> stack = new Stack();
        Message parent = this.decodeLine(ctx, (DefaultTransaction) null, stack);
        tree.setMessage(parent);
        while (ctx.getBuffer().readableBytes() > 0) {
            Message message = this.decodeLine(ctx, (DefaultTransaction) parent, stack);
            if (!(message instanceof DefaultTransaction)) {
                break;
            }
            parent = message;
        }
    }

    @Override
    public void enableLogging(Logger logger) {
        this.m_logger = logger;
    }

    @Override
    public void encode(MessageTree tree, ByteBuf buf) {
        int count = 0;
        int index = buf.writerIndex();
        buf.writeInt(0);
        count = count + this.encodeHeader(tree, buf);
        if (tree.getMessage() != null) {
            count += this.encodeMessage(tree.getMessage(), buf);
        }
        buf.setInt(index, count);
    }

    protected int encodeHeader(MessageTree tree, ByteBuf buf) {
        PlainTextMessageCodec.BufferHelper helper = this.m_bufferHelper;
        int count = 0;
        count = count + helper.write(buf, "PT1");
        count += helper.write(buf, (byte) 9);
        count += helper.write(buf, tree.getDomain());
        count += helper.write(buf, (byte) 9);
        count += helper.write(buf, tree.getHostName());
        count += helper.write(buf, (byte) 9);
        count += helper.write(buf, tree.getIpAddress());
        count += helper.write(buf, (byte) 9);
        count += helper.write(buf, tree.getThreadGroupName());
        count += helper.write(buf, (byte) 9);
        count += helper.write(buf, tree.getThreadId());
        count += helper.write(buf, (byte) 9);
        count += helper.write(buf, tree.getThreadName());
        count += helper.write(buf, (byte) 9);
        count += helper.write(buf, tree.getMessageId());
        count += helper.write(buf, (byte) 9);
        count += helper.write(buf, tree.getParentMessageId());
        count += helper.write(buf, (byte) 9);
        count += helper.write(buf, tree.getRootMessageId());
        count += helper.write(buf, (byte) 9);
        count += helper.write(buf, tree.getSessionToken());
        count += helper.write(buf, (byte) 10);
        return count;
    }

    protected int encodeLine(Message message, ByteBuf buf, char type, PlainTextMessageCodec.Policy policy) {
        PlainTextMessageCodec.BufferHelper helper = this.m_bufferHelper;
        int count = 0;
        count = count + helper.write(buf, (byte) type);
        if (type == 'T' && message instanceof Transaction m) {
            long duration = m.getDurationInMillis();
            count += helper.write(buf, this.m_dateHelper.format(message.getTimestamp() + duration));
        } else {
            count += helper.write(buf, this.m_dateHelper.format(message.getTimestamp()));
        }
        count += helper.write(buf, (byte) 9);
        count += helper.writeRaw(buf, message.getType());
        count += helper.write(buf, (byte) 9);
        count += helper.writeRaw(buf, message.getName());
        count += helper.write(buf, (byte) 9);
        if (policy != PlainTextMessageCodec.Policy.WITHOUT_STATUS) {
            count += helper.writeRaw(buf, message.getStatus());
            count += helper.write(buf, (byte) 9);
            Object data = message.getData();
            if (policy == PlainTextMessageCodec.Policy.WITH_DURATION && message instanceof Transaction) {
                long duration = ((Transaction) message).getDurationInMicros();
                count += helper.write(buf, String.valueOf(duration));
                count += helper.write(buf, "us");
                count += helper.write(buf, (byte) 9);
            }
            count += helper.writeRaw(buf, String.valueOf(data));
            count += helper.write(buf, (byte) 9);
        }
        count += helper.write(buf, (byte) 10);
        return count;
    }

    public int encodeMessage(Message message, ByteBuf buf) {
        if (message instanceof Transaction) {
            Transaction transaction = (Transaction) message;
            List<Message> children = transaction.getChildren();
            if (children.isEmpty()) {
                return this.encodeLine(transaction, buf, 'A', PlainTextMessageCodec.Policy.WITH_DURATION);
            } else {
                int count = 0;
                int len = children.size();
                count = count + this.encodeLine(transaction, buf, 't', PlainTextMessageCodec.Policy.WITHOUT_STATUS);
                for (int i = 0; i < len; ++i) {
                    Message child = (Message) children.get(i);
                    if (child != null) {
                        count += this.encodeMessage(child, buf);
                    }
                }
                count += this.encodeLine(transaction, buf, 'T', PlainTextMessageCodec.Policy.WITH_DURATION);
                return count;
            }
        } else if (message instanceof Event) {
            return this.encodeLine(message, buf, 'E', PlainTextMessageCodec.Policy.DEFAULT);
        } else if (message instanceof Trace) {
            return this.encodeLine(message, buf, 'L', PlainTextMessageCodec.Policy.DEFAULT);
        } else if (message instanceof Metric) {
            return this.encodeLine(message, buf, 'M', PlainTextMessageCodec.Policy.DEFAULT);
        } else if (message instanceof Heartbeat) {
            return this.encodeLine(message, buf, 'H', PlainTextMessageCodec.Policy.DEFAULT);
        } else {
            throw new RuntimeException(String.format("Unsupported message type: %s.", message));
        }
    }

    public void reset() {
        this.m_ctx.remove();
    }

    protected void setBufferWriter(BufferWriter writer) {
        this.m_writer = writer;
        this.m_bufferHelper = new PlainTextMessageCodec.BufferHelper(this.m_writer);
    }

    protected enum Policy {
        DEFAULT,
        WITHOUT_STATUS,
        WITH_DURATION;

        Policy() {
        }

        public static PlainTextMessageCodec.Policy getByMessageIdentifier(byte identifier) {
            switch (identifier) {
                case 65:
                case 84:
                    return WITH_DURATION;
                case 69:
                case 72:
                    return DEFAULT;
                case 116:
                    return WITHOUT_STATUS;
                default:
                    return DEFAULT;
            }
        }
    }

    protected static class DateHelper {
        private BlockingQueue<SimpleDateFormat> m_formats = new ArrayBlockingQueue<>(20);
        private Map<String, Long> m_map = new ConcurrentHashMap<>();

        protected DateHelper() {
        }

        public String format(long timestamp) {
            SimpleDateFormat format = (SimpleDateFormat) this.m_formats.poll();
            if (format == null) {
                format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                format.setTimeZone(TimeZone.getTimeZone("GMT+8"));
            }
            String ans = null;
            try {
                ans = format.format(new Date(timestamp));
            } finally {
                if (this.m_formats.remainingCapacity() > 0) {
                    this.m_formats.offer(format);
                }
            }
            return ans;
        }

        public long parse(String str) {
            int len = str.length();
            String date = str.substring(0, 10);
            Long baseline = (Long) this.m_map.get(date);
            if (baseline == null) {
                try {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    format.setTimeZone(TimeZone.getTimeZone("GMT+8"));
                    baseline = format.parse(date).getTime();
                    this.m_map.put(date, baseline);
                } catch (ParseException ignored) {
                    return -1L;
                }
            }
            long time = baseline;
            long metric = 1L;
            boolean millisecond = true;
            for (int i = len - 1; i > 10; --i) {
                char ch = str.charAt(i);
                if (ch >= '0' && ch <= '9') {
                    time += (long) (ch - 48) * metric;
                    metric *= 10L;
                } else if (millisecond) {
                    millisecond = false;
                } else {
                    metric = metric / 100L * 60L;
                }
            }
            return time;
        }
    }

    public static class Context {
        private ByteBuf m_buffer;
        private char[] m_data = new char[4194304];

        public Context() {
        }

        public ByteBuf getBuffer() {
            return this.m_buffer;
        }

        public char[] getData() {
            return this.m_data;
        }

        public PlainTextMessageCodec.Context setBuffer(ByteBuf buffer) {
            this.m_buffer = buffer;
            return this;
        }
    }

    protected static class BufferHelper {
        private BufferWriter m_writer;

        public BufferHelper(BufferWriter writer) {
            this.m_writer = writer;
        }

        public String read(PlainTextMessageCodec.Context ctx, byte separator) {
            ByteBuf buf = ctx.getBuffer();
            char[] data = ctx.getData();
            int from = buf.readerIndex();
            int to = buf.writerIndex();
            int index = 0;
            boolean flag = false;

            for (int i = from; i < to; ++i) {
                byte b = buf.readByte();
                if (b == separator) {
                    break;
                }
                if (index >= data.length) {
                    char[] data2 = new char[to - from];
                    System.arraycopy(data, 0, data2, 0, index);
                    data = data2;
                }
                char c = (char) (b & 255);
                if (c > 127) {
                    flag = true;
                }
                if (c == '\\' && i + 1 < to) {
                    byte b2 = buf.readByte();
                    if (b2 == 116) {
                        c = '\t';
                        ++i;
                    } else if (b2 == 114) {
                        c = '\r';
                        ++i;
                    } else if (b2 == 110) {
                        c = '\n';
                        ++i;
                    } else if (b2 == 92) {
                        c = '\\';
                        ++i;
                    } else {
                        buf.readerIndex(i + 1);
                    }
                }
                data[index] = c;
                ++index;
            }
            if (!flag) {
                return new String(data, 0, index);
            } else {
                byte[] ba = new byte[index];
                for (int i = 0; i < index; ++i) {
                    ba[i] = (byte) (data[i] & 255);
                }
                try {
                    return new String(ba, 0, index, "utf-8");
                } catch (UnsupportedEncodingException ignored) {
                    return new String(ba, 0, index);
                }
            }
        }

        public int write(ByteBuf buf, byte b) {
            buf.writeByte(b);
            return 1;
        }

        public int write(ByteBuf buf, String str) {
            if (str == null) {
                str = "null";
            }
            byte[] data = str.getBytes();
            buf.writeBytes(data);
            return data.length;
        }

        public int writeRaw(ByteBuf buf, String str) {
            if (str == null) {
                str = "null";
            }
            byte[] data;
            data = str.getBytes(StandardCharsets.UTF_8);
            return this.m_writer.writeTo(buf, data);
        }
    }
}
