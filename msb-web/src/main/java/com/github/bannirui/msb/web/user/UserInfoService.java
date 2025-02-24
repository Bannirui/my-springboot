package com.github.bannirui.msb.web.user;

import com.alibaba.fastjson.JSON;
import com.github.bannirui.msb.util.DigestUtil;
import com.github.bannirui.msb.web.filter.User;
import com.github.bannirui.msb.web.session.ISessionStorage;
import com.github.bannirui.msb.web.util.HttpUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UserInfoService implements IUserInfoService {
    private Logger logger = LoggerFactory.getLogger(UserInfoService.class);
    private ISessionStorage sessionStorage;
    private String appId;
    private String appSecret;
    private String permissionFetchUrl;

    public UserInfoService(ConfigurableEnvironment env, ISessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
        this.appId = env.getProperty("sso.appId");
        this.appSecret = env.getProperty("sso.secret");
        this.permissionFetchUrl = env.getProperty("sso.permissionFetchUrl");
    }

    @Override
    public boolean updatePermissions() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (Objects.isNull(context)) {
            return false;
        }
        Authentication authentication = context.getAuthentication();
        try {
            if (authentication != null && authentication.isAuthenticated()) {
                User user = (User) authentication;
                String openId = (String) user.getProperties("openid");
                List<User> existUserList = this.sessionStorage.getUser(openId);
                if (CollectionUtils.isEmpty(existUserList)) {
                    return false;
                }
                String url = this.permissionFetchUrl + openId;
                long timestamp = System.currentTimeMillis();
                Map<String, String> headerMap = new HashMap<>();
                headerMap.put("X-App-Id", this.appId);
                headerMap.put("X-Sign-Timestamp", "" + timestamp);
                headerMap.put("X-Sign", DigestUtil.digest("openid=" + openId + timestamp, this.appSecret, "UTF-8"));
                String permissionStr = HttpUtils.doGet(url, null, "UTF-8", headerMap, 1_000, 2_000);
                Map<String, Object> permissionMap = JSON.parseObject(permissionStr, HashMap.class);
                for (User existUser : existUserList) {
                    existUser.updatePermissions(permissionMap);
                }
                this.sessionStorage.updateUser(existUserList);
                return true;
            }
        } catch (IOException e) {
            this.logger.error("获取sso用户权限IO异常", e);
            return false;
        } catch (Exception e) {
            this.logger.error("获取sso用户权限", e);
            return false;
        }
        return false;
    }
}
