package com.github.bannirui.msb.web.config;

/**
 * web相关的整合 尤其是sso.
 */
public class WebConfiguration extends AbstractBeanRegistrar {
    @Bean
    public FilterRegistrationBean<RefererCheckFilter> testFilterRegistration() {
        FilterRegistrationBean<RefererCheckFilter> registration = new FilterRegistrationBean();
        registration.setFilter(new RefererCheckFilter());
        registration.addUrlPatterns(new String[]{"/*"});
        registration.addInitParameter("titans.web.referer", this.getProperty("titans.web.referer", ""));
        registration.setName("refererCheckFilter");
        registration.setOrder(-2147483648);
        return registration;
    }

    public void registerBeans() {
    }
}
