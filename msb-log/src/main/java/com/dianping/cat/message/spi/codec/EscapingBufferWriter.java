package com.dianping.cat.message.spi.codec;

import io.netty.buffer.ByteBuf;

public class EscapingBufferWriter implements BufferWriter {
    public static final String ID = "escape";

    public int writeTo(ByteBuf buf, byte[] data) {
        int len = data.length;
        int count = len;
        int offset = 0;
        for(int i = 0; i < len; ++i) {
            byte b = data[i];
            if (b == 9 || b == 13 || b == 10 || b == 92) {
                buf.writeBytes(data, offset, i - offset);
                buf.writeByte(92);
                if (b == 9) {
                    buf.writeByte(116);
                } else if (b == 13) {
                    buf.writeByte(114);
                } else if (b == 10) {
                    buf.writeByte(110);
                } else {
                    buf.writeByte(b);
                }
                ++count;
                offset = i + 1;
            }
        }
        if (len > offset) {
            buf.writeBytes(data, offset, len - offset);
        }
        return count;
    }
}
