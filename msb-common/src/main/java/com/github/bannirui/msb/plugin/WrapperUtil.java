package com.github.bannirui.msb.plugin;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Objects;
import org.springframework.util.Assert;

public class WrapperUtil {

    private static final String WRAPPER_FILE_NAME = "com.github.bannirui.msb.common.plugin.Wrapper";

    public WrapperUtil() {
    }

    public static <T> T getWrapperObj(Class<T> clz, Class[] argTypes, Object[] args, String typePrefix) throws Exception {
        T target = null;
        Class[] wrapperArgTypes = null;
        Object[] wrapperArgs = null;
        if (argTypes != null && argTypes.length > 0 && args != null && args.length > 0) {
            Assert.isTrue(args.length == argTypes.length, "argumentTypes cnt is not equal to args cnt");
            target = InterceptorUtil.getProxyObj(clz, argTypes, args, typePrefix);
            wrapperArgTypes = new Class[argTypes.length + 1];
            System.arraycopy(argTypes, 0, wrapperArgTypes, 0, argTypes.length);
            wrapperArgTypes[wrapperArgTypes.length - 1] = clz;
            wrapperArgs = new Object[args.length + 1];
            System.arraycopy(args, 0, wrapperArgs, 0, args.length);
            wrapperArgs[wrapperArgs.length - 1] = target;
            return wrapperObj(clz, wrapperArgTypes, wrapperArgs, typePrefix);
        } else {
            return getWrapperObj(clz, typePrefix);
        }
    }

    public static <T> T getWrapperObj(Class<T> clz, String typePrefix) throws Exception {
        T target = InterceptorUtil.getProxyObj(clz, typePrefix);
        Class[] wrapperArgTypes = new Class[] {clz};
        Object[] wrapperArgs = new Object[] {target};
        return wrapperObj(clz, wrapperArgTypes, wrapperArgs, typePrefix);
    }

    private static <T> T wrapperObj(Class clz, Class[] argTypes, Object[] args, String typePrefix) throws Exception {
        List<PluginDecorator<Class<?>>> wrapperDecorators =
            PluginConfigManager.getOrderedPluginClasses("com.github.bannirui.msb.common.plugin.Wrapper", typePrefix, true);
        if (Objects.isNull(wrapperDecorators) || wrapperDecorators.isEmpty()) {
            return (T) args[args.length - 1];
        }
        Object tmp = null;
        for (PluginDecorator<Class<?>> pd : wrapperDecorators) {
            Class tClass = pd.getPlugin();
            Constructor constructor = tClass.getConstructor(argTypes);
            tmp = constructor.newInstance(args);
            args[args.length - 1] = tmp;
        }
        return (T) tmp;
    }
}
