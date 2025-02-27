package com.github.bannirui.msb.endpoint.web;

import com.github.bannirui.msb.endpoint.health.HealthIndicator;
import jakarta.servlet.FilterConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class EndpointFilter implements Filter {
    private static final String ENDPOINT_PREFIX = "/_msb";
    private String key;

    public void init(FilterConfig filterConfig) {
        this.key = filterConfig.getInitParameter("msb.endpoint.check.key");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest)request;
        String requestPath = req.getServletPath();
        if (requestPath != null && requestPath.startsWith(EndpointFilter.ENDPOINT_PREFIX)) {
            Map<String, HealthIndicator> healthIndicatorMap = EndpointManager.getHealthIndicatorHashMap();
            HttpServletResponse httpServletResponse = (HttpServletResponse)response;
            httpServletResponse.setContentType("application/json");
            if (StringUtils.isEmpty(this.key)) {
                HealthIndicator dataSource = healthIndicatorMap.get("dataSource");
                if (dataSource != null && dataSource.health().get("status").toString().equals("UP")) {
                    httpServletResponse.setStatus(HttpStatus.OK.value());
                } else if (dataSource != null && dataSource.health().get("status").toString().equals("DOWN")) {
                    httpServletResponse.setStatus(HttpStatus.OK.value());
                } else {
                    httpServletResponse.setStatus(HttpStatus.OK.value());
                }
            } else {
                synchronized(this) {
                    boolean flag = true;
                    String[] keys = this.key.split(",");
                    for (String key : keys) {
                        HealthIndicator healthIndicator = healthIndicatorMap.get(key);
                        if (healthIndicator != null && !healthIndicator.health().get("status").equals("UP")) {
                            flag = false;
                            break;
                        }
                    }
                    if (this.checkKeyIsValid() && flag) {
                        httpServletResponse.setStatus(HttpStatus.OK.value());
                    } else {
                        httpServletResponse.setStatus(HttpStatus.OK.value());
                    }
                }
            }
            String authorization = req.getHeader("Authorization");
            String endpointName = requestPath.substring(EndpointFilter.ENDPOINT_PREFIX.length(), requestPath.length());
            httpServletResponse.getOutputStream().write(EndpointManager.dispatcher(request.getRemoteAddr(), endpointName, authorization).getBytes());
            httpServletResponse.getOutputStream().flush();
            httpServletResponse.flushBuffer();
            httpServletResponse.getOutputStream().close();
        } else {
            chain.doFilter(request, response);
        }
    }

    private boolean checkKeyIsValid() {
        boolean flag = false;
        String[] keys = this.key.split(",");
        Map<String, HealthIndicator> healthIndicatorMap = EndpointManager.getHealthIndicatorHashMap();
        for (String key : keys) {
            HealthIndicator healthIndicator = healthIndicatorMap.get(keys);
            if (healthIndicator != null) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    @Override
    public void destroy() {
    }
}
