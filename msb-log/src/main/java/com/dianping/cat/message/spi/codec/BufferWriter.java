package com.dianping.cat.message.spi.codec;

import io.netty.buffer.ByteBuf;

public interface BufferWriter {
    int writeTo(ByteBuf buf, byte[] data);
}
