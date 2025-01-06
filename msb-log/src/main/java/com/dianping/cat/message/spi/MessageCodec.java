package com.dianping.cat.message.spi;

import io.netty.buffer.ByteBuf;

public interface MessageCodec {
    MessageTree decode(ByteBuf buf);

    void decode(ByteBuf buf, MessageTree tree);

    void encode(MessageTree tree, ByteBuf buf);
}
