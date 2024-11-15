package com.github.bannirui.msb.common.listener.param;

import com.github.bannirui.msb.common.properties.ConfigChange;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Set;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

public class ApplicationContextParamResolver implements SpringParamResolver {

    public ApplicationContextParamResolver() {
    }

    @Override
    public List<String> attentionKeys(Parameter parameter) {
        return null;
    }

    @Override
    public Object resolveParameter(Parameter parameter, String paramName, ConfigChange configChange, Set<String> changedKeys, Environment environment,
                                   ApplicationContext applicationContext)
        throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return applicationContext;
    }

    @Override
    public boolean isSupport(Parameter parameter) {
        return parameter.getType().isAssignableFrom(ApplicationContext.class);
    }
}