package com.github.bannirui.msb.log.configuration;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.PropertyDefiner;
import ch.qos.logback.core.status.Status;
import com.github.bannirui.msb.common.env.EnvironmentMgr;
import com.github.bannirui.msb.common.util.StringUtil;
import java.io.File;

public class DynamicProperty implements PropertyDefiner {

    private static final String DEFAULT_ROOT_PATH = "/data/logs/";
    private static String curRootPath;

    static {
        if (checkAndMkDirs(DEFAULT_ROOT_PATH)) {
            curRootPath = DEFAULT_ROOT_PATH;
        } else {
            String userPath = System.getProperty("user.home") + "/logs";
            if (checkAndMkDirs(userPath)) {
                curRootPath = userPath;
            } else if (StringUtil.isNotEmpty(System.getProperty("msb.log.path.root"))) {
                curRootPath = System.getProperty("msb.log.path.root").trim();
            } else {
                curRootPath = userPath;
            }
        }
        if (!"/".equals(curRootPath.substring(curRootPath.length() - 1))) {
            curRootPath = curRootPath + "/";
        }
    }

    public DynamicProperty() {
    }

    @Override
    public String getPropertyValue() {
        String appName = EnvironmentMgr.getAppName();
        return curRootPath + appName;
    }

    @Override
    public void setContext(Context context) {

    }

    @Override
    public Context getContext() {
        return null;
    }

    @Override
    public void addStatus(Status status) {

    }

    @Override
    public void addInfo(String s) {

    }

    @Override
    public void addInfo(String s, Throwable throwable) {

    }

    @Override
    public void addWarn(String s) {

    }

    @Override
    public void addWarn(String s, Throwable throwable) {

    }

    @Override
    public void addError(String s) {

    }

    @Override
    public void addError(String s, Throwable throwable) {

    }

    private static boolean checkAndMkDirs(String dir) {
        File f = new File(dir);
        return f.exists() ? true : f.mkdirs();
    }
}
