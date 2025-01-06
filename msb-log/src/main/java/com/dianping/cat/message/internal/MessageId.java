package com.dianping.cat.message.internal;

import com.site.helper.Splitters;
import java.util.List;

public class MessageId {

    private static final long VERSION1_THRESHOLD = 1325347200000L;
    private String m_domain;
    private String m_ipAddressInHex;
    private long m_timestamp;
    private int m_index;

    public static MessageId parse(String messageId) {
        List<String> list = Splitters.by('-').split(messageId);
        int len = list.size();
        if (len < 4) {
            throw new RuntimeException("Invalid message id format: " + messageId);
        } else {
            String ipAddressInHex = (String) list.get(len - 3);
            long timestamp = Long.parseLong((String) list.get(len - 2));
            int index = Integer.parseInt((String) list.get(len - 1));
            String domain;
            if (len > 4) {
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < len - 3; ++i) {
                    if (i > 0) {
                        sb.append('-');
                    }
                    sb.append(list.get(i));
                }
                domain = sb.toString();
            } else {
                domain = list.get(0);
            }
            return new MessageId(domain, ipAddressInHex, timestamp, index);
        }
    }

    MessageId(String domain, String ipAddressInHex, long timestamp, int index) {
        this.m_domain = domain;
        this.m_ipAddressInHex = ipAddressInHex;
        this.m_timestamp = timestamp;
        this.m_index = index;
    }

    public String getDomain() {
        return this.m_domain;
    }

    public int getIndex() {
        return this.m_index;
    }

    public String getIpAddress() {
        StringBuilder sb = new StringBuilder();
        String local = this.m_ipAddressInHex;
        int length = local.length();
        for (int i = 0; i < length; i += 2) {
            char first = local.charAt(i);
            char next = local.charAt(i + 1);
            int temp = 0;
            if (first >= '0' && first <= '9') {
                temp = temp + (first - 48 << 4);
            } else {
                temp = temp + (first - 97 + 10 << 4);
            }
            if (next >= '0' && next <= '9') {
                temp += next - 48;
            } else {
                temp += next - 97 + 10;
            }
            if (sb.length() > 0) {
                sb.append('.');
            }
            sb.append(temp);
        }
        return sb.toString();
    }

    public String getIpAddressInHex() {
        return this.m_ipAddressInHex;
    }

    public long getTimestamp() {
        return this.m_timestamp > 1325347200000L ? this.m_timestamp : this.m_timestamp * 3600L * 1000L;
    }

    public int getVersion() {
        return this.m_timestamp > 1325347200000L ? 1 : 2;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.m_domain.length() + 32);
        sb.append(this.m_domain);
        sb.append('-');
        sb.append(this.m_ipAddressInHex);
        sb.append('-');
        sb.append(this.m_timestamp);
        sb.append('-');
        sb.append(this.m_index);
        return sb.toString();
    }
}
