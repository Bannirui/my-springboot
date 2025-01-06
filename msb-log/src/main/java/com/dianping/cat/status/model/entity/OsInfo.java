package com.dianping.cat.status.model.entity;

import com.dianping.cat.status.model.BaseEntity;
import com.dianping.cat.status.model.IVisitor;

public class OsInfo extends BaseEntity<OsInfo> {
    private String m_name;
    private String m_arch;
    private String m_version;
    private int m_availableProcessors;
    private double m_systemLoadAverage;
    private long m_processTime;
    private long m_totalPhysicalMemory;
    private long m_freePhysicalMemory;
    private long m_committedVirtualMemory;
    private long m_totalSwapSpace;
    private long m_freeSwapSpace;
    private long m_processUpTime;
    private double m_systemCpuUsage;

    @Override
    public void accept(IVisitor visitor) {
        visitor.visitOs(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OsInfo) {
            OsInfo _o = (OsInfo) obj;
            if (!this.equals(this.m_name, _o.getName())) {
                return false;
            } else if (!this.equals(this.m_arch, _o.getArch())) {
                return false;
            } else if (!this.equals(this.m_version, _o.getVersion())) {
                return false;
            } else if (this.m_availableProcessors != _o.getAvailableProcessors()) {
                return false;
            } else if (this.m_systemLoadAverage != _o.getSystemLoadAverage()) {
                return false;
            } else if (this.m_processTime != _o.getProcessTime()) {
                return false;
            } else if (this.m_totalPhysicalMemory != _o.getTotalPhysicalMemory()) {
                return false;
            } else if (this.m_freePhysicalMemory != _o.getFreePhysicalMemory()) {
                return false;
            } else if (this.m_committedVirtualMemory != _o.getCommittedVirtualMemory()) {
                return false;
            } else if (this.m_totalSwapSpace != _o.getTotalSwapSpace()) {
                return false;
            } else if (this.m_freeSwapSpace != _o.getFreeSwapSpace()) {
                return false;
            } else if (this.m_processUpTime != _o.getProcessUpTime()) {
                return false;
            } else {
                return this.m_systemCpuUsage == _o.getSystemCpuUsage();
            }
        } else {
            return false;
        }
    }

    public String getArch() {
        return this.m_arch;
    }

    public int getAvailableProcessors() {
        return this.m_availableProcessors;
    }

    public long getCommittedVirtualMemory() {
        return this.m_committedVirtualMemory;
    }

    public long getFreePhysicalMemory() {
        return this.m_freePhysicalMemory;
    }

    public long getFreeSwapSpace() {
        return this.m_freeSwapSpace;
    }

    public String getName() {
        return this.m_name;
    }

    public long getProcessTime() {
        return this.m_processTime;
    }

    public double getSystemLoadAverage() {
        return this.m_systemLoadAverage;
    }

    public long getTotalPhysicalMemory() {
        return this.m_totalPhysicalMemory;
    }

    public long getTotalSwapSpace() {
        return this.m_totalSwapSpace;
    }

    public String getVersion() {
        return this.m_version;
    }

    public long getProcessUpTime() {
        return this.m_processUpTime;
    }

    public double getSystemCpuUsage() {
        return this.m_systemCpuUsage;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = hash * 31 + (this.m_name == null ? 0 : this.m_name.hashCode());
        hash = hash * 31 + (this.m_arch == null ? 0 : this.m_arch.hashCode());
        hash = hash * 31 + (this.m_version == null ? 0 : this.m_version.hashCode());
        hash = hash * 31 + this.m_availableProcessors;
        hash = hash * 31 + (int) (Double.doubleToLongBits(this.m_systemLoadAverage) ^ Double.doubleToLongBits(this.m_systemLoadAverage) >>> 32);
        hash = hash * 31 + (int) (this.m_processTime ^ this.m_processTime >>> 32);
        hash = hash * 31 + (int) (this.m_totalPhysicalMemory ^ this.m_totalPhysicalMemory >>> 32);
        hash = hash * 31 + (int) (this.m_freePhysicalMemory ^ this.m_freePhysicalMemory >>> 32);
        hash = hash * 31 + (int) (this.m_committedVirtualMemory ^ this.m_committedVirtualMemory >>> 32);
        hash = hash * 31 + (int) (this.m_totalSwapSpace ^ this.m_totalSwapSpace >>> 32);
        hash = hash * 31 + (int) (this.m_freeSwapSpace ^ this.m_freeSwapSpace >>> 32);
        hash = hash * 31 + (int) (this.m_processUpTime ^ this.m_processUpTime >>> 32);
        hash = hash * 31 + (int) (Double.doubleToLongBits(this.m_systemCpuUsage) ^ Double.doubleToLongBits(this.m_systemCpuUsage) >>> 32);
        return hash;
    }

    @Override
    public void mergeAttributes(OsInfo other) {
        if (other.getName() != null) {
            this.m_name = other.getName();
        }
        if (other.getArch() != null) {
            this.m_arch = other.getArch();
        }
        if (other.getVersion() != null) {
            this.m_version = other.getVersion();
        }
        this.m_availableProcessors = other.getAvailableProcessors();
        this.m_systemLoadAverage = other.getSystemLoadAverage();
        this.m_processTime = other.getProcessTime();
        this.m_totalPhysicalMemory = other.getTotalPhysicalMemory();
        this.m_freePhysicalMemory = other.getFreePhysicalMemory();
        this.m_committedVirtualMemory = other.getCommittedVirtualMemory();
        this.m_totalSwapSpace = other.getTotalSwapSpace();
        this.m_freeSwapSpace = other.getFreeSwapSpace();
        this.m_processUpTime = other.getProcessUpTime();
        this.m_systemCpuUsage = other.getSystemCpuUsage();
    }

    public OsInfo setArch(String arch) {
        this.m_arch = arch;
        return this;
    }

    public OsInfo setAvailableProcessors(int availableProcessors) {
        this.m_availableProcessors = availableProcessors;
        return this;
    }

    public OsInfo setCommittedVirtualMemory(long committedVirtualMemory) {
        this.m_committedVirtualMemory = committedVirtualMemory;
        return this;
    }

    public OsInfo setFreePhysicalMemory(long freePhysicalMemory) {
        this.m_freePhysicalMemory = freePhysicalMemory;
        return this;
    }

    public OsInfo setFreeSwapSpace(long freeSwapSpace) {
        this.m_freeSwapSpace = freeSwapSpace;
        return this;
    }

    public OsInfo setName(String name) {
        this.m_name = name;
        return this;
    }

    public OsInfo setProcessTime(long processTime) {
        this.m_processTime = processTime;
        return this;
    }

    public OsInfo setSystemLoadAverage(double systemLoadAverage) {
        this.m_systemLoadAverage = systemLoadAverage;
        return this;
    }

    public OsInfo setTotalPhysicalMemory(long totalPhysicalMemory) {
        this.m_totalPhysicalMemory = totalPhysicalMemory;
        return this;
    }

    public OsInfo setTotalSwapSpace(long totalSwapSpace) {
        this.m_totalSwapSpace = totalSwapSpace;
        return this;
    }

    public OsInfo setVersion(String version) {
        this.m_version = version;
        return this;
    }

    public OsInfo setProcessUpTime(long m_processUpTime) {
        this.m_processUpTime = m_processUpTime;
        return this;
    }

    public OsInfo setSystemCpuUsage(double m_systemCpuUsage) {
        this.m_systemCpuUsage = m_systemCpuUsage;
        return this;
    }
}
