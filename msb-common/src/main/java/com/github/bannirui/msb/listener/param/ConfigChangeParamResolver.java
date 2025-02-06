package com.github.bannirui.msb.listener.param;

import com.github.bannirui.msb.properties.ConfigChange;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

public class ConfigChangeParamResolver implements SpringParamResolver {

    @Override
    public List<String> attentionKeys(Parameter parameter) {
        List<String> result = new ArrayList<>();
        result.add("__all");
        return result;
    }

    @Override
    public Object resolveParameter(Parameter parameter, String paramName, ConfigChange configChange, Set<String> changedKeys, Environment environment,
                                   ApplicationContext applicationContext)
        throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return configChange;
    }

    @Override
    public boolean isSupport(Parameter parameter) {
        return parameter.getType().isAssignableFrom(ConfigChange.class);
    }
}
