package com.github.bannirui.msb.web.config;

import com.github.bannirui.msb.ex.FrameworkException;
import com.github.bannirui.msb.plugin.PluginConfigManager;
import com.github.bannirui.msb.plugin.PluginDecorator;
import com.github.bannirui.msb.register.AbstractBeanRegistrar;
import com.github.bannirui.msb.register.BeanDefinition;
import com.github.bannirui.msb.web.filter.LoginAndLogoutFilter;
import com.github.bannirui.msb.web.filter.SSOFilter;
import com.github.bannirui.msb.web.filter.ZfeFilter;
import com.github.bannirui.msb.web.session.web.TitansCookieSerializer;
import jakarta.servlet.Filter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.session.web.http.CookieSerializer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

@EnableWebSecurity
@EnableGlobalMethodSecurity(
    prePostEnabled = true
)
public class SSOConfiguration extends AbstractBeanRegistrar {
    private static final String END_FILTER_NAME = "com.github.bannirui.msb.endpoint.web.EndpointFilter";
    private static final String SAME_SITE_NONE = "msb.web.sameSiteNone";
    private static Environment environment;

    @Override
    public void registerBeans() {
        this.registerBeanDefinitionIfNotExists(BeanDefinition.newInstance(PermissionService.class).setBeanName("permissionService"));
        Set<String> ssoFilterSet = PluginConfigManager.getPropertyValueSet("sso.filter");
        String ssoFilterName = null;
        if (ssoFilterSet != null && !ssoFilterSet.isEmpty()) {
            ssoFilterName = ssoFilterSet.iterator().next();
        }
        if (StringUtils.isEmpty(ssoFilterName)) {
            ssoFilterName = LoginAndLogoutFilter.class.getName();
        }
        try {
            SSOFilter ssoFilter = (SSOFilter)Class.forName(ssoFilterName).newInstance();
            ZfeFilter zfeFilter = (ZfeFilter)Class.forName(ZfeFilter.class.getName()).newInstance();
            super.registerBeanDefinitionIfNotExists(BeanDefinition.newInstance(SSOConfig.class)
                    .addPropertyValue("indexUrl", super.getProperty("sso.indexUrl"))
                    .addPropertyValue("ssoUrl", super.getProperty("sso.ssoUrl"))
                    .addPropertyValue("url", super.getProperty("sso.url"))
                    .addPropertyValue("appId", super.getProperty("sso.appId"))
                    .addPropertyValue("secret", super.getProperty("sso.secret"))
                    .addPropertyValue("loginResource", super.getProperty("sso.loginResource"))
                    .addPropertyValue("tokenResource", super.getProperty("sso.tokenResource"))
                    .addPropertyValue("zfeTokenResource", super.getProperty("sso.zfeTokenResource"))
                    .addPropertyValue("userResource", super.getProperty("sso.userResource"))
                    .addPropertyValue("staticResource", super.getProperty("sso.staticResource"))
                    .addPropertyValue("maxInactiveInterval", super.getProperty("sso.maxInactiveInterval"))
                    .addPropertyValue("scope", super.getProperty("sso.scope"))
                    .addPropertyValue("menuUrl", super.getProperty("sso.menuUrl"))
                    .addPropertyValue("ssoRedirectUrl", super.getProperty("sso.redirectUrl"))
                    .addPropertyValue("forceHttps", super.getPropertyAsBoolean("sso.forceHttps"))
                    .addPropertyValue("corsAllowedOrigins", super.getProperty("sso.cors.allowedOrigins"))
                    .addPropertyValue("corsAllowedMethod", super.getProperty("sso.cors.allowedMethod"))
                    .addPropertyValue("corsPath", super.getProperty("sso.cors.path"))
                    .addPropertyValue("ssoRedirectUrlOrUri", super.getProperty("sso.redirect.urloruri"))
                    .addPropertyValue("ssoRedirectDomains", super.getProperty("sso.redirect.domains"))
                    .addPropertyValue("permHistoryUrl", super.getProperty("sso.permHistoryUrl"))
                    .addPropertyValue("permissionFetchUrl", super.getProperty("sso.permissionFetchUrl"))
                    .addPropertyValue("ssoLogLevelString", super.getProperty("sso.logLevel"))
                    .addPropertyValue("ssoSessionMaxLife", super.getProperty("sso.sessionMaxLifeTime"))
                    .addPropertyValue("enableZfe", super.getPropertyAsBoolean("sso.enableZfe"))
                    .addPropertyValue("ssoFilter", ssoFilter)
                    .addPropertyValue("zfeFilter", zfeFilter));
        } catch (Exception e) {
            throw FrameworkException.getInstance(e, "SSOFilter加载失败 相关处理类为[{0}]!", ssoFilterName);
        }
        super.registerBeanDefinitionIfNotExists(BeanDefinition.newInstance(GlobalExceptionHandler.class));
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
                throw FrameworkException.getInstance(e, "Web Filter插件加载异常 请检查[{0}]", "javax.servlet.Filter");
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
        this.registerBeanDefinitionIfNotExists(BeanDefinition.newInstance(FilterRegistrationBean.class)
                .addPropertyValue("filter", clazz.newInstance())
                .addPropertyValue("urlPatterns", new ArrayList<String>(){{add("/*");}})
                .addPropertyValue("name", className.substring(className.lastIndexOf(".") + 1))
                .addPropertyValue("order", order).setBeanName(className));
    }

    private void registerSingleFilterWithParameterAndOrder(Class clazz, LinkedHashMap<String, String> initParameters, int order) throws Exception {
        String className = clazz.getName();
        this.registerBeanDefinitionIfNotExists(BeanDefinition.newInstance(FilterRegistrationBean.class).addPropertyValue("filter", clazz.newInstance())
                .addPropertyValue("urlPatterns", new ArrayList<String>(){{add("/*");}})
                .addPropertyValue("name", className.substring(className.lastIndexOf(".") + 1))
                .addPropertyValue("initParameters", initParameters)
                .addPropertyValue("order", order).setBeanName(className));
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
