package com.github.bannirui.msb.mq.sdk.common;

import java.util.List;
import java.util.Map;

public class RunCommonConfig {
    private String programName;
    private Map<String, String> environment;
    private List<String> args;
    private String command;
    private String libDir;
    private String autoRestart;
    private String exitCodes;
    private Integer startSecs;
    private Integer stopWaitSecs;
    private Boolean killAsGroup;
    private String stdoutLogfileMaxBytes;
    private String stdoutLogfileBackups;
    private String stderrLogfileMaxBytes;
    private String stderrLogfileBackups;
    private String stdoutLogfile;
    private String stderrLogfile;
    private String user;
    private String group;
    private Boolean stopAsGroup;
    private Integer startRetries;
    private String stopSignal;
    private Boolean redirectStderr;
    private Integer stdoutCaptureMaxBytes;
    private Boolean stdoutEventsEnabled;
    private Boolean stdoutSyslog;
    private Integer stderrCaptureMaxBytes;
    private Boolean stderrEventsEnabled;
    private Boolean stderrSyslog;

    public String getProgramName() {
        return this.programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public Map<String, String> getEnvironment() {
        return this.environment;
    }

    public void setEnvironment(Map<String, String> environment) {
        this.environment = environment;
    }

    public List<String> getArgs() {
        return this.args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

    public String getCommand() {
        return this.command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getLibDir() {
        return this.libDir;
    }

    public void setLibDir(String libDir) {
        this.libDir = libDir;
    }

    public String getAutoRestart() {
        return this.autoRestart;
    }

    public void setAutoRestart(String autoRestart) {
        this.autoRestart = autoRestart;
    }

    public String getExitCodes() {
        return this.exitCodes;
    }

    public void setExitCodes(String exitCodes) {
        this.exitCodes = exitCodes;
    }

    public Integer getStartSecs() {
        return this.startSecs;
    }

    public void setStartSecs(Integer startSecs) {
        this.startSecs = startSecs;
    }

    public Integer getStopWaitSecs() {
        return this.stopWaitSecs;
    }

    public void setStopWaitSecs(Integer stopWaitSecs) {
        this.stopWaitSecs = stopWaitSecs;
    }

    public Boolean getKillAsGroup() {
        return this.killAsGroup;
    }

    public void setKillAsGroup(Boolean killAsGroup) {
        this.killAsGroup = killAsGroup;
    }

    public Boolean getStopAsGroup() {
        return this.stopAsGroup;
    }

    public void setStopAsGroup(Boolean stopAsGroup) {
        this.stopAsGroup = stopAsGroup;
    }

    public Integer getStartRetries() {
        return this.startRetries;
    }

    public void setStartRetries(Integer startRetries) {
        this.startRetries = startRetries;
    }

    public String getStopSignal() {
        return this.stopSignal;
    }

    public void setStopSignal(String stopSignal) {
        this.stopSignal = stopSignal;
    }

    public Boolean getRedirectStderr() {
        return this.redirectStderr;
    }

    public void setRedirectStderr(Boolean redirectStderr) {
        this.redirectStderr = redirectStderr;
    }

    public Integer getStdoutCaptureMaxBytes() {
        return this.stdoutCaptureMaxBytes;
    }

    public void setStdoutCaptureMaxBytes(Integer stdoutCaptureMaxBytes) {
        this.stdoutCaptureMaxBytes = stdoutCaptureMaxBytes;
    }

    public Boolean getStdoutEventsEnabled() {
        return this.stdoutEventsEnabled;
    }

    public void setStdoutEventsEnabled(Boolean stdoutEventsEnabled) {
        this.stdoutEventsEnabled = stdoutEventsEnabled;
    }

    public Boolean getStdoutSyslog() {
        return this.stdoutSyslog;
    }

    public void setStdoutSyslog(Boolean stdoutSyslog) {
        this.stdoutSyslog = stdoutSyslog;
    }

    public Integer getStderrCaptureMaxBytes() {
        return this.stderrCaptureMaxBytes;
    }

    public void setStderrCaptureMaxBytes(Integer stderrCaptureMaxBytes) {
        this.stderrCaptureMaxBytes = stderrCaptureMaxBytes;
    }

    public Boolean getStderrEventsEnabled() {
        return this.stderrEventsEnabled;
    }

    public void setStderrEventsEnabled(Boolean stderrEventsEnabled) {
        this.stderrEventsEnabled = stderrEventsEnabled;
    }

    public Boolean getStderrSyslog() {
        return this.stderrSyslog;
    }

    public void setStderrSyslog(Boolean stderrSyslog) {
        this.stderrSyslog = stderrSyslog;
    }

    public String getUser() {
        return this.user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getStdoutLogfileMaxBytes() {
        return this.stdoutLogfileMaxBytes;
    }

    public void setStdoutLogfileMaxBytes(String stdoutLogfileMaxBytes) {
        this.stdoutLogfileMaxBytes = stdoutLogfileMaxBytes;
    }

    public String getStdoutLogfileBackups() {
        return this.stdoutLogfileBackups;
    }

    public void setStdoutLogfileBackups(String stdoutLogfileBackups) {
        this.stdoutLogfileBackups = stdoutLogfileBackups;
    }

    public String getStderrLogfileMaxBytes() {
        return this.stderrLogfileMaxBytes;
    }

    public void setStderrLogfileMaxBytes(String stderrLogfileMaxBytes) {
        this.stderrLogfileMaxBytes = stderrLogfileMaxBytes;
    }

    public String getStderrLogfileBackups() {
        return this.stderrLogfileBackups;
    }

    public void setStderrLogfileBackups(String stderrLogfileBackups) {
        this.stderrLogfileBackups = stderrLogfileBackups;
    }

    public String getStdoutLogfile() {
        return this.stdoutLogfile;
    }

    public void setStdoutLogfile(String stdoutLogfile) {
        this.stdoutLogfile = stdoutLogfile;
    }

    public String getStderrLogfile() {
        return this.stderrLogfile;
    }

    public void setStderrLogfile(String stderrLogfile) {
        this.stderrLogfile = stderrLogfile;
    }

    public String getGroup() {
        return this.group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
