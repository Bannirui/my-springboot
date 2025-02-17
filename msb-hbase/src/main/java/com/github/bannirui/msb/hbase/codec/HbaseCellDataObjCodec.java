package com.github.bannirui.msb.hbase.codec;

public abstract class HbaseCellDataObjCodec<T> implements HbaseCellDataCodec {

    @Override
    public byte[] encode(Object data) {
        return this.encodeT((T) data);
    }

    @Override
    public Object decode(byte[] bytes) {
        return this.decodeT(bytes);
    }

    public abstract byte[] encodeT(T data);

    public abstract T decodeT(byte[] bytes);
}
