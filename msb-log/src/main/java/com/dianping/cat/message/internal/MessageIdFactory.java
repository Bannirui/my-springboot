package com.dianping.cat.message.internal;

import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.site.helper.Splitters;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class MessageIdFactory {
    private volatile long m_timestamp = this.getTimestamp();
    private volatile AtomicInteger m_index;
    private String m_domain;
    private String m_ipAddress;
    private MappedByteBuffer m_byteBuffer;
    private RandomAccessFile m_markFile;
    private static final long HOUR = 3600000L;
    private BlockingQueue<String> m_reusedIds = new LinkedBlockingQueue<>(100_000);

    public void close() {
        try {
            this.m_markFile.close();
        } catch (Exception var2) {
        }
    }

    private File createMarkFile(String domain) {
        File mark = new File("/data/appdatas/cat/", "cat-" + domain + ".mark");
        if (!mark.exists()) {
            boolean success = true;
            try {
                success = mark.createNewFile();
            } catch (Exception var5) {
                success = false;
            }
            if (!success) {
                mark = this.createTempFile(domain);
            }
        } else if (!mark.canWrite()) {
            mark = this.createTempFile(domain);
        }
        return mark;
    }

    private File createTempFile(String domain) {
        String tmpDir = System.getProperty("java.io.tmpdir");
        File mark = new File(tmpDir, "cat-" + domain + ".mark");
        return mark;
    }

    public String getNextId() {
        String id = (String)this.m_reusedIds.poll();
        if (id != null) {
            return id;
        } else {
            long timestamp = this.getTimestamp();
            if (timestamp != this.m_timestamp) {
                this.m_index = new AtomicInteger(0);
                this.m_timestamp = timestamp;
            }
            int index = this.m_index.getAndIncrement();
            StringBuilder sb = new StringBuilder(this.m_domain.length() + 32);
            sb.append(this.m_domain);
            sb.append('-');
            sb.append(this.m_ipAddress);
            sb.append('-');
            sb.append(timestamp);
            sb.append('-');
            sb.append(index);
            return sb.toString();
        }
    }

    protected long getTimestamp() {
        long timestamp = MilliSecondTimer.currentTimeMillis();
        return timestamp / 3600000L;
    }

    public void initialize(String domain) throws IOException {
        this.m_domain = domain;
        if (this.m_ipAddress == null) {
            String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
            List<String> items = Splitters.by(".").noEmptyItem().split(ip);
            byte[] bytes = new byte[4];
            for(int i = 0; i < 4; ++i) {
                bytes[i] = (byte)Integer.parseInt((String)items.get(i));
            }
            StringBuilder sb = new StringBuilder(bytes.length / 2);
            for (byte b : bytes) {
                sb.append(Integer.toHexString(b >> 4 & 15));
                sb.append(Integer.toHexString(b & 15));
            }
            this.m_ipAddress = sb.toString();
        }
        File mark = this.createMarkFile(domain);
        this.m_markFile = new RandomAccessFile(mark, "rw");
        this.m_byteBuffer = this.m_markFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0L, 20L);
        if (this.m_byteBuffer.limit() > 0) {
            int index = this.m_byteBuffer.getInt();
            long lastTimestamp = this.m_byteBuffer.getLong();
            if (lastTimestamp == this.m_timestamp) {
                this.m_index = new AtomicInteger(index + 10000);
            } else {
                this.m_index = new AtomicInteger(0);
            }
        }
        this.saveMark();
    }

    protected void resetIndex() {
        this.m_index.set(0);
    }

    public void reuse(String id) {
        this.m_reusedIds.offer(id);
    }

    public void saveMark() {
        try {
            this.m_byteBuffer.rewind();
            this.m_byteBuffer.putInt(this.m_index.get());
            this.m_byteBuffer.putLong(this.m_timestamp);
        } catch (Exception var2) {
        }
    }

    public void setDomain(String domain) {
        this.m_domain = domain;
    }

    public void setIpAddress(String ipAddress) {
        this.m_ipAddress = ipAddress;
    }
}
