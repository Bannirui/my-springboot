package com.github.bannirui.msb.endpoint.web;

import com.github.bannirui.msb.endpoint.health.HealthIndicator;
import jakarta.servlet.FilterConfig;
import java.io.IOException;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

public class EndpointFilter implements Filter {
    private static final String ENDPOINT_PREFIX = "/_titans";
    private String key;

    public void init(FilterConfig filterConfig) {
        this.key = filterConfig.getInitParameter("titans.endpoint.check.key");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest)request;
        String requestPath = req.getServletPath();
        if (requestPath != null && requestPath.startsWith("/_titans")) {
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
                        HealthIndicator healthIndicator = (HealthIndicator)healthIndicatorMap.get(key);
                        if (healthIndicator != null && !((String)healthIndicator.health().get("status")).equals("UP")) {
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
            String endpointName = requestPath.substring("/_titans".length(), requestPath.length());
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
            HealthIndicator healthIndicator = (HealthIndicator)healthIndicatorMap.get(keys);
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
