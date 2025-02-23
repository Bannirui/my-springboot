package com.github.bannirui.msb.web.session;

import com.github.bannirui.msb.ex.FrameworkException;
import com.github.bannirui.msb.plugin.InterceptorUtil;
import com.github.bannirui.msb.web.user.IUserInfoService;
import com.github.bannirui.msb.web.user.UserInfoService;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.session.config.annotation.web.http.SpringHttpSessionConfiguration;
import org.springframework.util.ClassUtils;

@Configuration
public class SessionConfiguration extends SpringHttpSessionConfiguration implements EnvironmentAware {
    protected ConfigurableEnvironment env;
    public static String sessionStorageClass = "com.zto.titans.web.session.MapSessionStorageImpl";
    public static String sessionType = "COOKIE";
    private ISessionStorage sessionStorage;

    @Override
    public void setEnvironment(Environment environment) {
        this.env = (ConfigurableEnvironment)environment;
    }

    @Bean
    public SessionRepositoryImpl sessionRepository() {
        this.sessionStorage = this.getSessionStorage();
        return new SessionRepositoryImpl(this.env, this.sessionStorage);
    }

    @Bean
    public IUserInfoService userInfoService() {
        ISessionStorage sessionStorage = this.getSessionStorage();
        return new UserInfoService(this.env, this.sessionStorage);
    }

    private ISessionStorage getSessionStorage() {
        if (this.sessionStorage != null) {
            return this.sessionStorage;
        }
        Class storageClass = null;
        try {
            storageClass = ClassUtils.forName(sessionStorageClass, this.getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            throw FrameworkException.getInstance(e, "load sessionStorageClass error:" + sessionStorageClass);
        }
        try {
            this.sessionStorage = (ISessionStorage) InterceptorUtil.getProxyObj(storageClass, new Class[]{ConfigurableEnvironment.class}, new Object[]{this.env}, "Session.Command");
        } catch (Exception e) {
            throw FrameworkException.getInstance(e, "SessionStorage获取代理失败", e);
        }
        return this.sessionStorage;
    }
}
