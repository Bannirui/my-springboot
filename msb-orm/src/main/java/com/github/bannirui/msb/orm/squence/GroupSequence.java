package com.github.bannirui.msb.orm.squence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class GroupSequence extends AbstractSequence {
    private static final Logger logger = LoggerFactory.getLogger(GroupSequence.class);
    private DataSource dataSource;
    private String name;

    public GroupSequence(String name, DataSource dataSource) {
        this.name = name;
        this.dataSource = dataSource;
    }

    String getSequenceName() {
        return this.name;
    }

    SequenceDao getSequenceDao() {
        return new DefaultSequenceDao(this.dataSource);
    }
}
