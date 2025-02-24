package com.github.bannirui.msb.web.filter;

import com.alibaba.fastjson.JSON;
import com.github.bannirui.msb.entity.Result;
import com.github.bannirui.msb.util.UrlUtil;
import com.github.bannirui.msb.web.util.HttpUtils;
import com.github.bannirui.msb.web.util.RequestUtil;
import com.github.bannirui.msb.web.util.Token;
import com.github.bannirui.msb.web.util.TokenUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

public class LoginAndLogoutFilter implements SSOFilter {
    private final Logger logger = LoggerFactory.getLogger(LoginAndLogoutFilter.class);
    private static final String URL_FORMAT = "%s%s?appid=%s&redirect_uri=%s&response_type=code&scope=%s&state=%s";
    public static final String LOGIN_URL = "/login";
    public static final String SSO = "/sso";
    public static final String LOGOUT_URL = "/logout";
    public static final String LOGOUT_HTML_URL = "/logout.html";
    public static final String LOGOUT_HTML_URL_TEST = "/logout-test.html";
    public static final String LOGIN_CODE = "code";
    public static final String LOGIN_STATE = "state";
    public static final String REDIRECT_FRONT_URI = "redirectFrontURI";
    public static final String REDIRECT_URLORURI_URL_VALUE = "url";
    private static final Set<String> EXCLUDE_URI = new HashSet<>();
    private String appId;
    private String secret;
    private String ssoUrl;
    private String tokenResource;
    private String zfeTokenResource;
    private String userResource;
    private Integer maxInactiveInterval;
    private String loginResource;
    private String scope;
    private String indexUrl;
    private String url;
    private String menuUrl;
    private SecurityContextRepository repo;
    private String ssoRedirecturl;
    private String ssoRedirectUrlOrUri;
    private Set<String> ssoRedirectDomainSets = new HashSet<>();
    private Method ssoLogMethod = null;
    private Random random = new Random();

    public LoginAndLogoutFilter() {
        this.setRepo(new HttpSessionSecurityContextRepository());
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        EXCLUDE_URI.add("/sso");
        EXCLUDE_URI.add("/logout");
        EXCLUDE_URI.add("/login");
        EXCLUDE_URI.add("/");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpServletResponse response = (HttpServletResponse)servletResponse;
        if ("/login".equals(request.getServletPath())) {
            String code = request.getParameter("code");
            String state = request.getParameter("state");
            String stateSession = (String)request.getSession().getAttribute("state");
            this.logSSOMsg(String.format("\n[[SSO:4-1]]\nrequestUrl=%s\nheaders=%s\nrequestCode=%s\nrequestState=%s\nsessionState=%s\nsessionId=%s", request.getRequestURL(), this.buildHeaderInfo(request).toString(), code, state, stateSession, request.getSession().getId()));
            if (stateSession == null || !stateSession.equals(state)) {
                this.logSSOMsg(String.format("\n[[SSO:end1]]\nsend redirct contextPath %s", request.getContextPath()));
                response.sendRedirect(request.getContextPath());
                return;
            }
            try {
                Token token = this.getToken(code);
                Map<String, Object> userInfo = this.getUserInfo(token);
                HttpRequestResponseHolder holder = new HttpRequestResponseHolder(request, response);
                List<Menu> menus = this.getMenu(String.format(this.getMenuUrl(), this.getAppId()), userInfo.get("openid").toString(), this.secret);
                User currentUser = new User(userInfo, menus);
                currentUser.setAccessToken(token.getAccess_token());
                SecurityContextHolder.getContext().setAuthentication(currentUser);
                this.getRepo().saveContext(SecurityContextHolder.getContext(), holder.getRequest(), holder.getResponse());
                ((HttpServletRequest)servletRequest).getSession().setMaxInactiveInterval(this.getMaxInactiveInterval());
            } catch (Exception e) {
                logger.error("登录获取token/userinfo失败 code:{}, url:{}", code, this.url, e);
            }
            this.processRedirectFront(request, response);
        } else {
            HttpRequestResponseHolder holder = new HttpRequestResponseHolder(request, response);
            SecurityContext contextBeforeChainExecution = this.getRepo().loadContext(holder);
            boolean flag = false;
            try {
                flag = true;
                SecurityContextHolder.setContext(contextBeforeChainExecution);
                filterChain.doFilter(holder.getRequest(), holder.getResponse());
                flag = false;
            } finally {
                if (flag) {
                    SecurityContext contextAfterChainExecution = SecurityContextHolder.getContext();
                    this.getRepo().saveContext(contextAfterChainExecution, holder.getRequest(), holder.getResponse());
                }
            }
            SecurityContext contextAfterChainExecution = SecurityContextHolder.getContext();
            this.getRepo().saveContext(contextAfterChainExecution, holder.getRequest(), holder.getResponse());
        }
    }

    private String getRedirectFrontURI(HttpServletRequest request) {
        String redirectFrontURI = request.getParameter("redirectFrontURI");
        return StringUtils.isNotEmpty(redirectFrontURI) ? redirectFrontURI : "";
    }

    private String getLoginUrl(String state, String redirectFrontURI, String reqUrl) {
        if (StringUtils.isNotEmpty(reqUrl) && reqUrl.endsWith("/")) {
            reqUrl = reqUrl.substring(0, reqUrl.length() - 1);
        }
        String redirectUrl = HttpUtils.encode(reqUrl + "/login" + "?" + "redirectFrontURI" + "=" + redirectFrontURI);
        return String.format("%s%s?appid=%s&redirect_uri=%s&response_type=code&scope=%s&state=%s", this.getSsoUrl(), this.getLoginResource(), this.getAppId(), redirectUrl, this.getScope(), state);
    }

    public Token getToken(String code) throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("appid", this.getAppId());
        params.put("secret", this.getSecret());
        params.put("code", code);
        params.put("grant_type", "authorization_code");
        Integer rnd = this.random.nextInt(10000) * this.random.nextInt(10000);
        this.logSSOMsg(String.format("\n[[SSO:step4-3-1]]Before:\nurl=%s\nparams=%s\nrnd=%d", this.url + this.tokenResource, params.toString(), rnd));
        String str = HttpUtils.doGet(this.getUrl() + this.getTokenResource(), params);
        Token token = JSON.parseObject(str, Token.class);
        this.logSSOMsg(String.format("\n[[SSO:step4-3-1]]\nurl=%s\nparams=%s\ntoken.access_token=%s, token.expires_in=%s, token.refresh_token=%s, token.openid=%s, token.scope=%s, rnd=%d", this.getUrl() + this.getTokenResource(), params.toString(), token.getAccess_token(), token.getExpires_in(), token.getRefresh_token(), token.getOpenid(), token.getScope(), rnd));
        return token;
    }

    public Map<String, Object> getUserInfo(Token token) throws IOException {
        Map<String, Object> params = new HashMap();
        params.put("access_token", token.getAccess_token());
        params.put("openid", token.getOpenid());
        Integer rnd = this.random.nextInt(10000) * this.random.nextInt(10000);
        this.logSSOMsg(String.format("\n[[SSO:4-3-2]]Before:\nurl=%s\nparams=%s\nrnd=%d", this.url + this.userResource, params.toString(), rnd));
        String str = HttpUtils.doGet(this.url + this.userResource, params);
        this.logSSOMsg(String.format("\n[[SSO:4-3-2]]After:\nurl=%s\nparams=%s\nuserStr=%s\nrnd=%d", this.getUrl() + this.getUserResource(), params.toString(), str, rnd));
        return (Map)JSON.parseObject(str, HashMap.class);
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

    @Override
    public void destroy() {
        EXCLUDE_URI.clear();
    }

    @Override
    public String getAppId() {
        return this.appId;
    }

    @Override
    public void setAppId(String appId) {
        this.appId = appId;
    }

    @Override
    public String getSecret() {
        return this.secret;
    }

    @Override
    public void setSecret(String secret) {
        this.secret = secret;
    }

    @Override
    public String getSsoUrl() {
        return this.ssoUrl;
    }

    @Override
    public void setSsoUrl(String ssoUrl) {
        this.ssoUrl = ssoUrl;
    }

    @Override
    public String getTokenResource() {
        return this.tokenResource;
    }

    @Override
    public void setTokenResource(String tokenResource) {
        this.tokenResource = tokenResource;
    }

    @Override
    public String getUserResource() {
        return this.userResource;
    }

    @Override
    public void setUserResource(String userResource) {
        this.userResource = userResource;
    }

    @Override
    public Integer getMaxInactiveInterval() {
        return this.maxInactiveInterval;
    }

    @Override
    public void setMaxInactiveInterval(Integer maxInactiveInterval) {
        this.maxInactiveInterval = maxInactiveInterval;
    }

    @Override
    public String getLoginResource() {
        return this.loginResource;
    }

    @Override
    public void setLoginResource(String loginResource) {
        this.loginResource = loginResource;
    }

    @Override
    public String getScope() {
        return this.scope;
    }

    @Override
    public void setScope(String scope) {
        this.scope = scope;
    }

    @Override
    public String getIndexUrl() {
        return this.indexUrl;
    }

    @Override
    public void setIndexUrl(String indexUrl) {
        this.indexUrl = indexUrl;
    }

    @Override
    public String getUrl() {
        return this.url;
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String getMenuUrl() {
        return this.menuUrl;
    }

    @Override
    public void setMenuUrl(String menuUrl) {
        this.menuUrl = menuUrl;
    }

    @Override
    public SecurityContextRepository getRepo() {
        return this.repo;
    }

    @Override
    public void setRepo(SecurityContextRepository repo) {
        this.repo = repo;
    }

    @Override
    public String getSsoRedirecturl() {
        return this.ssoRedirecturl;
    }

    @Override
    public void setSsoRedirecturl(String ssoRedirecturl) {
        this.ssoRedirecturl = ssoRedirecturl;
    }

    @Override
    public String getSsoRedirectUrlOrUri() {
        return this.ssoRedirectUrlOrUri;
    }

    @Override
    public void setSsoRedirectUrlOrUri(String ssoRedirectUrlOrUri) {
        this.ssoRedirectUrlOrUri = ssoRedirectUrlOrUri;
    }

    @Override
    public Set<String> getSsoRedirectDomainSets() {
        return this.ssoRedirectDomainSets;
    }

    @Override
    public void setSsoRedirectDomainSets(Set<String> ssoRedirectDomainSets) {
        this.ssoRedirectDomainSets = ssoRedirectDomainSets;
    }

    @Override
    public Method getSsoLogMethod() {
        return this.ssoLogMethod;
    }

    @Override
    public void setSsoLogMethod(Method ssoLogMethod) {
        this.ssoLogMethod = ssoLogMethod;
    }

    public String getZfeTokenResource() {
        return this.zfeTokenResource;
    }

    @Override
    public void setZfeTokenResource(String zfeTokenResource) {
        this.zfeTokenResource = zfeTokenResource;
    }

    @Override
    public void processEntryPoint(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        String state = TokenUtils.randomState();
        request.getSession().setAttribute("state", state);
        this.logSSOMsg(String.format("\n[[SSO:step2-1]]\nrequestUrl:%s\nrequestHeaders:%s\nstate:%s\nsessionId:%s", request.getRequestURL(), this.buildHeaderInfo(request).toString(), state, request.getSession().getId()));
        String reqUrl = this.getIndexUrl();
        if (reqUrl == null || "null".equals(reqUrl) || "".equals(reqUrl)) {
            StringBuffer url = request.getRequestURL();
            reqUrl = url.delete(url.length() - request.getRequestURI().length(), url.length()).append("/").toString();
        }
        this.logSSOMsg(String.format("\n[[SSO:step2-2]]\nsso.indexUrl=%s\nrequestUrl=%s", this.getIndexUrl(), reqUrl));
        String requestURI = this.buildRedirectFrontUrl(request, response);
        requestURI = Base64.getEncoder().encodeToString(requestURI.getBytes());
        this.logSSOMsg(String.format("\n[[SSO:step2-4]]\nrequestURI[encode]=%s", requestURI));
        String loginUrl = this.getLoginUrl(state, requestURI, reqUrl);
        this.logSSOMsg(String.format("\n[[SSO:step2-5]]\nloginUrl=%s", loginUrl));
        if (!RequestUtil.isRequestAjax(request) && !RequestUtil.isJsonRequest(request)) {
            response.setContentType("application/json;charset=utf-8");
            this.logSSOMsg(String.format("\n[[SSO:step2-6]]\n send redirectUrl %s", loginUrl));
            response.sendRedirect(loginUrl);
        } else {
            String resultObj = JSON.toJSONString(Result.error("302", loginUrl));
            this.logSSOMsg(String.format("\n[[SSO:step2-6]]\nreturn ajaxObj %s", resultObj));
            response.getWriter().write(resultObj);
        }
    }

    @Override
    public void processRedirectFront(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
                        logger.error(String.format("不允许跳转到的域名[%s] sso.redirect.domains=%s 请在Apollo中配置sso.redirect.domains参数 多个域名中间用;分隔 不支持动态增加", substringStr, this.getSsoRedirectDomainSets().toString()));
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
                logger.error(e.getMessage(), e);
            }
            if (StringUtils.isNotEmpty(redirectFrontURI) && !EXCLUDE_URI.contains(redirectFrontURI)) {
                this.logSSOMsg(String.format("\n[[SSO:step-end3]]\nsend redirect frontURI %s", redirectFrontURI));
                response.sendRedirect(redirectFrontURI);
            } else {
                this.logSSOMsg(String.format("\n[[SSO:tep-end3]]\nsend redirect contextPath %s", request.getContextPath()));
                response.sendRedirect(request.getContextPath());
            }
        }
    }

    @Override
    public String buildRedirectFrontUrl(HttpServletRequest request, HttpServletResponse response) {
        String redirectFrontUrl = null;
        if (!RequestUtil.isRequestAjax(request) && !RequestUtil.isJsonRequest(request)) {
            redirectFrontUrl = request.getRequestURI();
            String querystr = request.getQueryString();
            if (null != querystr) {
                redirectFrontUrl = redirectFrontUrl + "?" + querystr;
            }
            this.logSSOMsg(String.format("\n[[SSO:step2-3]]\nisAjax=false\nrequestURI=%s\nquerystr=%s\nredirectFrontUrl=%s", request.getRequestURI(), querystr, redirectFrontUrl));
        } else {
            redirectFrontUrl = request.getHeader("referer");
            if (redirectFrontUrl != null) {
                int index = redirectFrontUrl.indexOf(35);
                if (index >= 0) {
                    redirectFrontUrl = redirectFrontUrl.substring(0, index);
                }
            }
            this.logSSOMsg(String.format("\n[[SSO:step2-3]]\nisAjax=true\nreferer=%s\nredirectFrontUrl=%s", request.getHeader("referer"), redirectFrontUrl));
        }
        return redirectFrontUrl == null ? "/" : redirectFrontUrl;
    }

    private Map<String, String> buildHeaderInfo(HttpServletRequest request) {
        Enumeration<String> headerNameIterator = request.getHeaderNames();
        Map<String, String> result = new HashMap<>();
        while(headerNameIterator.hasMoreElements()) {
            String headerName = headerNameIterator.nextElement();
            result.put(headerName, request.getHeader(headerName));
        }
        return result;
    }

    private void logSSOMsg(String message) {
        if (this.getSsoLogMethod() != null) {
            try {
                this.getSsoLogMethod().invoke(logger, message);
            } catch (Exception e) {
                logger.warn("SSO logSSOMsg throws exception", e);
            }
        }
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, jakarta.servlet.ServletException {
        this.processEntryPoint(request, response, authException);
    }
}
