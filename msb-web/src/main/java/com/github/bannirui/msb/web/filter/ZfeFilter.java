package com.github.bannirui.msb.web.filter;

import com.alibaba.fastjson.JSON;
import com.github.bannirui.msb.util.UrlUtil;
import com.github.bannirui.msb.web.ex.SsoNeedRefreshException;
import com.github.bannirui.msb.web.util.HttpUtils;
import com.github.bannirui.msb.web.util.Token;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.servlet.FilterConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;

public class ZfeFilter implements Filter {
    private Logger logger = LoggerFactory.getLogger(ZfeFilter.class);
    private String appId;
    private String secret;
    private String zfeTokenResource;
    private String userResource;
    private Integer maxInactiveInterval;
    private String scope;
    private String url;
    private String menuUrl;
    private SecurityContextRepository repo;
    private Method ssoLogMethod = null;
    private Random random = new Random();
    public static final String REDIRECT_FRONT_URI = "redirectFrontURI";
    public static final String REDIRECT_URLORURI_URL_VALUE = "url";
    private static final Set<String> EXCLUDE_URI = new HashSet<>();
    public static final String LOGIN_URL = "/login";
    public static final String SSO = "/sso";
    public static final String LOGOUT_URL = "/logout";
    private String ssoRedirecturl;
    private String ssoRedirectUrlOrUri;
    private Boolean enableZfe;
    private Set<String> ssoRedirectDomainSets = new HashSet<>();
    private final String NEED_REFRESH_JSON_STR = "{\"message\":\"登录用户已变更\",\"result\":null,\"status\":false,\"statusCode\":\"S230\"}";

    public void init(FilterConfig filterConfig) throws ServletException {
        EXCLUDE_URI.add("/sso");
        EXCLUDE_URI.add("/logout");
        EXCLUDE_URI.add("/login");
        EXCLUDE_URI.add("/");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, jakarta.servlet.ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpServletResponse response = (HttpServletResponse)servletResponse;
        String xPureMilk = request.getHeader("X-Pure-Milk");
        String xPureWater = request.getHeader("X-Pure-Water");
        if (StringUtils.isNotBlank(xPureMilk) && StringUtils.isNotBlank(xPureWater) && this.getEnableZfe()) {
            if (!this.isAuthenticated()) {
                try {
                    Token zfetToken = this.getZfetToken(xPureMilk, xPureWater, request);
                    Map<String, Object> userInfo = this.getUserInfo(zfetToken);
                    HttpRequestResponseHolder holder = new HttpRequestResponseHolder(request, response);
                    List<Menu> menus = this.getMenu(String.format(this.getMenuUrl(), this.getAppId()), userInfo.get("openid").toString(), this.secret);
                    User currentUser = new User(userInfo, menus);
                    currentUser.setAccessToken(zfetToken.getAccess_token());
                    SecurityContextHolder.getContext().setAuthentication(currentUser);
                    this.getRepo().saveContext(SecurityContextHolder.getContext(), holder.getRequest(), holder.getResponse());
                    request.getSession().setMaxInactiveInterval(this.getMaxInactiveInterval());
                    this.logSSOMsg(String.format("pure-water=%s pure-milk=%s username=%s", xPureWater, xPureMilk, currentUser.getName() != null ? currentUser.getName() : ""));
                } catch (SsoNeedRefreshException e) {
                    this.logSSOMsg(String.format("\n[[SSO]]\n sso need refresh"));
                    response.setContentType("application/json; charset=UTF-8");
                    response.getWriter().write("{\"message\":\"登录用户已变更\",\"result\":null,\"status\":false,\"statusCode\":\"S230\"}");
                    return;
                } catch (Exception e) {
                    this.logger.error("zfe登录获取token/userinfo失败 url:{} water {} milk {}", this.url, xPureWater, xPureMilk, e);
                }
            }
            this.processRedirectFront(request, response, filterChain);
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    public void processRedirectFront(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        if ("/login".equals(request.getServletPath())) {
            if (StringUtils.isNotEmpty(this.getSsoRedirecturl())) {
                this.logSSOMsg(String.format("\n[[SSO:step-end2]]\nsso.redirectUrl=%s\nsend redirct url %s", this.getSsoRedirecturl(), this.getSsoRedirecturl()));
                response.sendRedirect(this.getSsoRedirecturl());
            } else {
                String redirectFrontURI = this.getRedirectFrontURI(request);
                this.logSSOMsg(String.format("\n[[SSO:step6-2]]\nredirectFrontURI[encoded]=%s", redirectFrontURI));

                try {
                    redirectFrontURI = new String(Base64.getDecoder().decode(redirectFrontURI));
                    this.logSSOMsg(String.format("\n[[SSO:step6-3]]\nredirectFrontURI=%s\nsso.redirect.urloruri=%s\nsso.redirect.domains=%s", redirectFrontURI, this.getSsoRedirectUrlOrUri(), this.getSsoRedirectUrlOrUri() != null ? this.getSsoRedirectUrlOrUri().toString() : ""));
                    String substringStr;
                    if (!StringUtils.isEmpty(this.getSsoRedirectUrlOrUri()) && "url".equalsIgnoreCase(this.getSsoRedirectUrlOrUri())) {
                        substringStr = UrlUtil.retrieveDomainFromUrl(redirectFrontURI);
                        this.logSSOMsg(String.format("\n[[SSO:step6-4-2]]\nretrivedDomain=%s\nsso.redirect.domains=%s", substringStr, this.getSsoRedirectDomainSets().toString()));
                        if (StringUtils.isNotEmpty(substringStr) && !this.getSsoRedirectDomainSets().contains(substringStr)) {
                            this.logger.error(String.format("不允许跳转到的域名[%s], sso.redirect.domains=%s, 请在Apollo中配置 sso.redirect.domains 参数！ 多个域名中间用;分隔 ， 不支持动态增加", substringStr, this.getSsoRedirectDomainSets().toString()));
                            return;
                        }
                    } else {
                        substringStr = "://";
                        if (redirectFrontURI.indexOf(substringStr) > 0) {
                            redirectFrontURI = redirectFrontURI.substring(redirectFrontURI.indexOf(substringStr) + substringStr.length());
                            redirectFrontURI = redirectFrontURI.substring(redirectFrontURI.indexOf(47));
                        }
                    }
                } catch (Exception e) {
                    this.logger.error(e.getMessage(), e);
                }
                if (StringUtils.isNotEmpty(redirectFrontURI) && !EXCLUDE_URI.contains(redirectFrontURI)) {
                    this.logSSOMsg(String.format("\n[[SSO:step-end3]]\nsend redirect frontURI %s", redirectFrontURI));
                    response.sendRedirect(redirectFrontURI);
                } else {
                    filterChain.doFilter(request, response);
                }
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private String getRedirectFrontURI(HttpServletRequest request) {
        String redirectFrontURI = request.getParameter("redirectFrontURI");
        return StringUtils.isNotEmpty(redirectFrontURI) ? redirectFrontURI : "";
    }

    private boolean isAuthenticated() {
        SecurityContext context = SecurityContextHolder.getContext();
        return context != null && context.getAuthentication() != null && context.getAuthentication().isAuthenticated();
    }

    public Token getZfetToken(String uuid, String sessionid, HttpServletRequest request) throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("uuid", uuid);
        params.put("scope", this.getScope());
        params.put("appid", this.getAppId());
        params.put("sessionid", sessionid);
        params.put("secret", this.getSecret());
        Map<String, String> headers = new HashMap<>();
        Enumeration headerNames = request.getHeaderNames();
        while(true) {
            String name;
            do {
                if (!headerNames.hasMoreElements()) {
                    name = HttpUtils.doGet(this.getZfeTokenResource(), params, headers);
                    Token token = JSON.parseObject(name, Token.class);
                    if (token.getError() != null && token.getError().equals("need_refresh")) {
                        throw new SsoNeedRefreshException();
                    }
                    return token;
                }
                name = (String)headerNames.nextElement();
            } while(!"cookie".equals(name) && !"referer".equals(name) && !"user-agent".equals(name) && !"x-forwarded-for".equals(name));
            String value = request.getHeader(name);
            headers.put(name, value);
        }
    }

    public Map<String, Object> getUserInfo(Token token) throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("access_token", token.getAccess_token());
        params.put("openid", token.getOpenid());
        Integer rnd = this.random.nextInt(10000) * this.random.nextInt(10000);
        this.logSSOMsg(String.format("\n[[SSO:4-3-2]]Before:\nurl=%s\nparams=%s\nrnd=%d", this.url + this.userResource, params.toString(), rnd));
        String str = HttpUtils.doGet(this.url + this.userResource, params);
        this.logSSOMsg(String.format("\n[[SSO:4-3-2]]After:\nurl=%s\nparams=%s\nuserStr=%s\nrnd=%d", this.getUrl() + this.getUserResource(), params.toString(), str, rnd));
        return (Map) JSON.parseObject(str, HashMap.class);
    }

    public List<Menu> getMenu(String url, String openId, String secret) throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("secret", secret);
        params.put("openid", openId);
        Integer rnd = this.random.nextInt(10000) * this.random.nextInt(10000);
        this.logSSOMsg(String.format("\n[[SSO:4-3-3]]Before:\nurl=%s\nparams=%s\nrnd=%d", url, params.toString(), rnd));
        String result = HttpUtils.doGet(url, params);
        this.logSSOMsg(String.format("\n[[SSO:4-3-3]]After1:\nurl=%s\nparams=%s\nmenuStr=%s\nrnd=%d", url, params.toString(), result, rnd));
        List<Menu> menus = JSON.parseArray(result, Menu.class);
        this.logSSOMsg(String.format("\n[[SSO:4-3-3]]After2:\nurl=%s\nparams=%s\nmenus.Size=%d\nrnd=%d", url, params.toString(), menus == null ? 0 : menus.size(), rnd));
        return menus;
    }

    public Set<String> getSsoRedirectDomainSets() {
        return this.ssoRedirectDomainSets;
    }

    public void setSsoRedirectDomainSets(Set<String> ssoRedirectDomainSets) {
        this.ssoRedirectDomainSets = ssoRedirectDomainSets;
    }

    public String getSsoRedirecturl() {
        return this.ssoRedirecturl;
    }

    public void setSsoRedirecturl(String ssoRedirecturl) {
        this.ssoRedirecturl = ssoRedirecturl;
    }

    public String getSsoRedirectUrlOrUri() {
        return this.ssoRedirectUrlOrUri;
    }

    public void setSsoRedirectUrlOrUri(String ssoRedirectUrlOrUri) {
        this.ssoRedirectUrlOrUri = ssoRedirectUrlOrUri;
    }

    public String getMenuUrl() {
        return this.menuUrl;
    }

    public void setMenuUrl(String menuUrl) {
        this.menuUrl = menuUrl;
    }

    public Integer getMaxInactiveInterval() {
        return this.maxInactiveInterval;
    }

    public void setMaxInactiveInterval(Integer maxInactiveInterval) {
        this.maxInactiveInterval = maxInactiveInterval;
    }

    public String getAppId() {
        return this.appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
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

    public String getZfeTokenResource() {
        return this.zfeTokenResource;
    }

    public void setZfeTokenResource(String zfeTokenResource) {
        this.zfeTokenResource = zfeTokenResource;
    }

    public SecurityContextRepository getRepo() {
        return this.repo;
    }

    public void setRepo(SecurityContextRepository repo) {
        this.repo = repo;
    }

    public Method getSsoLogMethod() {
        return this.ssoLogMethod;
    }

    public void setSsoLogMethod(Method ssoLogMethod) {
        this.ssoLogMethod = ssoLogMethod;
    }

    public String getUserResource() {
        return this.userResource;
    }

    public void setUserResource(String userResource) {
        this.userResource = userResource;
    }

    public String getSecret() {
        return this.secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Boolean getEnableZfe() {
        return this.enableZfe;
    }

    public void setEnableZfe(Boolean enableZfe) {
        this.enableZfe = enableZfe;
    }

    private void logSSOMsg(String message) {
        if (this.getSsoLogMethod() != null) {
            try {
                this.getSsoLogMethod().invoke(this.logger, message);
            } catch (Exception var3) {
                this.logger.warn("SSO logSSOMsg throws exception", var3);
            }
        }
    }
}
