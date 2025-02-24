package com.github.bannirui.msb.web.config;

import com.alibaba.fastjson.JSONObject;
import com.github.bannirui.msb.entity.Result;
import com.github.bannirui.msb.enums.ExceptionEnum;
import com.github.bannirui.msb.ex.ErrorCodeException;
import com.github.bannirui.msb.web.filter.SSOFilter;
import com.github.bannirui.msb.web.filter.ZfeFilter;
import com.github.bannirui.msb.web.util.RequestUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 各成员由msb配置文件指定 前缀是sso
 */
public class SSOConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {
    private static Logger log = LoggerFactory.getLogger(SSOConfig.class);

    // sso.indexUrl
    private String indexUrl;
    // sso.ssoUrl
    private String ssoUrl;
    // sso.url
    private String url;
    // sso.appId
    private String appId;
    // sso.secret
    private String secret;
    // sso.loginResource
    private String loginResource;
    // sso.tokenResource
    private String tokenResource;
    // sso.zfeTokenResource
    private String zfeTokenResource;
    // sso.userResource
    private String userResource;
    // sso.staticResource
    private String staticResource;
    // sso.maxInactiveInterval
    private Integer maxInactiveInterval;
    // sso.scope
    private String scope;
    // sso.menuUrl
    private String menuUrl;
    // sso.redirectUrl
    private String ssoRedirectUrl;
    // sso.forceHttps
    private Boolean forceHttps;
    // sso.cors.allowedOrigins
    private String corsAllowedOrigins;
    // sso.cors.allowedMethod
    private String corsAllowedMethod;
    // sso.cors.path
    private String corsPath;
    // sso.redirect.urloruri
    private String ssoRedirectUrlOrUri;
    private SSOFilter SSOFilter;
    private ZfeFilter zfeFilter;
    // sso.redicret.domains
    private String ssoRedirectDomains;
    private String ssoLogLevelString;
    // sso.permHistoryUrl
    private String permHistoryUrl;
    // sso.permissionFetchUrl
    private String permissionFetchUrl;
    // sso.sessionMaxLifeTime
    private Integer ssoSessionMaxLife;
    // sso.enableZfe
    private Boolean enableZfe;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        if (this.getSSOFilter() == null) {
            throw new ErrorCodeException(ExceptionEnum.PARAM_EXCEPTION, "SSO", "ssoFilter is empty!");
        }
        String[] pathArray;
        if (this.getCorsAllowedOrigins() != null && this.getCorsAllowedMethod() != null && this.getCorsPath() != null) {
            CorsConfiguration configuration = new CorsConfiguration();
            configuration.setAllowedOrigins(Arrays.asList(this.getCorsAllowedOrigins().split(",")));
            configuration.setAllowedMethods(Arrays.asList(this.getCorsAllowedMethod().split(",")));
            configuration.applyPermitDefaultValues();
            configuration.setAllowCredentials(true);
            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            String[] paths = this.getCorsPath().split(",");
            for (String path : paths) {
                source.registerCorsConfiguration(path, configuration);
            }
            http.cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(source));
        }
        if (!StringUtils.isEmpty(this.getAppId()) && !StringUtils.isEmpty(this.getSecret())) {
            if (StringUtils.isEmpty(this.getSsoUrl())) {
                throw new ErrorCodeException(ExceptionEnum.PARAM_EXCEPTION, "SSO", "ssoUrl=[" + this.getSsoUrl() + "]");
            }
            String logoutRedirectUrl;
            if (this.ssoUrl.contains("test")) {
                logoutRedirectUrl = "/logout-test.html";
            } else {
                logoutRedirectUrl = "/logout.html";
            }
            if (StringUtils.isEmpty(this.loginResource)) {
                throw new ErrorCodeException(ExceptionEnum.PARAM_EXCEPTION, "SSO", "loginResource=[" + this.loginResource + "]");
            } else if (StringUtils.isEmpty(this.getTokenResource())) {
                throw new ErrorCodeException(ExceptionEnum.PARAM_EXCEPTION, "SSO", "tokenResource=[" + this.getTokenResource() + "]");
            } else if (StringUtils.isEmpty(this.getUserResource())) {
                throw new ErrorCodeException(ExceptionEnum.PARAM_EXCEPTION, "SSO", "userResource=[" + this.getUserResource() + "]");
            } else if (StringUtils.isEmpty(this.getStaticResource())) {
                throw new ErrorCodeException(ExceptionEnum.PARAM_EXCEPTION, "SSO", "staticResource=[" + this.getStaticResource() + "]");
            } else if (this.getMaxInactiveInterval() == null) {
                throw new ErrorCodeException(ExceptionEnum.PARAM_EXCEPTION, "SSO", "maxInactiveInterval=[" + this.getMaxInactiveInterval() + "]");
            } else if (StringUtils.isEmpty(this.getMenuUrl())) {
                throw new ErrorCodeException(ExceptionEnum.PARAM_EXCEPTION, "SSO", "menuUrl=[" + this.getMenuUrl() + "]");
            } else {
                Set<String> ssoRedirectDomainSet = new HashSet<>();
                if (StringUtils.isNotEmpty(this.getSsoRedirectDomains())) {
                    ssoRedirectDomainSet = new HashSet<>(Arrays.asList(this.getSsoRedirectDomains().split(";")));
                }
                this.staticResource = "/logout,/login," + logoutRedirectUrl + "," + this.staticResource;
                HttpSessionSecurityContextRepository httpSessionSecurityContextRepository = new HttpSessionSecurityContextRepository();
                pathArray = this.getStaticResource().split(",");
                this.getSSOFilter().setAppId(this.getAppId());
                this.getSSOFilter().setSecret(this.getSecret());
                this.getSSOFilter().setSsoUrl(this.getSsoUrl());
                this.getSSOFilter().setTokenResource(this.getTokenResource());
                this.getSSOFilter().setZfeTokenResource(this.getZfeTokenResource());
                this.getSSOFilter().setUserResource(this.getUserResource());
                this.getSSOFilter().setMaxInactiveInterval(this.getMaxInactiveInterval());
                this.getSSOFilter().setLoginResource(this.getLoginResource());
                this.getSSOFilter().setScope(this.getScope());
                this.getSSOFilter().setIndexUrl(this.urlFormat(this.getIndexUrl()));
                this.getSSOFilter().setUrl(this.getUrl());
                this.getSSOFilter().setMenuUrl(this.getMenuUrl());
                this.getSSOFilter().setSsoRedirecturl(this.getSsoRedirectUrl());
                this.getSSOFilter().setSsoRedirectUrlOrUri(this.getSsoRedirectUrlOrUri());
                this.getSSOFilter().setSsoRedirectDomainSets(ssoRedirectDomainSet);
                this.getSSOFilter().setSsoLogMethod(this.buildSSOLogMethod(this.getSsoLogLevelString()));
                this.getSSOFilter().setRepo(httpSessionSecurityContextRepository);
                this.getZfeFilter().setAppId(this.getAppId());
                this.getZfeFilter().setSecret(this.getSecret());
                this.getZfeFilter().setZfeTokenResource(this.getZfeTokenResource());
                this.getZfeFilter().setUserResource(this.getUserResource());
                this.getZfeFilter().setMaxInactiveInterval(this.getMaxInactiveInterval());
                this.getZfeFilter().setScope(this.getScope());
                this.getZfeFilter().setUrl(this.getUrl());
                this.getZfeFilter().setMenuUrl(this.getMenuUrl());
                this.getZfeFilter().setSsoRedirecturl(this.getSsoRedirectUrl());
                this.getZfeFilter().setSsoRedirectUrlOrUri(this.getSsoRedirectUrlOrUri());
                this.getZfeFilter().setSsoRedirectDomainSets(ssoRedirectDomainSet);
                this.getZfeFilter().setSsoLogMethod(this.buildSSOLogMethod(this.getSsoLogLevelString()));
                this.getZfeFilter().setRepo(httpSessionSecurityContextRepository);
                this.getZfeFilter().setRepo(httpSessionSecurityContextRepository);
                this.getZfeFilter().setEnableZfe(this.getEnableZfe());
                http
                        .headers(httpSecurityHeadersConfigurer -> httpSecurityHeadersConfigurer.frameOptions(frameOptionsConfig -> frameOptionsConfig.disable()))
                        .cors(AbstractHttpConfigurer::disable)
                        .authorizeRequests(expressionInterceptUrlRegistry -> expressionInterceptUrlRegistry.requestMatchers(pathArray).permitAll())
                        .logout(httpSecurityLogoutConfigurer -> httpSecurityLogoutConfigurer
                                .logoutUrl("/logout")
                                .invalidateHttpSession(true)
                                .logoutSuccessHandler((request, response, authentication) -> {
                                    if (!RequestUtil.isRequestAjax(request) && !RequestUtil.isJsonRequest(request)) {
                                        response.sendRedirect(logoutRedirectUrl);
                                    } else {
                                        response.setCharacterEncoding("UTF-8");
                                        response.setHeader("Content-Type", "application/json;charset=UTF-8");
                                        response.getWriter().write(JSONObject.toJSONString(new Result<>(true, "logout success!", null, "200")));
                                    }
                                }))
                        .addFilterBefore(this.getSSOFilter(), BasicAuthenticationFilter.class)
                        .addFilterBefore(this.getZfeFilter(), this.getSSOFilter().getClass())
                        .exceptionHandling(httpSecurityExceptionHandlingConfigurer -> httpSecurityExceptionHandlingConfigurer.authenticationEntryPoint(SSOConfig.this.getSSOFilter()));
            }
        } else {
            log.warn("未找到sso appId或者secret WebSecurity将自动放行所有请求");
            http
                    .headers(httpSecurityHeadersConfigurer -> httpSecurityHeadersConfigurer.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeRequests(expressionInterceptUrlRegistry -> expressionInterceptUrlRegistry.requestMatchers("/**").permitAll());
        }
    }

    public String getIndexUrl() {
        return this.indexUrl;
    }

    public void setIndexUrl(String indexUrl) {
        this.indexUrl = indexUrl;
    }

    public String getSsoUrl() {
        return this.ssoUrl;
    }

    public void setSsoUrl(String ssoUrl) {
        this.ssoUrl = ssoUrl;
    }

    public String getAppId() {
        return this.appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getSecret() {
        return this.secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getLoginResource() {
        return this.loginResource;
    }

    public void setLoginResource(String loginResource) {
        this.loginResource = loginResource;
    }

    public String getTokenResource() {
        return this.tokenResource;
    }

    public void setTokenResource(String tokenResource) {
        this.tokenResource = tokenResource;
    }

    public String getUserResource() {
        return this.userResource;
    }

    public void setUserResource(String userResource) {
        this.userResource = userResource;
    }

    public String getStaticResource() {
        return this.staticResource;
    }

    public void setStaticResource(String staticResource) {
        this.staticResource = staticResource;
    }

    public Integer getMaxInactiveInterval() {
        return this.maxInactiveInterval;
    }

    public void setMaxInactiveInterval(Integer maxInactiveInterval) {
        this.maxInactiveInterval = maxInactiveInterval;
    }

    public String getScope() {
        return this.scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMenuUrl() {
        return this.menuUrl;
    }

    public void setMenuUrl(String menuUrl) {
        this.menuUrl = menuUrl;
    }

    public String getSsoRedirectUrl() {
        return this.ssoRedirectUrl;
    }

    public void setSsoRedirectUrl(String ssoRedirectUrl) {
        this.ssoRedirectUrl = ssoRedirectUrl;
    }

    public void setForceHttps(Boolean forceHttps) {
        this.forceHttps = forceHttps;
    }

    public String getCorsAllowedOrigins() {
        return this.corsAllowedOrigins;
    }

    public void setCorsAllowedOrigins(String corsAllowedOrigins) {
        this.corsAllowedOrigins = corsAllowedOrigins;
    }

    public String getCorsAllowedMethod() {
        return this.corsAllowedMethod;
    }

    public void setCorsAllowedMethod(String corsAllowedMethod) {
        this.corsAllowedMethod = corsAllowedMethod;
    }

    public String getCorsPath() {
        return this.corsPath;
    }

    public void setCorsPath(String corsPath) {
        this.corsPath = corsPath;
    }

    public SSOFilter getSSOFilter() {
        return this.SSOFilter;
    }

    public void setSSOFilter(SSOFilter SSOFilter) {
        this.SSOFilter = SSOFilter;
    }

    public String getSsoRedirectUrlOrUri() {
        return this.ssoRedirectUrlOrUri;
    }

    public void setSsoRedirectUrlOrUri(String ssoRedirectUrlOrUri) {
        this.ssoRedirectUrlOrUri = ssoRedirectUrlOrUri;
    }

    public String getSsoRedirectDomains() {
        return this.ssoRedirectDomains;
    }

    public void setSsoRedirectDomains(String ssoRedirectDomains) {
        this.ssoRedirectDomains = ssoRedirectDomains;
    }

    public String getSsoLogLevelString() {
        return this.ssoLogLevelString;
    }

    public void setSsoLogLevelString(String ssoLogLevelString) {
        this.ssoLogLevelString = ssoLogLevelString;
        Method method = this.buildSSOLogMethod(this.ssoLogLevelString);
        this.SSOFilter.setSsoLogMethod(method);
        this.zfeFilter.setSsoLogMethod(method);
    }

    public String getZfeTokenResource() {
        return this.zfeTokenResource;
    }

    public void setZfeTokenResource(String zfeTokenResource) {
        this.zfeTokenResource = zfeTokenResource;
    }

    public ZfeFilter getZfeFilter() {
        return this.zfeFilter;
    }

    public void setZfeFilter(ZfeFilter zfeFilter) {
        this.zfeFilter = zfeFilter;
    }

    public String getPermHistoryUrl() {
        return this.permHistoryUrl;
    }

    public void setPermHistoryUrl(String permHistoryUrl) {
        this.permHistoryUrl = permHistoryUrl;
    }

    public String getPermissionFetchUrl() {
        return this.permissionFetchUrl;
    }

    public void setPermissionFetchUrl(String permissionFetchUrl) {
        this.permissionFetchUrl = permissionFetchUrl;
    }

    public Integer getSsoSessionMaxLife() {
        return this.ssoSessionMaxLife;
    }

    public void setSsoSessionMaxLife(Integer ssoSessionMaxLife) {
        this.ssoSessionMaxLife = ssoSessionMaxLife;
    }

    public Boolean getEnableZfe() {
        return this.enableZfe;
    }

    public void setEnableZfe(Boolean enableZfe) {
        this.enableZfe = enableZfe;
    }

    private Method buildSSOLogMethod(String ssoLogLevelString) {
        Method ssoLogMethod = null;
        if (StringUtils.isNotEmpty(ssoLogLevelString)) {
            try {
                if ("debug".equalsIgnoreCase(ssoLogLevelString)) {
                    ssoLogMethod = Logger.class.getMethod("debug", String.class);
                } else if ("info".equalsIgnoreCase(ssoLogLevelString)) {
                    ssoLogMethod = Logger.class.getMethod("info", String.class);
                } else if ("warn".equalsIgnoreCase(ssoLogLevelString)) {
                    ssoLogMethod = Logger.class.getMethod("warn", String.class);
                } else if ("error".equalsIgnoreCase(ssoLogLevelString)) {
                    ssoLogMethod = Logger.class.getMethod("error", String.class);
                }
            } catch (Exception e) {
            }
        }
        return ssoLogMethod;
    }

    private String urlFormat(String indexUrl) {
        try {
            URL u = new URL(indexUrl);
            StringBuffer sb = new StringBuffer();
            sb.append(u.getProtocol());
            sb.append("://");
            sb.append(u.getHost());
            if (u.getPort() != -1) {
                sb.append(":");
                sb.append(u.getPort());
            }
            return sb.toString();
        } catch (MalformedURLException e) {
            return indexUrl;
        }
    }
}
