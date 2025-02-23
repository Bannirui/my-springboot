package com.github.bannirui.msb.web.util;

import com.alibaba.fastjson.JSON;
import com.github.bannirui.msb.util.DigestUtil;
import com.github.bannirui.msb.web.config.SSOConfiguration;
import com.github.bannirui.msb.web.entity.FullMenu;
import com.github.bannirui.msb.web.entity.PermissionApplyGuide;
import com.github.bannirui.msb.web.filter.User;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

public class UserExtensionUtils {
    private static final Logger logger = LoggerFactory.getLogger(UserExtensionUtils.class);

    public static List<FullMenu> getFullMenu(User user) {
        Environment environment = SSOConfiguration.getEnvironment();
        String appId = environment.getProperty("sso.appId");
        String secret = environment.getProperty("sso.secret");
        String fullMenuUrl = environment.getProperty("sso.fullMenuUrl");
        return getFullMenu(fullMenuUrl, user, appId, secret);
    }

    public static List<FullMenu> getFullMenu(String url, User user, String appId, String secret) {
        Object openid = user.getProperties("openid");
        Object userid = user.getProperties("user_id");
        TreeMap<String, Object> params = new TreeMap<>();
        params.put("openid", openid);
        params.put("userid", userid);
        String timestamp = Long.toString(System.currentTimeMillis());
        String sign = doSign(params, timestamp, secret);
        Map headerMap = buildHeaders(appId, sign, timestamp);
        try {
            String fullMenuJson = HttpUtils.doGet(url, params, StandardCharsets.UTF_8.displayName(), headerMap);
            return JSON.parseArray(fullMenuJson, FullMenu.class);
        } catch (IOException e) {
            logger.error("获取用户全部菜单时异常", e);
            return null;
        }
    }

    public static PermissionApplyGuide permissionApplyGuide(User user, String keyName) {
        Environment environment = SSOConfiguration.getEnvironment();
        String appId = environment.getProperty("sso.appId");
        String secret = environment.getProperty("sso.secret");
        String permissionApplyGuideUrl = environment.getProperty("sso.permissionApplyGuideUrl");
        return permissionApplyGuide(permissionApplyGuideUrl, user, appId, secret, keyName);
    }

    public static PermissionApplyGuide permissionApplyGuide(String url, User user, String appId, String secret, String keyName) {
        Object openid = user.getProperties("openid");
        Object userid = user.getProperties("user_id");
        TreeMap<String, Object> params = new TreeMap<>();
        params.put("openid", openid);
        params.put("userid", userid);
        params.put("key_name", keyName);
        String timestamp = Long.toString(System.currentTimeMillis());
        String sign = doSign(params, timestamp, secret);
        Map<String, String> headers = buildHeaders(appId, sign, timestamp);
        try {
            String guideJson = HttpUtils.doGet(url, params, StandardCharsets.UTF_8.displayName(), headers);
            return JSON.parseObject(guideJson, PermissionApplyGuide.class);
        } catch (IOException e) {
            logger.error("", e);
            return null;
        }
    }

    private static Map<String, String> buildHeaders(String appId, String sign, String unitTime) {
        Map<String, String> map = new HashMap<>();
        map.put("X-App-Id", appId);
        map.put("X-Sign-Timestamp", unitTime);
        map.put("X-Sign", sign);
        map.put("Content-Type", "application/json");
        return map;
    }

    private static String doSign(TreeMap<String, Object> params, String timestamp, String secret) {
        try {
            String param = "";
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                param = param + (StringUtils.isNotEmpty(param) ? "&" : "") + entry.getKey() + "=" + entry.getValue();
            }
            String msg = param + timestamp + secret;
            return DigestUtil.encryptBASE64(DigestUtil.encryptMD5(msg.getBytes()));
        } catch (Exception e) {
            logger.error("", e);
            return null;
        }
    }
}
