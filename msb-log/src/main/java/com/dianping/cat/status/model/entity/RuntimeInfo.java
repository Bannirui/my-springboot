package com.dianping.cat.status.model.entity;

import com.dianping.cat.status.model.BaseEntity;
import com.dianping.cat.status.model.IVisitor;

public class RuntimeInfo extends BaseEntity<RuntimeInfo> {
    private long m_startTime;
    private long m_upTime;
    private String m_javaVersion;
    private String m_userName;
    private String m_userDir;
    private String m_javaClasspath;

    @Override
    public void accept(IVisitor visitor) {
        visitor.visitRuntime(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RuntimeInfo) {
            RuntimeInfo _o = (RuntimeInfo) obj;
            if (this.m_startTime != _o.getStartTime()) {
                return false;
            } else if (this.m_upTime != _o.getUpTime()) {
                return false;
            } else if (!this.equals(this.m_javaVersion, _o.getJavaVersion())) {
                return false;
            } else if (!this.equals(this.m_userName, _o.getUserName())) {
                return false;
            } else if (!this.equals(this.m_userDir, _o.getUserDir())) {
                return false;
            } else {
                return this.equals(this.m_javaClasspath, _o.getJavaClasspath());
            }
        } else {
            return false;
        }
    }

    public String getJavaClasspath() {
        return this.m_javaClasspath;
    }

    public String getJavaVersion() {
        return this.m_javaVersion;
    }

    public long getStartTime() {
        return this.m_startTime;
    }

    public long getUpTime() {
        return this.m_upTime;
    }

    public String getUserDir() {
        return this.m_userDir;
    }

    public String getUserName() {
        return this.m_userName;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = hash * 31 + (int) (this.m_startTime ^ this.m_startTime >>> 32);
        hash = hash * 31 + (int) (this.m_upTime ^ this.m_upTime >>> 32);
        hash = hash * 31 + (this.m_javaVersion == null ? 0 : this.m_javaVersion.hashCode());
        hash = hash * 31 + (this.m_userName == null ? 0 : this.m_userName.hashCode());
        hash = hash * 31 + (this.m_userDir == null ? 0 : this.m_userDir.hashCode());
        hash = hash * 31 + (this.m_javaClasspath == null ? 0 : this.m_javaClasspath.hashCode());
        return hash;
    }

    @Override
    public void mergeAttributes(RuntimeInfo other) {
        this.m_startTime = other.getStartTime();
        this.m_upTime = other.getUpTime();
        if (other.getJavaVersion() != null) {
            this.m_javaVersion = other.getJavaVersion();
        }
        if (other.getUserName() != null) {
            this.m_userName = other.getUserName();
        }
    }

    public RuntimeInfo setJavaClasspath(String javaClasspath) {
        this.m_javaClasspath = javaClasspath;
        return this;
    }

    public RuntimeInfo setJavaVersion(String javaVersion) {
        this.m_javaVersion = javaVersion;
        return this;
    }

    public RuntimeInfo setStartTime(long startTime) {
        this.m_startTime = startTime;
        return this;
    }

    public RuntimeInfo setUpTime(long upTime) {
        this.m_upTime = upTime;
        return this;
    }

    public RuntimeInfo setUserDir(String userDir) {
        this.m_userDir = userDir;
        return this;
    }

    public RuntimeInfo setUserName(String userName) {
        this.m_userName = userName;
        return this;
    }
}
