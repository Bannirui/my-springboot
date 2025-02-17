package com.github.bannirui.msb.hbase.codec;

public interface HbaseCellDataCodec {
    byte[] encode(Object data);

    Object decode(byte[] bytes);
}
