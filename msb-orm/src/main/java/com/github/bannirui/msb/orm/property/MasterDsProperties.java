package com.github.bannirui.msb.orm.property;

public class MasterDsProperties extends BaseDsProperties {
    private SlaveDsProperties slave;

    public SlaveDsProperties getSlave() {
        return this.slave;
    }

    public void setSlave(SlaveDsProperties slave) {
        this.slave = slave;
    }
}
