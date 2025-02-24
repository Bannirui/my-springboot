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

import java.util.Objects;

@Configuration
public class SessionConfiguration extends SpringHttpSessionConfiguration implements EnvironmentAware {
    protected ConfigurableEnvironment env;
    /**
     * session的持久化实现{@link MapSessionStorageImpl}的类
     */
    public static String sessionStorageClass = MapSessionStorageImpl.class.getName();
    /**
     * session的持久化实现{@link MapSessionStorageImpl}对象
     */
    private ISessionStorage sessionStorage;
    public static String sessionType = "COOKIE";

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
        this.sessionStorage = this.getSessionStorage();
        return new UserInfoService(this.env, this.sessionStorage);
    }

    /**
     * {@link MapSessionStorageImpl}实例对象
     */
    private ISessionStorage getSessionStorage() {
        if (Objects.nonNull(this.sessionStorage)) {
            return this.sessionStorage;
        }
        // 反射实例化缓存起来
        Class storageClass = null;
        try {
            storageClass = ClassUtils.forName(this.sessionStorageClass, this.getClass().getClassLoader());
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
