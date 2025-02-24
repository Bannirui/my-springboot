package com.github.bannirui.msb.web.filter;

import com.alibaba.fastjson.JSON;
import com.github.bannirui.msb.entity.Result;
import com.github.bannirui.msb.util.UrlUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RefererCheckFilter implements Filter {
    public static final String REFERER_CHECK_WHITE = "titans.web.referer";
    private boolean disabled;
    private Set<String> whiteSet;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String white = filterConfig.getInitParameter("titans.web.referer");
        if (StringUtils.isEmpty(white)) {
            this.whiteSet = new HashSet<>();
            this.disabled = true;
        } else {
            this.whiteSet = new HashSet<>(Arrays.asList(white.split(",")));
            this.disabled = false;
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (this.disabled) {
            chain.doFilter(request, response);
        } else {
            HttpServletRequest req = (HttpServletRequest)request;
            String referer = req.getHeader("referer");
            if (StringUtils.isEmpty(referer)) {
                chain.doFilter(request, response);
            } else {
                String domain = UrlUtil.retrieveDomainFromUrl(referer);
                if (StringUtils.isNotEmpty(domain) && this.whiteSet.contains(domain)) {
                    chain.doFilter(request, response);
                } else {
                    String resultObj = JSON.toJSONString(Result.error("302", "Your current domain does not allow access to the application."));
                    response.getWriter().write(resultObj);
                }
            }
        }
    }
}
