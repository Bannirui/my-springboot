package com.github.bannirui.msb.common;

import com.github.bannirui.msb.common.env.EnvironmentMgr;
import com.github.bannirui.msb.common.util.StringUtil;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.core.env.AbstractEnvironment;

public class MsbLoggingEnvironment extends AbstractEnvironment {
    public MsbLoggingEnvironment() {
    }

    @Override
    protected Set<String> doGetActiveProfiles() {
        Set<String> activeProfiles = new LinkedHashSet<>();
        if (StringUtil.isNotEmpty(EnvironmentMgr.getEnv())) {
            activeProfiles.add(EnvironmentMgr.getEnv());
        }
        if (StringUtil.isNotEmpty(System.getProperty("spring.profiles.active"))) {
            activeProfiles.add(System.getProperty("spring.profiles.active"));
        }
        return activeProfiles;
    }
}
