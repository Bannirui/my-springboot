package com.github.bannirui.msb.web.config;

import com.github.bannirui.msb.ex.FrameworkException;
import com.github.bannirui.msb.plugin.PluginConfigManager;
import com.github.bannirui.msb.plugin.PluginDecorator;
import com.github.bannirui.msb.register.AbstractBeanRegistrar;
import com.github.bannirui.msb.register.BeanDefinition;
import com.github.bannirui.msb.web.filter.TitansSSOFilter;
import com.github.bannirui.msb.web.filter.ZfeFilter;
import com.github.bannirui.msb.web.session.web.TitansCookieSerializer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@EnableWebSecurity
@EnableGlobalMethodSecurity(
    prePostEnabled = true
)
public class SSOConfiguration extends AbstractBeanRegistrar {
    private static final String END_FILTER_NAME = "com.zto.titans.endpoint.web.EndpointFilter";
    private static final String SAME_SITE_NONE = "titans.web.sameSiteNone";
    private static Environment environment;

    @Override
    public void registerBeans() {
        this.registerBeanDefinitionIfNotExists(BeanDefinition.newInstance(PermissionService.class).setBeanName("permissionService"));
        Set<String> ssoFilterSet = PluginConfigManager.getPropertyValueSet("sso.filter");
        String titansSSOFilterName = null;
        if (ssoFilterSet != null && !ssoFilterSet.isEmpty()) {
            titansSSOFilterName = ssoFilterSet.iterator().next();
        }
        if (StringUtils.isEmpty(titansSSOFilterName)) {
            titansSSOFilterName = "com.github.bannirui.msb.web.filter.LoginAndLogoutFilter";
        }
        try {
            TitansSSOFilter titansSSOFilter = (TitansSSOFilter)Class.forName(titansSSOFilterName).newInstance();
            ZfeFilter zfeFilter = (ZfeFilter)Class.forName("com.github.bannirui.msb.web.filter.ZfeFilter").newInstance();
            this.registerBeanDefinitionIfNotExists(BeanDefinition.newInstance(SSOConfig.class).addPropertyValue("indexUrl", this.getProperty("sso.indexUrl")).addPropertyValue("ssoUrl", this.getProperty("sso.ssoUrl")).addPropertyValue("url", this.getProperty("sso.url")).addPropertyValue("appId", this.getProperty("sso.appId")).addPropertyValue("secret", this.getProperty("sso.secret")).addPropertyValue("loginResource", this.getProperty("sso.loginResource")).addPropertyValue("tokenResource", this.getProperty("sso.tokenResource")).addPropertyValue("zfeTokenResource", this.getProperty("sso.zfeTokenResource")).addPropertyValue("userResource", this.getProperty("sso.userResource")).addPropertyValue("staticResource", this.getProperty("sso.staticResource")).addPropertyValue("maxInactiveInterval", this.getProperty("sso.maxInactiveInterval")).addPropertyValue("scope", this.getProperty("sso.scope")).addPropertyValue("menuUrl", this.getProperty("sso.menuUrl")).addPropertyValue("ssoRedirectUrl", this.getProperty("sso.redirectUrl")).addPropertyValue("forceHttps", this.getPropertyAsBoolean("sso.forceHttps")).addPropertyValue("corsAllowedOrigins", this.getProperty("sso.cors.allowedOrigins")).addPropertyValue("corsAllowedMethod", this.getProperty("sso.cors.allowedMethod")).addPropertyValue("corsPath", this.getProperty("sso.cors.path")).addPropertyValue("ssoRedirectUrlOrUri", this.getProperty("sso.redirect.urloruri")).addPropertyValue("ssoRedirectDomains", this.getProperty("sso.redirect.domains")).addPropertyValue("permHistoryUrl", this.getProperty("sso.permHistoryUrl")).addPropertyValue("permissionFetchUrl", this.getProperty("sso.permissionFetchUrl")).addPropertyValue("ssoLogLevelString", this.getProperty("sso.logLevel")).addPropertyValue("ssoSessionMaxLife", this.getProperty("sso.sessionMaxLifeTime")).addPropertyValue("enableZfe", this.getPropertyAsBoolean("sso.enableZfe")).addPropertyValue("titansSSOFilter", titansSSOFilter).addPropertyValue("zfeFilter", zfeFilter));
        } catch (Exception e) {
            throw FrameworkException.getInstance(e, "TitansSSOFilter 加载失败， 相关处理类为[{0}]!", titansSSOFilterName);
        }
        this.registerBeanDefinitionIfNotExists(BeanDefinition.newInstance(GlobalExceptionHandler.class));
        List<PluginDecorator<Class<?>>> filterClasses = PluginConfigManager.getOrderedPluginClasses(Filter.class.getName(), true);
        int i = 0;
        for (PluginDecorator<Class<?>> pd : filterClasses) {
            try {
                Class clazz = pd.getPlugin();
                String filter = clazz.getName();
                int order = pd.getOrder();
                if (filter.equals("com.github.bannirui.msb.endpoint.web.EndpointFilter")) {
                    LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap<>();
                    linkedHashMap.put("endpoint.check.key", StringUtils.isEmpty(this.getProperty("endpoint.check.key")) ? "" : this.getProperty("titans.endpoint.check.key"));
                    this.registerSingleFilterWithParameterAndOrder(clazz, linkedHashMap, i++);
                } else if (order != 0) {
                    this.registerSingleFilterWithOrder(clazz, order);
                } else {
                    this.registerSingleFilterWithOrder(clazz, i++);
                }
            } catch (Exception e) {
                throw FrameworkException.getInstance(e, "Web Filter插件加载异常,请检查[{0}]", "javax.servlet.Filter");
            }
        }

    }

    @Override
    public void setEnvironment(Environment env) {
        super.setEnvironment(env);
        environment = env;
    }

    public static Environment getEnvironment() {
        return environment;
    }

    private void registerSingleFilterWithOrder(Class clazz, int order) throws Exception {
        String className = clazz.getName();
        this.registerBeanDefinitionIfNotExists(BeanDefinition.newInstance(FilterRegistrationBean.class).addPropertyValue("filter", clazz.newInstance()).addPropertyValue("urlPatterns", Lists.newArrayList(new String[]{"/*"})).addPropertyValue("name", className.substring(className.lastIndexOf(".") + 1)).addPropertyValue("order", order).setBeanName(className));
    }

    private void registerSingleFilterWithParameterAndOrder(Class clazz, LinkedHashMap<String, String> initParameters, int order) throws Exception {
        String className = clazz.getName();
        this.registerBeanDefinitionIfNotExists(BeanDefinition.newInstance(FilterRegistrationBean.class).addPropertyValue("filter", clazz.newInstance()).addPropertyValue("urlPatterns", Lists.newArrayList(new String[]{"/*"})).addPropertyValue("name", className.substring(className.lastIndexOf(".") + 1)).addPropertyValue("initParameters", initParameters).addPropertyValue("order", order).setBeanName(className));
    }

    @Bean
    public CookieSerializer httpSessionIdResolver() {
        boolean sameSiteNone = this.getPropertyAsBoolean("titans.web.sameSiteNone", false);
        TitansCookieSerializer cookieSerializer = new TitansCookieSerializer();
        cookieSerializer.setCookieName("SESSION");
        cookieSerializer.setUseHttpOnlyCookie(false);
        cookieSerializer.setSameSiteNone(sameSiteNone);
        return cookieSerializer;
    }
}
