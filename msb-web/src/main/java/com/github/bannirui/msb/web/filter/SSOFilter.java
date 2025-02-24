package com.github.bannirui.msb.web.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.context.SecurityContextRepository;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Set;

public interface SSOFilter extends Filter, AuthenticationEntryPoint {
    String buildRedirectFrontUrl(HttpServletRequest request, HttpServletResponse response);

    void processEntryPoint(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException;

    void processRedirectFront(HttpServletRequest request, HttpServletResponse response) throws IOException;

    String getAppId();

    void setAppId(String appId);

    String getSecret();

    void setSecret(String secret);

    String getSsoUrl();

    void setSsoUrl(String ssoUrl);

    String getTokenResource();

    void setTokenResource(String tokenResource);

    String getUserResource();

    void setUserResource(String userResource);

    Integer getMaxInactiveInterval();

    void setMaxInactiveInterval(Integer maxInactiveInterval);

    String getLoginResource();

    void setLoginResource(String loginResource);

    String getScope();

    void setScope(String scope);

    String getIndexUrl();

    void setIndexUrl(String indexUrl);

    String getUrl();

    void setUrl(String url);

    String getMenuUrl();

    void setMenuUrl(String menuUrl);

    void setZfeTokenResource(String zfeTokenResource);

    SecurityContextRepository getRepo();

    void setRepo(SecurityContextRepository repo);

    String getSsoRedirecturl();

    void setSsoRedirecturl(String ssoRedirecturl);

    String getSsoRedirectUrlOrUri();

    void setSsoRedirectUrlOrUri(String ssoRedirectUrlOrUri);

    Set<String> getSsoRedirectDomainSets();

    void setSsoRedirectDomainSets(Set<String> ssoRedirectDomainSets);

    Method getSsoLogMethod();

    void setSsoLogMethod(Method ssoLogMethod);
}
