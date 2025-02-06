package com.github.bannirui.msb.listener.param;

import com.github.bannirui.msb.properties.ConfigChange;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Set;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

public interface SpringParamResolver {
    String ATTENTION_ALL_KEYS = "__all";

    List<String> attentionKeys(Parameter parameter);

    Object resolveParameter(Parameter parameter, String paramName, ConfigChange configChange, Set<String> changedKeys, Environment environment,
                            ApplicationContext applicationContext)
        throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException;

    boolean isSupport(Parameter parameter);

}
