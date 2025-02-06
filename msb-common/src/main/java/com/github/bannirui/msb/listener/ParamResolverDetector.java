package com.github.bannirui.msb.listener;

import com.github.bannirui.msb.env.MsbEnvironmentMgr;
import com.github.bannirui.msb.listener.param.SpringParamResolver;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ParamResolverDetector {
    private static final Log logger = LogFactory.getLog(ParamResolverDetector.class);
    private static List<SpringParamResolver> spring_param_resolver_list = new ArrayList<>();

    public static List<SpringParamResolver> getSpringParamResolverList() {
        return spring_param_resolver_list;
    }

    static {
        String springParamResolvers = MsbEnvironmentMgr.getProperty("listener.param.resolvers");
        if (StringUtils.isNotEmpty(springParamResolvers)) {
            String[] paramResolverClassNames = springParamResolvers.split(",");
            for (String paramResolverClassName : paramResolverClassNames) {
                try {
                    Class<?> paramResolverClass = Class.forName(paramResolverClassName);
                    spring_param_resolver_list.add((SpringParamResolver) paramResolverClass.newInstance());
                } catch (Exception e) {
                    logger.error(String.format("ParamResolver class[%s] init error!", paramResolverClassName), e);
                }
            }
        }
    }
}
