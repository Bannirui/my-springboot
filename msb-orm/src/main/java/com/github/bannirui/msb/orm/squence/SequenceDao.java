package com.github.bannirui.msb.orm.squence;

public interface SequenceDao extends Lifecycle {
    SequenceRange nextRange(String var1);

    int getStep();

    int getRetryTimes();

    void adjust(String name) throws SQLException;
}
