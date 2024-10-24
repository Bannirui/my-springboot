package com.github.bannirui.msb.common.listener;

import com.github.bannirui.msb.common.env.EnvironmentMgr;
import com.github.bannirui.msb.common.listener.param.SpringParamResolver;
import com.github.bannirui.msb.common.util.StringUtil;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ParamResolverDetector {
    private static final Log logger = LogFactory.getLog(ParamResolverDetector.class);
    private static List<SpringParamResolver> springParamResolverList = new ArrayList();

    public ParamResolverDetector() {
    }

    public static List<SpringParamResolver> getSpringParamResolverList() {
        return springParamResolverList;
    }

    static {
        String springParamResolvers = EnvironmentMgr.getProperty("listener.param.resolvers");
        if (StringUtil.isNotEmpty(springParamResolvers)) {
            String[] paramResolverClassNames = springParamResolvers.split(",");
            for (String paramResolverClassName : paramResolverClassNames) {
                try {
                    Class<?> paramResolverClass = Class.forName(paramResolverClassName);
                    springParamResolverList.add((SpringParamResolver) paramResolverClass.newInstance());
                } catch (Exception e) {
                    logger.error(String.format("ParamResolver class[%s] init error!", paramResolverClassName), e);
                }
            }
        }
    }
}
