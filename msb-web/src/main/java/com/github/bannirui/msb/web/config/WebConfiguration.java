package com.github.bannirui.msb.web.config;

import com.github.bannirui.msb.register.AbstractBeanRegistrar;
import com.github.bannirui.msb.web.filter.RefererCheckFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

/**
 * web相关的整合 尤其是sso.
 */
public class WebConfiguration extends AbstractBeanRegistrar {
    @Bean
    public FilterRegistrationBean<RefererCheckFilter> testFilterRegistration() {
        FilterRegistrationBean<RefererCheckFilter> registration = new FilterRegistrationBean();
        registration.setFilter(new RefererCheckFilter());
        registration.addUrlPatterns("/*");
        registration.addInitParameter("web.referer", this.getProperty("web.referer", ""));
        registration.setName("refererCheckFilter");
        registration.setOrder(-2147483648);
        return registration;
    }

    @Override
    public void registerBeans() {
    }
}
