package com.github.bannirui.msb.web.session.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TitansCookieSerializer implements CookieSerializer {
    private static final Pattern CHROME_80_PATTERN = Pattern.compile("Chrome\\/(([0-9]+)\\.?([\\w]+)?(\\.[\\w]+)?(\\.[\\w]+)?)");
    private static final Integer CHROME_80_VERSION = 80;
    private DefaultCookieSerializer springCookieSerializer = new DefaultCookieSerializer();
    private boolean sameSiteNone = false;

    public TitansCookieSerializer() {
        this.springCookieSerializer.setSameSite(null);
        this.springCookieSerializer.setUseSecureCookie(false);
    }

    @Override
    public void writeCookieValue(CookieValue cookieValue) {
        this.springCookieSerializer.writeCookieValue(cookieValue);
        if (this.sameSiteNone) {
            this.writeTitansCookieValue(cookieValue);
        }
    }

    private void writeTitansCookieValue(CookieValue cookieValue) {
        HttpServletRequest request = cookieValue.getRequest();
        HttpServletResponse response = cookieValue.getResponse();
        StringBuilder cookie = new StringBuilder(response.getHeader("Set-Cookie"));
        this.dealChrome80(cookie, request);
        request.getScheme();
        response.setHeader("Set-Cookie", cookie.toString());
    }

    private void dealChrome80(StringBuilder cookie, HttpServletRequest req) {
        String userAgent = req.getHeader("User-Agent");
        if (!StringUtils.isEmpty(userAgent)) {
            Matcher matcher = CHROME_80_PATTERN.matcher(userAgent);
            if (matcher.find()) {
                String majorVersion = matcher.group(2);
                if (CHROME_80_VERSION.compareTo(Integer.valueOf(majorVersion)) <= 0) {
                    if (req.isSecure()) {
                        cookie.append("; SameSite=").append("None");
                        cookie.append("; Secure");
                    }
                }
            }
        }
    }

    public List<String> readCookieValues(HttpServletRequest request) {
        List<String> sessionIds = new ArrayList<>();
        String xPureMilk = request.getHeader("X-Pure-Milk");
        String xPureWater = request.getHeader("X-Pure-Water");
        if (StringUtils.isNotEmpty(xPureMilk) && StringUtils.isNotEmpty(xPureWater)) {
            sessionIds.add(xPureMilk + "_" + xPureWater);
        }
        List<String> cookieSessionList = this.springCookieSerializer.readCookieValues(request);
        sessionIds.addAll(cookieSessionList);
        return sessionIds;
    }

    public void setCookieName(String cookieName) {
        this.springCookieSerializer.setCookieName(cookieName);
    }

    public void setUseHttpOnlyCookie(boolean useHttpOnlyCookie) {
        this.springCookieSerializer.setUseHttpOnlyCookie(useHttpOnlyCookie);
    }

    public void setSameSiteNone(boolean sameSiteNone) {
        this.sameSiteNone = sameSiteNone;
    }
}
