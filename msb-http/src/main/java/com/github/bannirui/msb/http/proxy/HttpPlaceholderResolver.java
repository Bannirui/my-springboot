package com.github.bannirui.msb.http.proxy;

import com.github.bannirui.msb.http.annotation.HttpPlaceholderValue;
import com.github.bannirui.msb.http.util.ApplicationContextUtil;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Properties;
import org.apache.commons.collections4.MapUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.util.PropertyPlaceholderHelper;

public class HttpPlaceholderResolver {
    private PropertyPlaceholderHelper propertyPlaceholderHelper = new PropertyPlaceholderHelper("${", "}");

    public Properties getHttpMethodPlaceholderParamValue(Method method, Object[] args) {
        Properties properties = new Properties();
        if (args != null && args.length > 0) {
            Parameter[] parameters = method.getParameters();
            for(int i = 0,sz=parameters.length; i < sz; ++i) {
                HttpPlaceholderValue httpPlaceholderValueAnnotation = parameters[i].getAnnotation(HttpPlaceholderValue.class);
                if (httpPlaceholderValueAnnotation != null && args[i] != null) {
                    properties.put(httpPlaceholderValueAnnotation.value(), args[i].toString());
                }
            }
        }
        return properties;
    }

    public String replacePlaceholders(String url, Properties properties) {
        String newUrl = url;
        if (MapUtils.isNotEmpty(properties)) {
            newUrl = this.propertyPlaceholderHelper.replacePlaceholders(url, properties);
        }
        ApplicationContext applicationContext = ApplicationContextUtil.getApplicationContext();
        if (null != applicationContext) {
            newUrl = applicationContext.getEnvironment().resolveRequiredPlaceholders(newUrl);
        }
        return newUrl;
    }
}
