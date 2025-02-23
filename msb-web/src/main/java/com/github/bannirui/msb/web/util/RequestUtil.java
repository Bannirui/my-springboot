package com.github.bannirui.msb.web.util;

import javax.servlet.http.HttpServletRequest;

public class RequestUtil {

    public static boolean isRequestAjax(HttpServletRequest request) {
        String ajaxHeader = request.getHeader("X-Requested-With");
        return "XMLHttpRequest".equals(ajaxHeader);
    }

    public static boolean isJsonRequest(HttpServletRequest request) {
        return request.getHeader("Accept") != null && request.getHeader("Accept").contains("application/json");
    }
}
