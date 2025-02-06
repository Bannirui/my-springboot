package com.github.bannirui.msb.orm.squence;

public interface Sequence {
    long nextValue();

    long nextValue(int type);

    boolean exhaustValue();
}
