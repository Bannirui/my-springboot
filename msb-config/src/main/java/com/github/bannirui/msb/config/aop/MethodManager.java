package com.github.bannirui.msb.config.aop;

import com.github.bannirui.msb.common.enums.ExceptionEnum;
import com.github.bannirui.msb.common.ex.ErrorCodeException;
import com.github.bannirui.msb.common.util.ArrayUtil;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.util.ReflectionUtils;

public class MethodManager {
    private static final Map<String, Method> METHOD_MAP = new ConcurrentHashMap();

    public MethodManager() {
    }

    public static Method findMethod(Class<?> clazz, String methodName, Class<?>[] paramsClass) {
        if (methodName == null) {
            throw new ErrorCodeException(ExceptionEnum.PARAM_EXCEPTION, new Object[] {"反射类方法", methodName});
        } else {
            String key = generateKey(clazz, methodName, paramsClass);
            if (METHOD_MAP.containsKey(key)) {
                return (Method) METHOD_MAP.get(key);
            } else {
                Method method = ReflectionUtils.findMethod(clazz, methodName, paramsClass);
                if (method == null) {
                    throw new ErrorCodeException(ExceptionEnum.PARAM_EXCEPTION, new Object[] {"反射类方法", methodName});
                } else {
                    METHOD_MAP.putIfAbsent(key, method);
                    return (Method) METHOD_MAP.get(key);
                }
            }
        }
    }

    public static String generateKey(Class<?> clazz, String methodName, Class<?>[] paramsClass) {
        StringBuffer sb = new StringBuffer();
        sb.append(clazz.getName());
        sb.append(".");
        sb.append(methodName);
        sb.append("(");
        if (!ArrayUtil.isEmpty(paramsClass)) {
            for (Class<?> c : paramsClass) {
                sb.append(c.getName());
                sb.append(",");
            }
        }
        sb.append(")");
        return sb.toString();
    }
}
