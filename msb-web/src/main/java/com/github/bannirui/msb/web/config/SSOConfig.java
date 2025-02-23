package com.github.bannirui.msb.web.config;

public class SSOConfig extends WebSecurityConfigurerAdapter {
    private static Logger log = LoggerFactory.getLogger(SSOConfig.class);
    private String indexUrl;
    private String ssoUrl;
    private String url;
    private String appId;
    private String secret;
    private String loginResource;
    private String tokenResource;
    private String zfeTokenResource;
    private String userResource;
    private String staticResource;
    private Integer maxInactiveInterval;
    private String scope;
    private String menuUrl;
    private String ssoRedirectUrl;
    private Boolean forceHttps;
    private String corsAllowedOrigins;
    private String corsAllowedMethod;
    private String corsPath;
    private String ssoRedirectUrlOrUri;
    private TitansSSOFilter titansSSOFilter;
    private ZfeFilter zfeFilter;
    private String ssoRedirectDomains;
    private String ssoLogLevelString;
    private String permHistoryUrl;
    private String permissionFetchUrl;
    private Integer ssoSessionMaxLife;
    private Boolean enableZfe;

    public SSOConfig() {
    }

    protected void configure(HttpSecurity http) throws Exception {
        if (this.getTitansSSOFilter() == null) {
            throw new ErrorCodeException(ExceptionEnum.PARAMETER_EXCEPTION, new Object[]{"SSO", "TitansSSOFilter is empty!"});
        } else {
            String[] pathArray;
            if (this.getCorsAllowedOrigins() != null && this.getCorsAllowedMethod() != null && this.getCorsPath() != null) {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(Arrays.asList(this.getCorsAllowedOrigins().split(",")));
                configuration.setAllowedMethods(Arrays.asList(this.getCorsAllowedMethod().split(",")));
                configuration.applyPermitDefaultValues();
                configuration.setAllowCredentials(true);
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                String[] paths = this.getCorsPath().split(",");
                pathArray = paths;
                int var6 = paths.length;

                for(int var7 = 0; var7 < var6; ++var7) {
                    String path = pathArray[var7];
                    source.registerCorsConfiguration(path, configuration);
                }

                http.cors().configurationSource(source);
            }

            if (!StringUtil.isEmpty(this.getAppId()) && !StringUtil.isEmpty(this.getSecret())) {
                if (StringUtil.isEmpty(this.getSsoUrl())) {
                    throw new ErrorCodeException(ExceptionEnum.PARAMETER_EXCEPTION, new Object[]{"SSO", "ssoUrl=[" + this.getSsoUrl() + "]"});
                } else {
                    String logoutRedirectUrl;
                    if (this.ssoUrl.contains("test")) {
                        logoutRedirectUrl = "/logout-test.html";
                    } else {
                        logoutRedirectUrl = "/logout.html";
                    }

                    if (StringUtil.isEmpty(this.loginResource)) {
                        throw new ErrorCodeException(ExceptionEnum.PARAMETER_EXCEPTION, new Object[]{"SSO", "loginResource=[" + this.loginResource + "]"});
                    } else if (StringUtil.isEmpty(this.getTokenResource())) {
                        throw new ErrorCodeException(ExceptionEnum.PARAMETER_EXCEPTION, new Object[]{"SSO", "tokenResource=[" + this.getTokenResource() + "]"});
                    } else if (StringUtil.isEmpty(this.getUserResource())) {
                        throw new ErrorCodeException(ExceptionEnum.PARAMETER_EXCEPTION, new Object[]{"SSO", "userResource=[" + this.getUserResource() + "]"});
                    } else if (StringUtil.isEmpty(this.getStaticResource())) {
                        throw new ErrorCodeException(ExceptionEnum.PARAMETER_EXCEPTION, new Object[]{"SSO", "staticResource=[" + this.getStaticResource() + "]"});
                    } else if (this.getMaxInactiveInterval() == null) {
                        throw new ErrorCodeException(ExceptionEnum.PARAMETER_EXCEPTION, new Object[]{"SSO", "maxInactiveInterval=[" + this.getMaxInactiveInterval() + "]"});
                    } else if (StringUtil.isEmpty(this.getMenuUrl())) {
                        throw new ErrorCodeException(ExceptionEnum.PARAMETER_EXCEPTION, new Object[]{"SSO", "menuUrl=[" + this.getMenuUrl() + "]"});
                    } else {
                        Set<String> ssoRedirectDomainSet = new HashSet();
                        if (StringUtil.isNotEmpty(this.getSsoRedirectDomains())) {
                            ssoRedirectDomainSet = new HashSet(Arrays.asList(this.getSsoRedirectDomains().split(";")));
                        }

                        this.staticResource = "/logout,/login," + logoutRedirectUrl + "," + this.staticResource;
                        HttpSessionSecurityContextRepository httpSessionSecurityContextRepository = new HttpSessionSecurityContextRepository();
                        pathArray = this.getStaticResource().split(",");
                        this.getTitansSSOFilter().setAppId(this.getAppId());
                        this.getTitansSSOFilter().setSecret(this.getSecret());
                        this.getTitansSSOFilter().setSsoUrl(this.getSsoUrl());
                        this.getTitansSSOFilter().setTokenResource(this.getTokenResource());
                        this.getTitansSSOFilter().setZfeTokenResource(this.getZfeTokenResource());
                        this.getTitansSSOFilter().setUserResource(this.getUserResource());
                        this.getTitansSSOFilter().setMaxInactiveInterval(this.getMaxInactiveInterval());
                        this.getTitansSSOFilter().setLoginResource(this.getLoginResource());
                        this.getTitansSSOFilter().setScope(this.getScope());
                        this.getTitansSSOFilter().setIndexUrl(this.urlFormat(this.getIndexUrl()));
                        this.getTitansSSOFilter().setUrl(this.getUrl());
                        this.getTitansSSOFilter().setMenuUrl(this.getMenuUrl());
                        this.getTitansSSOFilter().setSsoRedirecturl(this.getSsoRedirectUrl());
                        this.getTitansSSOFilter().setSsoRedirectUrlOrUri(this.getSsoRedirectUrlOrUri());
                        this.getTitansSSOFilter().setSsoRedirectDomainSets(ssoRedirectDomainSet);
                        this.getTitansSSOFilter().setSsoLogMethod(this.buildSSOLogMethod(this.getSsoLogLevelString()));
                        this.getTitansSSOFilter().setRepo(httpSessionSecurityContextRepository);
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
                        ((HttpSecurity)((HttpSecurity)((FormLoginConfigurer)((HttpSecurity)((AuthorizedUrl)((AuthorizedUrl)((HttpSecurity)((HttpSecurity)http.headers().frameOptions().disable().and()).csrf().disable()).authorizeRequests().antMatchers(pathArray)).permitAll().anyRequest()).authenticated().and()).formLogin().permitAll()).and()).logout().logoutUrl("/logout").invalidateHttpSession(true).logoutSuccessHandler((request, response, authenticate) -> {
                            if (!RequestUtil.isRequestAjax(request) && !RequestUtil.isJsonRequest(request)) {
                                response.sendRedirect(logoutRedirectUrl);
                            } else {
                                response.setCharacterEncoding("UTF-8");
                                response.setHeader("Content-Type", "application/json;charset=UTF-8");
                                response.getWriter().write(JSONObject.toJSONString(new Result(true, "logout success!", (Object)null, "200")));
                            }

                        }).permitAll().and()).addFilterBefore(this.getTitansSSOFilter(), BasicAuthenticationFilter.class).addFilterBefore(this.getZfeFilter(), this.getTitansSSOFilter().getClass());
                        http.exceptionHandling().authenticationEntryPoint(this.getTitansSSOFilter());
                    }
                }
            } else {
                log.warn("未找到sso appId或者secret WebSecurity将自动放行所有请求");
                ((AuthorizedUrl)((HttpSecurity)((HttpSecurity)http.headers().frameOptions().disable().and()).csrf().disable()).authorizeRequests().antMatchers(new String[]{"/**"})).permitAll();
            }
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

    public TitansSSOFilter getTitansSSOFilter() {
        return this.titansSSOFilter;
    }

    public void setTitansSSOFilter(TitansSSOFilter titansSSOFilter) {
        this.titansSSOFilter = titansSSOFilter;
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
        this.titansSSOFilter.setSsoLogMethod(method);
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
        if (StringUtil.isNotEmpty(ssoLogLevelString)) {
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
            } catch (Exception var4) {
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
        } catch (MalformedURLException var4) {
            return indexUrl;
        }
    }
}
